package fastaistate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/**
 * High-performance, lock-free shared blackboard state for multi-agent workflows.
 */
public class FastBlackboard {
    private final String scopeId;
    private final ConcurrentHashMap<String, StateEntry> storage = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<StateChangeListener>> keyListeners = new ConcurrentHashMap<>();
    private final List<StateChangeListener> globalListeners = new CopyOnWriteArrayList<>();
    private final AtomicLong globalVersion = new AtomicLong(1);

    public FastBlackboard(String scopeId) {
        this.scopeId = Objects.requireNonNull(scopeId, "scopeId cannot be null");
    }

    public String getScopeId() {
        return scopeId;
    }

    /**
     * Puts a value into the blackboard.
     *
     * @param key Key name.
     * @param value Arbitrary state value.
     * @return The updated StateEntry.
     */
    public StateEntry set(String key, Object value) {
        Objects.requireNonNull(key, "key cannot be null");
        long ver = globalVersion.incrementAndGet();
        long now = System.currentTimeMillis();
        StateEntry newEntry = new StateEntry(key, value, ver, now);

        StateEntry oldEntry = storage.put(key, newEntry);
        notifyListeners(key, oldEntry, newEntry);
        return newEntry;
    }

    /**
     * Gets the current value for a key.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        StateEntry entry = storage.get(key);
        return entry != null ? (T) entry.value() : null;
    }

    /**
     * Gets a typed value with a default fallback.
     */
    @SuppressWarnings("unchecked")
    public <T> T getOrDefault(String key, T defaultValue) {
        StateEntry entry = storage.get(key);
        return (entry != null && entry.value() != null) ? (T) entry.value() : defaultValue;
    }

    /**
     * Gets the full StateEntry metadata.
     */
    public StateEntry getEntry(String key) {
        return storage.get(key);
    }

    /**
     * Checks if a key exists in state.
     */
    public boolean contains(String key) {
        return storage.containsKey(key);
    }

    /**
     * Atomic Compare-And-Swap (CAS) update based on version.
     *
     * @param key State key.
     * @param expectedVersion Expected entry version.
     * @param newValue New value to set if version matches.
     * @return True if update succeeded, false if conflict occurred.
     */
    public boolean compareAndSet(String key, long expectedVersion, Object newValue) {
        Objects.requireNonNull(key, "key cannot be null");
        StateEntry current = storage.get(key);
        long curVer = current != null ? current.version() : 0L;

        if (curVer != expectedVersion) {
            return false;
        }

        long newVer = globalVersion.incrementAndGet();
        long now = System.currentTimeMillis();
        StateEntry next = new StateEntry(key, newValue, newVer, now);

        if (current == null) {
            if (storage.putIfAbsent(key, next) == null) {
                notifyListeners(key, null, next);
                return true;
            }
            return false;
        } else {
            if (storage.replace(key, current, next)) {
                notifyListeners(key, current, next);
                return true;
            }
            return false;
        }
    }

    /**
     * Atomically computes a new value.
     */
    public StateEntry compute(String key, Function<StateEntry, Object> computeFn) {
        Objects.requireNonNull(key, "key cannot be null");
        while (true) {
            StateEntry current = storage.get(key);
            Object nextVal = computeFn.apply(current);
            long expectedVer = current != null ? current.version() : 0L;
            long newVer = globalVersion.incrementAndGet();
            long now = System.currentTimeMillis();
            StateEntry next = new StateEntry(key, nextVal, newVer, now);

            if (current == null) {
                if (storage.putIfAbsent(key, next) == null) {
                    notifyListeners(key, null, next);
                    return next;
                }
            } else {
                if (storage.replace(key, current, next)) {
                    notifyListeners(key, current, next);
                    return next;
                }
            }
        }
    }

    /**
     * Removes an entry from the state.
     */
    public StateEntry remove(String key) {
        StateEntry old = storage.remove(key);
        if (old != null) {
            notifyListeners(key, old, null);
        }
        return old;
    }

    /**
     * Creates an immutable point-in-time snapshot of the blackboard.
     */
    public StateSnapshot snapshot() {
        return new StateSnapshot(scopeId, globalVersion.get(), new HashMap<>(storage));
    }

    /**
     * Returns delta changes since a given global version.
     */
    public List<StateEntry> getDeltasSince(long sinceVersion) {
        List<StateEntry> deltas = new ArrayList<>();
        for (StateEntry entry : storage.values()) {
            if (entry.version() > sinceVersion) {
                deltas.add(entry);
            }
        }
        deltas.sort(Comparator.comparingLong(StateEntry::version));
        return Collections.unmodifiableList(deltas);
    }

    /**
     * Registers a listener for a specific key.
     */
    public void addListener(String key, StateChangeListener listener) {
        keyListeners.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    /**
     * Registers a global listener for all state mutations.
     */
    public void addGlobalListener(StateChangeListener listener) {
        globalListeners.add(listener);
    }

    /**
     * Clears all state in this blackboard.
     */
    public void clear() {
        storage.clear();
    }

    /**
     * Returns the number of state keys.
     */
    public int size() {
        return storage.size();
    }

    /**
     * Returns the current global version.
     */
    public long getGlobalVersion() {
        return globalVersion.get();
    }

    private void notifyListeners(String key, StateEntry oldEntry, StateEntry newEntry) {
        List<StateChangeListener> list = keyListeners.get(key);
        if (list != null) {
            for (StateChangeListener l : list) {
                l.onStateChanged(key, oldEntry, newEntry);
            }
        }
        for (StateChangeListener l : globalListeners) {
            l.onStateChanged(key, oldEntry, newEntry);
        }
    }
}
