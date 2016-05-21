package rx.javafx.sources;

public final class MapChange<K,T> {
    private final K key;
    private final T value;
    private final Flag flag;

    MapChange(K key, T value, Flag flag) {
        this.key = key;
        this.value = value;
        this.flag = flag;
    }

    public K getKey() {
        return key;
    }

    public T getValue() {
        return value;
    }

    public Flag getFlag() {
        return flag;
    }
}
