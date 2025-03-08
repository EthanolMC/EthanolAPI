package rocks.ethanol.ethanolapi.auth;

import rocks.ethanol.ethanolapi.structure.ThrowingConsumer;
import rocks.ethanol.ethanolapi.utis.URLUtil;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EthanolAuthenticator {

    public static final ThrowingConsumer<DiscordAuthURL, IOException> DESKTOP_OPENER = url -> Desktop.getDesktop().browse(url.toURI());

    private static final Pattern PATTERN = Pattern.compile("^GET (.*) HTTP/.*$");

    private final DiscordAuthURL url;

    public EthanolAuthenticator(final DiscordAuthURL url) {
        this.url = url;
    }

    private void openAuthSite(final ThrowingConsumer<DiscordAuthURL, IOException> opener) throws IOException {
        opener.accept(this.url);
    }

    private String catchAuthenticationResponse(final int timeout) throws IOException {
        final ServerSocket server = new ServerSocket(this.url.getRedirectUri().getPort());
        if (timeout > 0) {
            server.setSoTimeout(timeout);
        }

        while (true) {
            final Socket socket = server.accept();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            final Matcher matcher = EthanolAuthenticator.PATTERN.matcher(reader.readLine());
            if (!matcher.matches())
                continue;

            final String path = matcher.group(1);
            final int queryIndex = path.indexOf("?");
            if (queryIndex == -1 || path.length() < queryIndex + 1)
                continue;

            final Map<String, String> query = URLUtil.extractQuery(path.substring(queryIndex + 1));
            if (!query.containsKey("code"))
                continue;

            socket.close();
            server.close();
            return query.get("code");
        }
    }

    public final String authenticate(final int timeout, final ThrowingConsumer<DiscordAuthURL, IOException> opener) throws IOException {
        this.openAuthSite(opener);
        return this.catchAuthenticationResponse(timeout);
    }

    public final CompletableFuture<String> authenticateAsync(final int timeout, final ThrowingConsumer<DiscordAuthURL, IOException> opener) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return this.authenticate(timeout, opener);
            } catch (final IOException exception) {
                throw new RuntimeException(exception);
            }
        });
    }

    public final DiscordAuthURL getUrl() {
        return this.url;
    }
}
