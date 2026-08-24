package fastaistate;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Global registry and coordinator for scoped blackboards in multi-agent workflows.
 */
public final class FastAIState {
    private static final ConcurrentHashMap<String, FastBlackboard> BLACKBOARDS = new ConcurrentHashMap<>();

    private FastAIState() {}

    /**
     * Gets or creates a scoped blackboard.
     *
     * @param scopeId Unique scope or conversation ID.
     * @return FastBlackboard instance.
     */
    public static FastBlackboard of(String scopeId) {
        return BLACKBOARDS.computeIfAbsent(scopeId, FastBlackboard::new);
    }

    /**
     * Creates a new unmanaged blackboard.
     */
    public static FastBlackboard create(String scopeId) {
        return new FastBlackboard(scopeId);
    }

    /**
     * Removes and frees a scoped blackboard.
     */
    public static FastBlackboard remove(String scopeId) {
        return BLACKBOARDS.remove(scopeId);
    }

    /**
     * Clears all blackboards in memory.
     */
    public static void clearAll() {
        BLACKBOARDS.clear();
    }
}
