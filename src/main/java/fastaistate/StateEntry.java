package fastaistate;

import java.util.Objects;

/**
 * Immutable snapshot of a single state entry with generation/version tracking.
 *
 * @param key The entry key.
 * @param value The value associated with the key.
 * @param version Monotonically increasing revision number for CAS and delta tracking.
 * @param timestamp Epoch timestamp in milliseconds.
 */
public record StateEntry(String key, Object value, long version, long timestamp) {
    public StateEntry {
        Objects.requireNonNull(key, "key cannot be null");
    }

    /**
     * Casts the value to the requested type.
     */
    @SuppressWarnings("unchecked")
    public <T> T as(Class<T> type) {
        if (value == null) return null;
        if (!type.isInstance(value)) {
            throw new ClassCastException("Value for key '" + key + "' is of type " + value.getClass().getName() + ", not " + type.getName());
        }
        return (T) value;
    }
}
