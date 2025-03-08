package rocks.ethanol.ethanolapi.auth;

import rocks.ethanol.ethanolapi.utis.URLUtil;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class DiscordAuthURL {

    private final long clientId;
    private final URI redirectUri;
    private final String[] scopes;

    public DiscordAuthURL(final long clientId, final URI redirectUri, final String... scopes) {
        this.clientId = clientId;
        this.scopes = scopes;
        this.redirectUri = redirectUri;
    }

    @Override
    public final String toString() {
        return String.format("https://discord.com/api/oauth2/authorize?client_id=%s&response_type=code&redirect_uri=%s&scope=%s", this.clientId, URLUtil.urlEncode(this.redirectUri.toString()), String.join("%20", this.scopes));
    }

    public final URI toURI() {
        return URI.create(this.toString());
    }

    public final URL toURL() throws MalformedURLException {
        return this.toURI().toURL();
    }

    public final long getClientId() {
        return this.clientId;
    }

    public final URI getRedirectUri() {
        return this.redirectUri;
    }

    public final String[] getScopes() {
        return this.scopes;
    }
}
