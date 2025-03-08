import rocks.ethanol.ethanolapi.EthanolAPI;
import rocks.ethanol.ethanolapi.auth.EthanolAuthenticator;
import rocks.ethanol.ethanolapi.server.listener.EthanolServerListener;
import rocks.ethanol.ethanolapi.server.listener.exceptions.EthanolServerListenerAuthenticationException;

import java.io.IOException;

public class Bootstrap {


    public static void main(final String... args) throws IOException {
        try (final EthanolServerListener serverListener = EthanolServerListener.createDefault(EthanolAPI.DEFAULT_AUTHENTICATOR.authenticate(60_000, EthanolAuthenticator.DESKTOP_OPENER))) {
            serverListener.run();
            EthanolAPI.connect(serverListener.getServers()[0]).startAsync().whenComplete((connector, throwable) -> {
                if (throwable != null) {
                    throwable.printStackTrace();
                    return;
                }

                connector.listen(System.out::println);
                try {
                    connector.writeLine("help");
                } catch (final IOException exception) {
                    throw new RuntimeException(exception);
                }
            });
        } catch (final EthanolServerListenerAuthenticationException exception) {
            System.err.println(exception.getKind());
        }
    }

}
