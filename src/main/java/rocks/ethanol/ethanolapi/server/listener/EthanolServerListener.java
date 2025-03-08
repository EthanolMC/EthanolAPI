package rocks.ethanol.ethanolapi.server.listener;

import rocks.ethanol.ethanolapi.server.listener.exceptions.EthanolServerListenerAuthenticationException;
import rocks.ethanol.ethanolapi.server.listener.exceptions.EthanolServerListenerConnectException;
import rocks.ethanol.ethanolapi.server.listener.exceptions.EthanolServerListenerInvalidUpdateRateException;
import rocks.ethanol.ethanolapi.structure.ThrowingRunnable;
import rocks.ethanol.ethanolapi.structure.ThrowingSupplier;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class EthanolServerListener implements ThrowingRunnable<IOException>, Closeable {

    private static final ThrowingSupplier<Socket, IOException> DEFAULT_SOCKET_SUPPLIER = () -> {
        final Socket socket = new Socket();
        socket.connect(new InetSocketAddress("_ftp._tcp.ethanol.rocks", 12345));
        return socket;
    };

    private final Object lock;
    private final String authenticationCode;
    private final ThrowingSupplier<Socket, IOException> socketSupplier;
    private final ScheduledExecutorService service;
    private final int updateRate;
    private Socket socket;
    private DataOutputStream outputStream;
    private DataInputStream inputStream;
    private EthanolServer[] servers;

    public EthanolServerListener(final String authenticationCode, final ThrowingSupplier<Socket, IOException> socketSupplier, final ScheduledExecutorService service, final int updateRate) {
        this.authenticationCode = authenticationCode;
        this.socketSupplier = socketSupplier;
        this.service = service;
        this.updateRate = updateRate;
        this.servers = new EthanolServer[0];
        this.lock = new Object();
    }

    public static EthanolServerListener createDefault(final String authenticationCode, final int updateRate) {
        if (updateRate < 5_000 || updateRate > 60_000)
            throw new IllegalArgumentException("Update rate must be between 5 and 60 seconds");

        return new EthanolServerListener(authenticationCode, EthanolServerListener.DEFAULT_SOCKET_SUPPLIER, Executors.newSingleThreadScheduledExecutor(), updateRate);
    }

    public static EthanolServerListener createDefault(final String authenticationCode) {
        return EthanolServerListener.createDefault(authenticationCode, 10_000);
    }

    @Override
    public void run() throws IOException, EthanolServerListenerConnectException {
        this.socket = this.socketSupplier.get();
        this.socket.setSoTimeout(10_000);
        this.outputStream = new DataOutputStream(this.socket.getOutputStream());
        this.inputStream = new DataInputStream(this.socket.getInputStream());

        synchronized (this.lock) {
            this.outputStream.writeInt(this.updateRate);
            {
                final int status = this.inputStream.readUnsignedByte();
                if (status != 0) {
                    throw new EthanolServerListenerInvalidUpdateRateException();
                }
            }

            this.outputStream.writeUTF(this.authenticationCode);
            {
                final int status = this.inputStream.readUnsignedByte();
                if (status != 0) {
                    throw new EthanolServerListenerAuthenticationException(EthanolServerListenerAuthenticationException.Kind.getById(status));
                }
            }
        }

        this.requestServers();
        this.service.scheduleWithFixedDelay(() -> {
            try {
                this.requestServers();
            } catch (final IOException exception) {
                try {
                    this.close();
                } catch (final IOException ignored) { }
                throw new RuntimeException(exception);
            }
        }, this.updateRate, this.updateRate, TimeUnit.MILLISECONDS);
    }

    private void requestServers() throws IOException {
        synchronized (this.lock) {
            this.outputStream.write(1);
            if (this.inputStream.readUnsignedByte() != 1)
                throw new EOFException();

            final EthanolServer[] servers = new EthanolServer[this.inputStream.readInt()];
            for (int i = 0; i < servers.length; i++) {
                final byte[] address = new byte[this.inputStream.readUnsignedByte()];
                this.inputStream.read(address);
                final int port = this.inputStream.readInt();
                final String authentication = this.inputStream.readUTF();
                final String version = this.inputStream.readUTF();
                final int onlinePlayers = this.inputStream.readInt();
                final int maxPlayers = this.inputStream.readInt();
                servers[i] = new EthanolServer(new InetSocketAddress(InetAddress.getByAddress(address), port), authentication, version, onlinePlayers, maxPlayers);
            }
            this.servers = servers;
        }
    }

    @Override
    public void close() throws IOException {
        if (this.service != null) {
            this.service.shutdownNow();
        }
        if (this.socket != null && !this.socket.isClosed()) {
            this.socket.close();
        }
        this.inputStream = null;
        this.outputStream = null;
        this.socket = null;
    }

    public final EthanolServer[] getServers() {
        return this.servers;
    }
}
