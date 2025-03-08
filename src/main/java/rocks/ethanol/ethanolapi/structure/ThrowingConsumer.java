package rocks.ethanol.ethanolapi.structure;

@FunctionalInterface
public interface ThrowingConsumer<T, E extends Throwable> {
    void accept(T o) throws E;
}
