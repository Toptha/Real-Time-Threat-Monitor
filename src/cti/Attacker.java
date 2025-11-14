package cti;

import java.time.Instant;
import java.util.Random;

/**
 * Attacker object with predefined attack types and intensity.
 * Intensity: 1..10 (1 = slight, 10 = extreme)
 */
public class Attacker {
    public enum AttackType {
        CPU_OVERLOAD,
        MEM_FILL,
        PORT_FLOOD,
        ANOMALY_SPIKE
    }

    private final String id;
    private final String ip;
    private final Instant timestamp;
    private final AttackType type;
    private final int intensity; // 1..10

    public Attacker(String id, String ip, AttackType type, int intensity) {
        this.id = id;
        this.ip = ip;
        this.type = type;
        this.intensity = Math.max(1, Math.min(10, intensity));
        this.timestamp = Instant.now();
    }

    public String getId() { return id; }
    public String getIp() { return ip; }
    public Instant getTimestamp() { return timestamp; }
    public AttackType getType() { return type; }
    public int getIntensity() { return intensity; }

    /**
     * Random attacker for demos. Random attack type + intensity.
     */
    public static Attacker random() {
        Random r = new Random();
        String ip = (r.nextInt(200) + 10) + "." + r.nextInt(256) + "." + r.nextInt(256) + "." + r.nextInt(256);
        String id = "att-" + Math.abs(r.nextInt() % 10000);
        AttackType[] vals = AttackType.values();
        AttackType t = vals[r.nextInt(vals.length)];
        int intensity = 1 + r.nextInt(10);
        return new Attacker(id, ip, t, intensity);
    }

    @Override
    public String toString() {
        return String.format("Attacker{id=%s, ip=%s, type=%s, intensity=%d, ts=%s}",
                id, ip, type.name(), intensity, timestamp.toString());
    }
}
