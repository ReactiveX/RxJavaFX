package rx.javafx.sources;

public final class ListChange<T> {
    private final T value;
    private final Flag flag;

    private ListChange(T value, Flag flag) {
        this.value = value;
        this.flag = flag;
    }
    public static <T> ListChange<T> of(T value, Flag flag) {
        return new ListChange<>(value, flag);
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
