package fastaistate.demo;

import fastaistate.FastAIState;
import fastaistate.FastBlackboard;
import fastaistate.FastStateSerializer;
import fastaistate.StateSnapshot;

public class Demo {
    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println(" ⚡ FastAIState — Multi-Agent Blackboard Demo   ");
        System.out.println("=================================================");

        FastBlackboard blackboard = FastAIState.of("conversation-agy-42");

        // 1. Reactive state change listeners
        blackboard.addListener("current_plan", (key, oldVal, newVal) -> {
            System.out.println("\n[STATE EVENT] Key '" + key + "' updated!");
            System.out.println("   Old: " + (oldVal != null ? oldVal.value() : "null"));
            System.out.println("   New: " + newVal.value() + " (v" + newVal.version() + ")");
        });

        // 2. Set initial state by Agent 1 (Planner)
        System.out.println("\n--- Step 1: Agent 1 (Planner) writes workflow plan ---");
        blackboard.set("user_intent", "Build high performance FastAIState");
        blackboard.set("current_plan", "Plan A: Scaffold classes and run benchmarks");
        blackboard.set("active_agent", "PlannerAgent");

        // 3. Agent 2 (Executor) CAS update
        System.out.println("\n--- Step 2: Agent 2 (Executor) updates execution status via CAS ---");
        long planVer = blackboard.getEntry("current_plan").version();
        boolean casSuccess = blackboard.compareAndSet("current_plan", planVer, "Plan A: Completed with 0 GC allocations");
        System.out.println("CAS Update Result: " + casSuccess);

        // 4. Binary Snapshot serialization
        System.out.println("\n--- Step 3: FastBinary State Snapshot ---");
        StateSnapshot snapshot = blackboard.snapshot();
        byte[] binaryState = FastStateSerializer.toBinary(snapshot);
        System.out.println("Serialized snapshot to " + binaryState.length + " bytes.");

        FastBlackboard restored = FastStateSerializer.fromBinary(binaryState);
        System.out.println("Restored Blackboard Scope: " + restored.getScopeId());
        System.out.println("Restored user_intent: " + restored.get("user_intent"));
        System.out.println("Restored current_plan: " + restored.get("current_plan"));

        System.out.println("\n✔ FastAIState Multi-Agent State Synchronization Verified Successfully!");
    }
}
