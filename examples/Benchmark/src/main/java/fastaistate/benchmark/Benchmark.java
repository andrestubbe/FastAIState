package fastaistate.benchmark;

import fastaistate.FastAIState;
import fastaistate.FastBlackboard;
import fastaistate.FastStateSerializer;
import fastaistate.StateSnapshot;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 3, time = 1)
@Fork(1)
public class Benchmark {

    private FastBlackboard blackboard;
    private StateSnapshot snapshot;
    private byte[] binaryData;

    @Setup
    public void setup() {
        blackboard = FastAIState.create("bench-scope");
        for (int i = 0; i < 50; i++) {
            blackboard.set("key_" + i, "val_" + i);
        }
        snapshot = blackboard.snapshot();
        binaryData = FastStateSerializer.toBinary(snapshot);
    }

    @org.openjdk.jmh.annotations.Benchmark
    public Object benchmarkStateRead() {
        return blackboard.get("key_25");
    }

    @org.openjdk.jmh.annotations.Benchmark
    public Object benchmarkStateWrite() {
        return blackboard.set("hot_key", "updated_value");
    }

    @org.openjdk.jmh.annotations.Benchmark
    public Object benchmarkCompareAndSet() {
        long ver = blackboard.getGlobalVersion();
        return blackboard.compareAndSet("hot_key", ver, "cas_value");
    }

    @org.openjdk.jmh.annotations.Benchmark
    public byte[] benchmarkBinarySerialization() {
        return FastStateSerializer.toBinary(snapshot);
    }

    @org.openjdk.jmh.annotations.Benchmark
    public FastBlackboard benchmarkBinaryDeserialization() {
        return FastStateSerializer.fromBinary(binaryData);
    }
}
