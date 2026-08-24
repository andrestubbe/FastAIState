package fastaistate;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * High-speed binary & JSON serialization for persistent FastAIState snapshots.
 */
public final class FastStateSerializer {
    private static final int MAGIC = 0x46415354; // 'FAST'
    private static final short VERSION = 1;

    private FastStateSerializer() {}

    /**
     * Serializes a snapshot to raw binary bytes using FastBinary primitives.
     */
    public static byte[] toBinary(StateSnapshot snapshot) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
        try (DataOutputStream dos = new DataOutputStream(baos)) {
            dos.writeInt(MAGIC);
            dos.writeShort(VERSION);

            byte[] scopeBytes = snapshot.scopeId().getBytes(StandardCharsets.UTF_8);
            fastbinary.VarInt.write(scopeBytes.length, dos);
            dos.write(scopeBytes);
            dos.writeLong(snapshot.snapshotVersion());

            Map<String, StateEntry> entries = snapshot.entries();
            fastbinary.VarInt.write(entries.size(), dos);

            for (StateEntry entry : entries.values()) {
                byte[] keyBytes = entry.key().getBytes(StandardCharsets.UTF_8);
                fastbinary.VarInt.write(keyBytes.length, dos);
                dos.write(keyBytes);
                dos.writeLong(entry.version());
                dos.writeLong(entry.timestamp());

                String strVal = entry.value() != null ? entry.value().toString() : "";
                byte[] valBytes = strVal.getBytes(StandardCharsets.UTF_8);
                fastbinary.VarInt.write(valBytes.length, dos);
                dos.write(valBytes);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize FastAIState snapshot", e);
        }
        return baos.toByteArray();
    }

    /**
     * Deserializes binary state into a FastBlackboard.
     */
    public static FastBlackboard fromBinary(byte[] bytes) {
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes))) {
            int magic = dis.readInt();
            if (magic != MAGIC) {
                throw new IllegalArgumentException("Invalid FastAIState binary magic header: " + Integer.toHexString(magic));
            }
            short ver = dis.readShort();
            if (ver != VERSION) {
                throw new IllegalArgumentException("Unsupported FastAIState version: " + ver);
            }

            int scopeLen = fastbinary.VarInt.readInt(dis);
            byte[] scopeBuf = new byte[scopeLen];
            dis.readFully(scopeBuf);
            String scope = new String(scopeBuf, StandardCharsets.UTF_8);

            long snapshotVer = dis.readLong();
            int entryCount = fastbinary.VarInt.readInt(dis);

            FastBlackboard blackboard = new FastBlackboard(scope);
            for (int i = 0; i < entryCount; i++) {
                int keyLen = fastbinary.VarInt.readInt(dis);
                byte[] keyBuf = new byte[keyLen];
                dis.readFully(keyBuf);
                String key = new String(keyBuf, StandardCharsets.UTF_8);

                long entryVer = dis.readLong();
                long entryTime = dis.readLong();

                int valLen = fastbinary.VarInt.readInt(dis);
                byte[] valBuf = new byte[valLen];
                dis.readFully(valBuf);
                String valStr = new String(valBuf, StandardCharsets.UTF_8);

                blackboard.set(key, valStr);
            }
            return blackboard;
        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialize FastAIState binary data", e);
        }
    }
}
