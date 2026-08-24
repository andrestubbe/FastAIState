package fastaistate;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable point-in-time snapshot of the shared state.
 */
public record StateSnapshot(String scopeId, long snapshotVersion, Map<String, StateEntry> entries) {
    public StateSnapshot {
        Objects.requireNonNull(scopeId, "scopeId cannot be null");
        entries = Collections.unmodifiableMap(entries != null ? entries : Collections.emptyMap());
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        StateEntry entry = entries.get(key);
        return entry != null ? (T) entry.value() : null;
    }

    public boolean contains(String key) {
        return entries.containsKey(key);
    }
}
