package rocks.ethanol.ethanolapi;

import rocks.ethanol.ethanolapi.auth.DiscordAuthURL;
import rocks.ethanol.ethanolapi.auth.EthanolAuthenticator;
import rocks.ethanol.ethanolapi.server.connector.EthanolServerConnector;
import rocks.ethanol.ethanolapi.server.listener.EthanolServer;
import rocks.ethanol.ethanolapi.server.listener.EthanolServerListener;

import java.net.URI;

public class EthanolAPI {

    public static final EthanolAuthenticator DEFAULT_AUTHENTICATOR = new EthanolAuthenticator(new DiscordAuthURL(1196036069787975721L, URI.create("http://127.0.0.1:40000/auth"), "identify"));

    public static EthanolServerListener createServerListener(final String authenticationCode) {
        return EthanolServerListener.createDefault(authenticationCode);
    }

    public static EthanolServerListener createServerListener(final String authenticationCode, final int updateRate) {
        return EthanolServerListener.createDefault(authenticationCode, updateRate);
    }

    public static EthanolServerConnector connect(final String authentication) {
        return EthanolServerConnector.createDefault(authentication);
    }

    public static EthanolServerConnector connect(final EthanolServer server) {
        return EthanolServerConnector.createDefault(server.getAuthentication());
    }

}
