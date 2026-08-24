package fastaistate;

/**
 * Functional callback listener for state change events.
 */
@FunctionalInterface
public interface StateChangeListener {
    /**
     * Invoked when a state entry changes.
     *
     * @param key The modified key.
     * @param oldValue The previous state entry (null if newly created).
     * @param newValue The new state entry (null if removed).
     */
    void onStateChanged(String key, StateEntry oldValue, StateEntry newValue);
}
