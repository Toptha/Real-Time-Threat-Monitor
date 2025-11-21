package cti;

import java.time.Instant;
import java.util.Random;

public class Attacker<TId> {
    public enum AttackType {
        CPU_OVERLOAD,
        MEM_FILL,
        PORT_FLOOD,
        ANOMALY_SPIKE
    }

    private final TId id;
    private final String ip;
    private final Instant timestamp;
    private final AttackType type;
    private final int intensity;

    public Attacker(TId id, String ip, AttackType type, int intensity) {
        this.id = id;
        this.ip = ip;
        this.type = type;
        this.intensity = Math.max(1, Math.min(10, intensity));
        this.timestamp = Instant.now();
    }

    public TId getId() { return id; }
    public String getIp() { return ip; }
    public Instant getTimestamp() { return timestamp; }
    public AttackType getType() { return type; }
    public int getIntensity() { return intensity; }

    /** Convenience factory for the common String id case. */
    public static Attacker<String> random() {
        Random r = new Random();
        String ip = (r.nextInt(200) + 10) + "." + r.nextInt(256) + "." + r.nextInt(256) + "." + r.nextInt(256);
        String id = "att-" + Math.abs(r.nextInt() % 10000);
        AttackType[] vals = AttackType.values();
        AttackType t = vals[r.nextInt(vals.length)];
        int intensity = 1 + r.nextInt(10);
        return new Attacker<>(id, ip, t, intensity);
    }

    @Override
    public String toString() {
        return String.format("Attacker{id=%s, ip=%s, type=%s, intensity=%d, ts=%s}",
                String.valueOf(id), ip, type.name(), intensity, timestamp.toString());
    }
}
