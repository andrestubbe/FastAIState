package fastaistate;

import fastfileformat.BinaryHeader;
import fastfileformat.BinaryReader;
import fastfileformat.BinaryWriter;
import fastfileformat.FastFileFormat;

import java.util.Map;

/**
 * High-speed dual-format serialization for persistent FastAIState snapshots using FastFileFormat.
 */
public final class FastStateSerializer {
    /**
     * Standard FastJava AI State payload type identifier (0x0002).
     */
    public static final short PAYLOAD_TYPE_STATE = 0x0002;

    private FastStateSerializer() {}

    /**
     * Serializes a snapshot to raw binary bytes using FastFileFormat.
     *
     * @param snapshot The state snapshot.
     * @return Compact binary byte array with 12-byte FastFileFormat header.
     */
    public static byte[] toBinary(StateSnapshot snapshot) {
        BinaryWriter payloadWriter = FastFileFormat.binaryWriter(256);

        payloadWriter.writeString(snapshot.scopeId());
        payloadWriter.writeLong(snapshot.snapshotVersion());

        Map<String, StateEntry> entries = snapshot.entries();
        payloadWriter.writeVarInt(entries.size());

        for (StateEntry entry : entries.values()) {
            payloadWriter.writeString(entry.key());
            payloadWriter.writeLong(entry.version());
            payloadWriter.writeLong(entry.timestamp());
            String strVal = entry.value() != null ? entry.value().toString() : "";
            payloadWriter.writeString(strVal);
        }

        byte[] payload = payloadWriter.toByteArray();

        BinaryHeader header = new BinaryHeader(
                FastFileFormat.DEFAULT_MAGIC,
                FastFileFormat.DEFAULT_VERSION,
                PAYLOAD_TYPE_STATE,
                payload.length
        );

        BinaryWriter finalWriter = FastFileFormat.binaryWriter(12 + payload.length);
        finalWriter.writeHeader(
                FastFileFormat.DEFAULT_MAGIC,
                FastFileFormat.DEFAULT_VERSION,
                PAYLOAD_TYPE_STATE,
                payload.length
        );
        finalWriter.writeBytes(payload);
        return finalWriter.toByteArray();
    }

    /**
     * Deserializes binary state into a FastBlackboard using FastFileFormat.
     *
     * @param bytes Binary payload with FastFileFormat header.
     * @return Restored FastBlackboard instance.
     */
    public static FastBlackboard fromBinary(byte[] bytes) {
        BinaryReader reader = FastFileFormat.binaryReader(bytes);
        BinaryHeader header = reader.readHeader();

        if (header.getMagic() != FastFileFormat.DEFAULT_MAGIC) {
            throw new IllegalArgumentException("Invalid FastFileFormat magic header: " + Integer.toHexString(header.getMagic()));
        }
        if (header.getPayloadType() != PAYLOAD_TYPE_STATE) {
            throw new IllegalArgumentException("Unexpected payload type for FastAIState: " + header.getPayloadType());
        }

        String scope = reader.readString();
        long snapshotVer = reader.readLong();
        int entryCount = reader.readVarInt();

        FastBlackboard blackboard = new FastBlackboard(scope);
        for (int i = 0; i < entryCount; i++) {
            String key = reader.readString();
            long entryVer = reader.readLong();
            long entryTime = reader.readLong();
            String valStr = reader.readString();
            blackboard.set(key, valStr);
        }
        return blackboard;
    }
}
