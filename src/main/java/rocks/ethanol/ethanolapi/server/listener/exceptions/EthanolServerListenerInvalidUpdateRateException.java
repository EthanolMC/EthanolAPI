package rocks.ethanol.ethanolapi.server.listener.exceptions;

public class EthanolServerListenerInvalidUpdateRateException extends EthanolServerListenerConnectException {

    public EthanolServerListenerInvalidUpdateRateException() {
        super("Invalid update rate");
    }
}
