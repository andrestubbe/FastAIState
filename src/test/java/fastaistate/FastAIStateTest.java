package fastaistate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class FastAIStateTest {

    @BeforeEach
    public void setUp() {
        FastAIState.clearAll();
    }

    @Test
    public void testBasicStateOperations() {
        FastBlackboard bb = FastAIState.of("session-101");
        assertNotNull(bb);
        assertEquals("session-101", bb.getScopeId());

        bb.set("user_query", "Find latest AI benchmarks");
        bb.set("max_tokens", 4096);
        bb.set("active", true);

        assertEquals("Find latest AI benchmarks", bb.get("user_query"));
        assertEquals(4096, (Integer) bb.get("max_tokens"));
        assertTrue((Boolean) bb.get("active"));
        assertEquals(3, bb.size());
    }

    @Test
    public void testCompareAndSet() {
        FastBlackboard bb = FastAIState.create("scope-cas");
        StateEntry entry = bb.set("status", "PENDING");
        long initialVer = entry.version();

        // Successful CAS
        boolean success = bb.compareAndSet("status", initialVer, "RUNNING");
        assertTrue(success);
        assertEquals("RUNNING", bb.get("status"));

        // Stale CAS fails
        boolean failed = bb.compareAndSet("status", initialVer, "COMPLETED");
        assertFalse(failed);
        assertEquals("RUNNING", bb.get("status"));
    }

    @Test
    public void testStateChangeListener() {
        FastBlackboard bb = FastAIState.create("scope-listener");
        AtomicInteger eventCount = new AtomicInteger(0);

        bb.addListener("task_plan", (key, oldVal, newVal) -> {
            assertEquals("task_plan", key);
            eventCount.incrementAndGet();
        });

        bb.set("task_plan", "Step 1: Parse");
        bb.set("task_plan", "Step 2: Execute");
        bb.set("unrelated_key", 123);

        assertEquals(2, eventCount.get());
    }

    @Test
    public void testDeltaTracking() {
        FastBlackboard bb = FastAIState.create("scope-deltas");
        bb.set("k1", "v1");
        long checkpointVer = bb.getGlobalVersion();

        bb.set("k2", "v2");
        bb.set("k3", "v3");

        List<StateEntry> deltas = bb.getDeltasSince(checkpointVer);
        assertEquals(2, deltas.size());
        assertEquals("k2", deltas.get(0).key());
        assertEquals("k3", deltas.get(1).key());
    }

    @Test
    public void testBinarySerialization() {
        FastBlackboard bb = FastAIState.create("scope-ser");
        bb.set("agent_role", "Planner");
        bb.set("retry_count", "3");

        StateSnapshot snapshot = bb.snapshot();
        byte[] binary = FastStateSerializer.toBinary(snapshot);
        assertNotNull(binary);
        assertTrue(binary.length > 0);

        FastBlackboard restored = FastStateSerializer.fromBinary(binary);
        assertEquals("scope-ser", restored.getScopeId());
        assertEquals("Planner", restored.get("agent_role"));
        assertEquals("3", restored.get("retry_count"));
    }

    @Test
    public void testHighConcurrency() throws InterruptedException {
        FastBlackboard bb = FastAIState.create("scope-concurrency");
        int threads = 8;
        int opsPerThread = 10_000;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);

        for (int t = 0; t < threads; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    for (int i = 0; i < opsPerThread; i++) {
                        bb.set("thread_" + threadId, i);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();
        assertEquals(threads, bb.size());
    }
}
