# FastAIState 0.1.0 [ALPHA] — Lock-Free Shared Blackboard & Agent State for Java

[![Status](https://img.shields.io/badge/status-0.1.0-brightgreen.svg)](https://github.com/andrestubbe/FastAIState/releases/tag/0.1.0)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20Linux%20%7C%20macOS-lightgrey.svg)]()
[![JitPack](https://img.shields.io/badge/JitPack-ready-green.svg)](https://jitpack.io/#andrestubbe/FastAIState)

---

**⚡ High-performance lock-free shared blackboard memory, delta revision tracking, and binary state snapshot engine for multi-agent workflows.**

**FastAIState** provides a shared blackboard coordination memory for multi-agent execution graphs, automated task pipelines, and tool execution environments. It eliminates prompt context stuffing by offering a lock-free, thread-safe, observable key-value store with atomic CAS (Compare-And-Swap), delta tracking, and zero-allocation binary serialization via **[FastBinary](https://github.com/andrestubbe/FastBinary)**.

---

## Key Features

- **⚡ Lock-Free Concurrency** — Atomic CAS (`compareAndSet`) updates with monotonically increasing generation revisions.
- **🔄 Delta & Revision Tracking** — Microsecond delta extraction (`getDeltasSince`) to stream state diffs across distributed workers.
- **📡 Reactive State Listeners** — Key-specific and global change listeners (`StateChangeListener`) for event-driven orchestration.
- **💾 FastBinary State Snapshots** — High-density binary state serialization (`FastStateSerializer`) powered by LEB128 VarInts.
- **🌐 Zero Dependencies** — Native-speed pure Java 17+ architecture backed by `FastCore` and `FastBinary`.

---

## Real-World Scenarios

- **🤖 Multi-Agent Coordination** — Shared blackboard for planner, coder, and reviewer subagents without prompt token pollution.
- **🛠️ Tool & Environment State** — Storing live workspace variables, session parameters, and execution flags across tool calls.
- **📑 Session Checkpoints & Replays** — Saving point-in-time state snapshots to disk for deterministic re-runs and rollbacks.
- **⚡ Reactive UI & Telemetry Synchronization** — Hooking UI panels and console monitors directly to state mutation events.

---

## Performance Benchmarks

FastAIState is profiled using **JMH** to guarantee ultra-low latency and lock-free execution under massive concurrency.

| Benchmark Operation | Score (ops/ms) | Ops per Second | Memory Allocation |
|---|---|---|---|
| **Compare-And-Swap (CAS)** | **~302,000 ops/ms** | **> 302 Million** | **0 bytes / op (Zero GC)** |
| **Blackboard State Read** | **~104,000 ops/ms** | **> 104 Million** | **0 bytes / op (Zero GC)** |
| **Blackboard State Write** | **~15,500 ops/ms** | **> 15.5 Million** | **Minimal entry overhead** |
| **Binary State Snapshot Serialization** | **~93,000 ops/ms** | **> 93,000 / sec** | **High-density VarInt stream** |
| **Binary State Snapshot Deserialization** | **~82,000 ops/ms** | **> 82,000 / sec** | **Zero-copy decoding** |

*Run the benchmarks locally:* `.\run-benchmark.bat`

---

## API Quick Reference

| Method / Class | Description |
|---|---|
| `FastAIState.of("scopeId")` | Gets or creates a scoped shared blackboard instance. |
| `blackboard.set("key", value)` | Sets a state value and bumps the global revision version. |
| `blackboard.get("key")` | Retrieves a state value by key. |
| `blackboard.compareAndSet(key, ver, val)` | Atomically updates value if expected version matches. |
| `blackboard.addListener(key, listener)` | Registers a reactive change listener for a specific key. |
| `blackboard.getDeltasSince(version)` | Returns list of state entries modified after given revision. |
| `FastStateSerializer.toBinary(snapshot)` | Encodes state snapshot into a compact FastBinary payload. |
| `FastStateSerializer.fromBinary(bytes)` | Restores blackboard state from binary bytes. |

---

## Quick Start

```java
import fastaistate.*;

public class Example {
    public static void main(String[] args) {
        FastBlackboard blackboard = FastAIState.of("workflow-task-1");

        // 1. Reactive listener
        blackboard.addListener("agent_status", (key, oldVal, newVal) -> {
            System.out.println("Status changed: " + newVal.value());
        });

        // 2. Write state
        blackboard.set("agent_status", "PLANNING");

        // 3. Atomic CAS update
        long ver = blackboard.getEntry("agent_status").version();
        blackboard.compareAndSet("agent_status", ver, "EXECUTING");

        // 4. Compact Binary Snapshot
        byte[] binary = FastStateSerializer.toBinary(blackboard.snapshot());
        FastBlackboard restored = FastStateSerializer.fromBinary(binary);
    }
}
```

---

## Installation

### JitPack (Maven)

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastAIState</artifactId>
        <version>0.1.0</version>
    </dependency>
</dependencies>
```

---

## License

MIT License. See [LICENSE](LICENSE) file for details.

---

**Part of the FastJava Ecosystem** — *Making the JVM faster. Small package. Maximum speed. Zero bloat. 🚀📋*
