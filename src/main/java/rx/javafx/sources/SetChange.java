package rx.javafx.sources;

public final class SetChange<T> {
    private final T value;
    private final Flag flag;

    SetChange(T value, Flag flag) {
        this.value = value;
        this.flag = flag;
    }
    public T getValue() {
        return value;
    }
    public Flag getFlag() {
        return flag;
    }
    @Override
    public String toString() {
        return flag + " " + value;
    }
}
