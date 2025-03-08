# Ethanol API

An API for managing and automating actions for your Ethanol servers.

## Example usage
```java
public class Example {
    public static void main(final String[] args) {
        final EthanolAuthenticator authenticator = EthanolAPI.DEFAULT_AUTHENTICATOR;
        System.out.println("Auth URL: " + authenticator.getUrl());
        final String authKey = authenticator.authenticate(30000, EthanolAuthenticator.DESKTOP_OPENER);
        final EthanolServerListener serverListener = EthanolAPI.createServerListener(authKey);
        serverListener.run();
        final EthanolServer[] servers = serverListener.getServers();
        try {
            for (final EthanolServer server : servers) {
                final EthanolServerConnector connector = EthanolServerConnector.createDefault(server.getAuthentication());
                connector.run();
                System.out.println("Connected to server, sending serverinfo command ;3");
                connector.writeLine("serverinfo");
                System.out.println("(" + server.getAddress().toString() + ") | " + connector.readLine());
                connector.close();
            }
        } catch (final Exception exception) {
            exception.printStackTrace();
        }
    }
}
```