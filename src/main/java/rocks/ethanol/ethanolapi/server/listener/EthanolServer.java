package rocks.ethanol.ethanolapi.server.listener;

import java.net.InetSocketAddress;

public class EthanolServer {

    private final InetSocketAddress address;
    private final String authentication;
    private final String version;
    private final int onlinePlayers;
    private final int maxPlayers;

    public EthanolServer(final InetSocketAddress address, final String authentication, final String version, final int onlinePlayers, final int maxPlayers) {
        this.address = address;
        this.authentication = authentication;
        this.version = version;
        this.onlinePlayers = onlinePlayers;
        this.maxPlayers = maxPlayers;
    }

    @Override
    public String toString() {
        return "EthanolServer{" +
                "address=" + this.address +
                ", authentication='" + this.authentication + '\'' +
                ", version='" + this.version + '\'' +
                ", onlinePlayers=" + this.onlinePlayers +
                ", maxPlayers=" + this.maxPlayers +
                '}';
    }

    public final InetSocketAddress getAddress() {
        return this.address;
    }

    public final String getAuthentication() {
        return this.authentication;
    }

    public final String getVersion() {
        return this.version;
    }

    public final int getOnlinePlayers() {
        return this.onlinePlayers;
    }

    public final int getMaxPlayers() {
        return this.maxPlayers;
    }
}
