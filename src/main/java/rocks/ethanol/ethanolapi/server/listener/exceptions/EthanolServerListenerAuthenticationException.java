package rocks.ethanol.ethanolapi.server.listener.exceptions;

import java.util.Arrays;

public class EthanolServerListenerAuthenticationException extends EthanolServerListenerConnectException {

    private final Kind kind;

    public EthanolServerListenerAuthenticationException(final Kind kind) {
        super("Authentication failed: ".concat(kind.name()));
        this.kind = kind;
    }

    public final Kind getKind() {
        return this.kind;
    }

    public enum Kind {
        UNKNOWN(-1),
        INVALID_CODE(1),
        NOT_A_MEMBER(1),
        NOT_VERIFIED(2),
        NO_TERMS_OF_SERVICE(3);

        private final int id;

        Kind(final int id) {
            this.id = id;
        }

        public static Kind getById(final int id) {
            return Arrays.stream(Kind.values()).filter(kind -> kind.id == id).findFirst().orElse(Kind.UNKNOWN);
        }
    }

}
