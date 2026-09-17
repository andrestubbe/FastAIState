> [!WARNING]
> **🚧 WIP — Active AI Pipeline Construction & Architecture Optimization in Progress.**

# FastAIState [ALPHA-2026-09-08] — Lock-Free Shared Blackboard & Agent State for Java

[![Status](https://img.shields.io/badge/status-0.1.2-brightgreen.svg)](https://github.com/andrestubbe/FastAIState/releases/tag/0.1.2)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+-lightgrey.svg)]()
[![JitPack](https://img.shields.io/badge/JitPack-ready-green.svg)](https://jitpack.io/#andrestubbe/FastAIState)

---

**⚡ High-performance lock-free shared blackboard memory, delta revision tracking, and binary state snapshot engine for multi-agent workflows.**

**FastAIState** provides a shared blackboard coordination memory for multi-agent execution graphs, automated task pipelines, and tool execution environments. It eliminates prompt context stuffing by offering a lock-free, thread-safe, observable key-value store with atomic CAS (Compare-And-Swap), delta tracking, and zero-allocation binary serialization via **[FastFileFormat](https://github.com/andrestubbe/FastFileFormat)** & **[FastBinary](https://github.com/andrestubbe/FastBinary)**.

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

        // 4. Compact FastFileFormat Binary Snapshot
        byte[] binary = FastStateSerializer.toBinary(blackboard.snapshot());
        FastBlackboard restored = FastStateSerializer.fromBinary(binary);
    }
}
```

---

## Table of Contents

- [Why FastAIState?](#why-fastaistate)
- [Key Features](#key-features)
- [Architecture Overview](#architecture-overview)
- [Performance Benchmarks](#performance-benchmarks)
- [API Quick Reference](#api-quick-reference)
- [Technical Demos & Benchmarks](#technical-demos--benchmarks)
- [Installation](#installation)
- [Documentation](#documentation)
- [Platform Support](#platform-support)
- [License](#license)
- [Related Projects](#related-projects)

---

## Why FastAIState?

Traditional multi-agent frameworks pass entire conversational state trees and task context as bloated prompt strings or rely on heavy external databases:

- **Token Exhaustion & Prompt Bloat** — Stuffing global execution state into every LLM call rapidly hits token limits and multiplies inference costs.
- **Concurrency Bottlenecks & Race Conditions** — Synchronizing concurrent subagents with coarse mutexes causes thread contention and latency spikes.
- **Serialization Overhead** — Bulky JSON state dumps consume excessive CPU cycles and memory allocations during rapid task iteration loops.

FastAIState eliminates these issues by decoupling shared memory into an ultra-fast in-memory blackboard with zero-allocation binary snapshots:

| Feature | Prompt Context Passing | Redis / Relational State | FastAIState |
|:---|:---|:---|:---|
| **Access Latency** | 500–2,000 ms (LLM Round-trip) | 1–5 ms (Network RPC) | < 10 ns (Direct memory) |
| **Concurrency Model** | ❌ None (Single-threaded prompt) | ⚠️ Distributed locking / Transactions | ✅ Lock-Free atomic CAS (`compareAndSet`) |
| **State Delta Tracking** | ❌ Full context dump required | ⚠️ Manual change CDC logging | ✅ Built-in monotonically increasing revision versions |
| **Serialization Overhead** | High token consumption | 5–20 KB JSON strings | Compact binary streams via FastBinary / FastFileFormat |
| **GC Pressure** | Token encoding garbage | High JSON object allocations | Zero GC on reads & CAS |

---

## Key Features

- ⚡ **Lock-Free Concurrency** — Atomic CAS (`compareAndSet`) updates with monotonically increasing generation revisions.
- 🔄 **Delta & Revision Tracking** — Microsecond delta extraction (`getDeltasSince`) to stream state diffs across distributed workers.
- 📡 **Reactive State Listeners** — Key-specific and global change listeners (`StateChangeListener`) for event-driven orchestration.
- 💾 **FastFileFormat State Snapshots** — Dual-format state serialization (`FastStateSerializer`) with standard 12-byte header and VarInt streams.
- 🌐 **Zero Dependencies** — Native-speed pure Java 17+ architecture backed by `FastCore`, `FastBinary`, and `FastFileFormat`.

---

## Architecture Overview

FastAIState integrates directly with the FastJava AI multi-agent orchestration stack:

- 🧠 **[FastAIState](https://github.com/andrestubbe/FastAIState)** (Shared Memory): Lock-free blackboard coordinating multi-agent task and session variables.
- 🤖 **[FastAIAgent](https://github.com/andrestubbe/FastAIAgent)** (Autonomous Control): Drives multi-agent plan and execution loops referencing state tokens.
- 🧩 **[FastAIReasoner](https://github.com/andrestubbe/FastAIReasoner)** (Deterministic Reasoning): Inspects state snapshots to construct reasoning graphs and trees.
- ⚡ **[FastAIRuntime](https://github.com/andrestubbe/FastAIRuntime)** (Execution Pipeline): Executes sandboxed tools and updates blackboard status upon completion.

---

## Performance Benchmarks

FastAIState is profiled using **JMH** to guarantee ultra-low latency and lock-free execution under massive concurrency:

| Benchmark Operation | Score (ops/ms) | Ops per Second | Memory Allocation |
|:---|:---|:---|:---|
| **Compare-And-Swap (CAS)** | **~137,800 ops/ms** | **> 137 Million** | **0 bytes / op (Zero GC)** |
| **Blackboard State Read** | **~100,800 ops/ms** | **> 100 Million** | **0 bytes / op (Zero GC)** |
| **Blackboard State Write** | **~18,200 ops/ms** | **> 18.2 Million** | **Minimal entry overhead** |
| **Binary State Serialization** | **~83,300 ops/ms** | **> 83,300 / sec** | **High-density VarInt stream** |
| **Binary State Deserialization** | **~69,800 ops/ms** | **> 69,800 / sec** | **Zero-copy decoding** |

*Measured on Windows 11 x64, Intel Core i5 (Surface Pro 8), JDK 21.0.12.1.*

---

## API Quick Reference

| Method / Class | Return Type | Description |
|:---|:---|:---|
| `FastAIState.of(scopeId)` | `FastBlackboard` | Gets or creates a scoped shared blackboard instance. |
| `blackboard.set(key, value)` | `StateEntry` | Sets a state value and bumps the global revision version. |
| `blackboard.get(key)` | `Object` | Retrieves a state value by key. |
| `blackboard.compareAndSet(key, ver, val)` | `boolean` | Atomically updates value if expected version matches. |
| `blackboard.addListener(key, listener)` | `void` | Registers a reactive change listener for a specific key. |
| `blackboard.getDeltasSince(version)` | `List<StateEntry>` | Returns list of state entries modified after given revision. |
| `FastStateSerializer.toBinary(snapshot)` | `byte[]` | Encodes state snapshot into a compact FastBinary payload. |
| `FastStateSerializer.fromBinary(bytes)` | `FastBlackboard` | Restores blackboard state from binary bytes. |

---

## Technical Demos & Benchmarks

| Case | Java Example | Launcher | Description |
|:---|:---|:---|:---|
| **Multi-Agent Blackboard Coordination** | [Demo.java](examples/Demo/src/main/java/fastaistate/demo/Demo.java) | `run-demo.bat` | Reactive listeners, atomic CAS updates across agents, and binary state serialization. |
| **JMH Microbenchmark Suite** | [Benchmark.java](examples/Benchmark/src/main/java/fastaistate/benchmark/Benchmark.java) | `run-benchmark.bat` | Lock-free CAS throughput, concurrent blackboard reads/writes, and snapshot serialization. |

---

## Installation

### Option 1: Maven (JitPack)

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
        <version>0.1.2</version>
    </dependency>
</dependencies>
```

### Option 2: Gradle (via JitPack)

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.andrestubbe:FastAIState:0.1.2'
}
```

### Option 3: Direct Download (No Build Tool)

Download the latest JARs directly to add them to your classpath:

1. 🧠 **[FastAIState-0.1.2.jar](https://github.com/andrestubbe/FastAIState/releases/download/0.1.2/FastAIState-0.1.2.jar)** (Shared Blackboard Engine)
2. 📄 **[FastFileFormat-0.1.1.jar](https://github.com/andrestubbe/FastFileFormat/releases/download/0.1.1/FastFileFormat-0.1.1.jar)** (Dual Binary & Text File Format)
3. ⚡ **[FastBinary-0.1.1.jar](https://github.com/andrestubbe/FastBinary/releases/download/0.1.1/FastBinary-0.1.1.jar)** (VarInt & Binary Packing)
4. ⚙️ **[fastcore-0.1.0.jar](https://github.com/andrestubbe/FastCore/releases/download/0.1.0/fastcore-0.1.0.jar)** (Foundation Library)

---

## Documentation

- **[REFERENCE.md](docs/REFERENCE.md)**: Full API reference and method signatures.
- **[PHILOSOPHY.md](docs/PHILOSOPHY.md)**: Architectural design principles and lock-free goals.
- **[CHANGELOG.md](docs/CHANGELOG.md)**: Release history and version notes.
- **[ROADMAP.md](docs/ROADMAP.md)**: Future milestones and planned features.
- **[COMPILE.md](docs/COMPILE.md)**: Instructions for compiling from source.

---

## Platform Support

| Platform | Architecture | Status | Notes |
|:---|:---|:---|:---|
| Windows 10/11 | x64, ARM64 | ✅ Fully Supported | Native high-performance pure Java |
| Linux | x64, ARM64 | ✅ Fully Supported | Tested on Ubuntu / Debian / RHEL |
| macOS | Apple Silicon, x64 | ✅ Fully Supported | Tested on macOS Sonoma / Sequoia |

---

## License

MIT License — See [LICENSE](LICENSE) file for details.

---

## Related Projects

- [FastAI](https://github.com/andrestubbe/FastAI) — Unified AI client interface for Java
- [FastAIAgent](https://github.com/andrestubbe/FastAIAgent) — Autonomous agent loop, intent-graphs, and tool execution
- [FastAIBot](https://github.com/andrestubbe/FastAIBot) — Zero-bloat bot harnesses and persona runtime
- [FastAIGraph](https://github.com/andrestubbe/FastAIGraph) — In-memory knowledge graph and multi-hop relationship engine
- [FastAIHybrid](https://github.com/andrestubbe/FastAIHybrid) — Dense-sparse hybrid search fusion (BM25 + Vectors)
- [FastAIMatcher](https://github.com/andrestubbe/FastAIMatcher) — Automated SOX compliance and hybrid rule matching engine
- [FastAIMCP](https://github.com/andrestubbe/FastAIMCP) — Model Context Protocol (MCP) server & tool integration
- [FastAIMemory](https://github.com/andrestubbe/FastAIMemory) — Conversation history, sliding windows, and rolling summaries
- [FastAIMetrics](https://github.com/andrestubbe/FastAIMetrics) — Ultra-fast lock-free token, latency, cost tracking and evaluation engine
- [FastAIModel](https://github.com/andrestubbe/FastAIModel) — Native local inference runtime (GGUF/ONNX)
- [FastAIRag](https://github.com/andrestubbe/FastAIRag) — Ultra-fast document chunking and vector retrieval
- [FastAIReasoner](https://github.com/andrestubbe/FastAIReasoner) — Deterministic planning, chain-of-thought, and self-correction
- [FastAIRerank](https://github.com/andrestubbe/FastAIRerank) — Cross-encoder relevance filtering and Top-N prompt pruner
- [FastAIRuntime](https://github.com/andrestubbe/FastAIRuntime) — Sandboxed process runner and tool-calling execution pipeline
- [FastAIVectorDB](https://github.com/andrestubbe/FastAIVectorDB) — High-throughput SIMD/AVX2 vector database
- [FastAIVision](https://github.com/andrestubbe/FastAIVision) — High-speed local multimodal vision, UI-element grounding, and screen-VLM engine
- [FastCore](https://github.com/andrestubbe/FastCore) — Unified JNI loader and platform abstraction

---

**Part of the FastJava Ecosystem** — *Making the JVM faster. Small package. Maximum speed. Zero bloat. 🚀📋*