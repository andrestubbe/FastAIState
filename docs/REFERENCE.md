# FastAIState API Reference

## Classes & Interfaces

### 1. `fastaistate.FastAIState`
* `public static FastBlackboard of(String scopeId)`: Gets or creates a scoped shared blackboard instance.
* `public static FastBlackboard create(String scopeId)`: Creates an unmanaged blackboard instance.
* `public static FastBlackboard remove(String scopeId)`: Removes a scoped blackboard from global registry.
* `public static void clearAll()`: Clears all blackboards in memory.

### 2. `fastaistate.FastBlackboard`
* `public StateEntry set(String key, Object value)`: Updates or inserts a state key with a new monotonically increasing revision version.
* `public <T> T get(String key)`: Retrieves value cast to requested type.
* `public <T> T getOrDefault(String key, T defaultValue)`: Retrieves value with fallback.
* `public StateEntry getEntry(String key)`: Retrieves full metadata entry (`key`, `value`, `version`, `timestamp`).
* `public boolean compareAndSet(String key, long expectedVersion, Object newValue)`: Atomic CAS update.
* `public StateEntry compute(String key, Function<StateEntry, Object> computeFn)`: Atomic computation.
* `public StateEntry remove(String key)`: Removes key from state.
* `public StateSnapshot snapshot()`: Creates point-in-time snapshot.
* `public List<StateEntry> getDeltasSince(long sinceVersion)`: Returns all entries updated after `sinceVersion`.
* `public void addListener(String key, StateChangeListener listener)`: Key-specific event listener.
* `public void addGlobalListener(StateChangeListener listener)`: Global state event listener.

### 3. `fastaistate.FastStateSerializer`
* `public static byte[] toBinary(StateSnapshot snapshot)`: Serializes state to compact binary payload.
* `public static FastBlackboard fromBinary(byte[] bytes)`: Deserializes binary state to FastBlackboard.
