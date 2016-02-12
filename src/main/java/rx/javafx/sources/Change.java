package rx.javafx.sources;

public final class Change<T> {
    private final T oldVal;
    private final T newVal;

    public Change(T oldVal, T newVal) {
        this.oldVal = oldVal;
        this.newVal = newVal;
    }
    public T getOldVal() {
        return oldVal;
    }
    public T getNewVal() {
        return newVal;
    }
}
