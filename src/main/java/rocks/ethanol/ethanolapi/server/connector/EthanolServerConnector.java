package rocks.ethanol.ethanolapi.server.connector;

import rocks.ethanol.ethanolapi.server.connector.exceptions.EthanolConnectorAuthenticateException;
import rocks.ethanol.ethanolapi.server.connector.exceptions.EthanolConnectorConnectException;
import rocks.ethanol.ethanolapi.server.connector.io.CustomBufferedReader;
import rocks.ethanol.ethanolapi.server.connector.io.CustomBufferedWriter;
import rocks.ethanol.ethanolapi.structure.ThrowingRunnable;
import rocks.ethanol.ethanolapi.structure.ThrowingSupplier;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class EthanolServerConnector implements ThrowingRunnable<IOException>, Closeable {

    private static final ThrowingSupplier<Socket, IOException> DEFAULT_SOCKET_SUPPLIER = () -> {
        final Socket socket = new Socket();
        socket.connect(new InetSocketAddress("_ftp._tcp.ethanol.rocks", 39997));
        return socket;
    };

    private final String authentication;
    private final ThrowingSupplier<Socket, IOException> socketSupplier;
    private final ExecutorService service;
    private CustomBufferedReader reader;
    private CustomBufferedWriter writer;
    private Socket socket;
    private final List<Consumer<String>> listeners;
    private boolean listening;
    private boolean closed;
    private boolean opened;

    public EthanolServerConnector(final String authentication, final ThrowingSupplier<Socket, IOException> socketSupplier, final ExecutorService service) {
        this.authentication = authentication;
        this.socketSupplier = socketSupplier;
        this.service = service;
        this.listeners = new CopyOnWriteArrayList<>();
        this.listening = false;
        this.closed = false;
        this.opened = false;
    }

    public static EthanolServerConnector createDefault(final String authentication) {
        return new EthanolServerConnector(authentication, EthanolServerConnector.DEFAULT_SOCKET_SUPPLIER, Executors.newFixedThreadPool(2));
    }

    @Override
    public void run() throws IOException, EthanolConnectorConnectException {
        if (this.opened || this.closed) {
            throw new IllegalStateException();
        }

        this.opened = true;
        this.socket = this.socketSupplier.get();
        this.socket.setSoTimeout(10_000);
        this.socket.setReuseAddress(true);

        final InetSocketAddress local = (InetSocketAddress) this.socket.getLocalSocketAddress();
        final DataOutputStream outputStream = new DataOutputStream(this.socket.getOutputStream());
        final DataInputStream inputStream = new DataInputStream(this.socket.getInputStream());

        {
            final String[] split = this.authentication.split(":");
            if (split.length != 2)
                throw new EthanolConnectorAuthenticateException();

            {
                final UUID id = UUID.fromString(split[0]);
                outputStream.writeLong(id.getMostSignificantBits());
                outputStream.writeLong(id.getLeastSignificantBits());
            }

            {
                final UUID key = UUID.fromString(split[1]);
                outputStream.writeLong(key.getMostSignificantBits());
                outputStream.writeLong(key.getLeastSignificantBits());
            }
        }

        final int status = inputStream.read();
        if (status != 0) {
            this.close();
            throw new EthanolConnectorAuthenticateException();
        }
        final InetSocketAddress target = EthanolServerConnector.readAddress(inputStream);
        this.socket.close();
        this.socket = new Socket();
        this.socket.setReuseAddress(true);
        this.socket.bind(local);
        this.socket.connect(target, 10_000);
        this.reader = new CustomBufferedReader(new InputStreamReader(this.socket.getInputStream()));
        this.writer = new CustomBufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
    }

    public CompletableFuture<EthanolServerConnector> startAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                this.run();
            } catch (final IOException exception) {
                throw new RuntimeException(exception);
            }
            return this;
        }, this.service);
    }

    public final String readLine() throws IOException {
        if (this.listening) {
            throw new IllegalStateException("Cannot read line while listening!");
        }

        return this.reader.readLine();
    }

    public final void writeLine(final String line) throws IOException {
        this.writer.writeLine(line);
    }

    public void listen(final Consumer<String> listener) {
        this.listeners.add(listener);
        if (!this.listening) {
            this.listening = true;
            this.service.execute(() -> {
                try {
                    while (this.socket.isConnected()) {
                        final String line = this.reader.readLine();
                        for (final Consumer<String> singleListener : this.listeners) {
                            singleListener.accept(line);
                        }
                    }
                } catch (final IOException exception) {
                    throw new RuntimeException(exception);
                }
                try {
                    this.close();
                } catch (final IOException ignored) { }
            });
        }
    }

    private static InetSocketAddress readAddress(final DataInputStream dataInputStream) throws IOException {
        final byte[] address = new byte[dataInputStream.readUnsignedByte()];
        if (address.length != 4 && address.length != 16)
            throw new IllegalStateException("Invalid IP-Address");

        dataInputStream.readFully(address);

        final int port = dataInputStream.readUnsignedShort();
        return new InetSocketAddress(InetAddress.getByAddress(address), port);
    }


    @Override
    public void close() throws IOException {
        this.closed = true;
        this.opened = false;
        if (this.service != null && !this.service.isShutdown()) {
            this.service.shutdownNow();
        }
        if (this.socket != null && !this.socket.isClosed()) {
            this.socket.close();
        }
        this.socket = null;
    }

    public final boolean isOpened() {
        return this.opened;
    }

    public final boolean isClosed() {
        return this.closed;
    }
}
