package rocks.ethanol.ethanolapi.structure;

@FunctionalInterface
public interface ThrowingRunnable<E extends Throwable> {
    void run() throws E;
}
