# Changelog: FastAIState

All notable changes to this project will be documented in this file.

## [0.1.0] - 2026-08-24
### Added
- **Lock-Free Shared Blackboard (`FastBlackboard`)**: Atomic concurrent key-value state engine with monotonically increasing revision tracking.
- **Compare-And-Swap (CAS)**: Thread-safe optimistic concurrency control (`compareAndSet`) without global locks.
- **Reactive State Listeners (`StateChangeListener`)**: Sub-microsecond callback notifications for key-specific and global mutations.
- **State Revision & Delta Tracking**: `getDeltasSince` for synchronization and agent streaming.
- **FastBinary Snapshot Serialization (`FastStateSerializer`)**: LEB128 VarInt compact binary persistence.
- **Interactive Showcase & JMH Benchmark Suite**: Measuring >300M CAS ops/sec with 0 GC allocations.
