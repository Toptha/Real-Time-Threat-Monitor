package cti;

import com.sun.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.util.Locale;

public final class SystemStatsReader {

    private static final OperatingSystemMXBean SUN_BEAN = getSun();
    private static final Object RAW_BEAN = ManagementFactory.getOperatingSystemMXBean();

    private SystemStatsReader() {}

    private static OperatingSystemMXBean getSun() {
        try {
            Object b = ManagementFactory.getOperatingSystemMXBean();
            if (b instanceof OperatingSystemMXBean) return (OperatingSystemMXBean) b;
        } catch (Throwable ignored) {}
        return null;
    }

    public static double getCpuPercent() {
        try {
            if (SUN_BEAN != null) {
                double v = SUN_BEAN.getSystemCpuLoad();
                if (v >= 0) return clamp(v * 100);
            }
            Method m = RAW_BEAN.getClass().getMethod("getSystemCpuLoad");
            Object o = m.invoke(RAW_BEAN);
            if (o instanceof Number) {
                double v = ((Number) o).doubleValue();
                if (v >= 0) return clamp(v * 100);
            }
        } catch (Throwable ignored) {}
        return -1;
    }

    public static double getMemoryPercent() {
        try {
            if (SUN_BEAN != null) {
                long t = SUN_BEAN.getTotalPhysicalMemorySize();
                long f = SUN_BEAN.getFreePhysicalMemorySize();
                if (t > 0) return clamp(((double)(t - f) / t) * 100);
            }
            Method mt = RAW_BEAN.getClass().getMethod("getTotalPhysicalMemorySize");
            Method mf = RAW_BEAN.getClass().getMethod("getFreePhysicalMemorySize");
            Object ot = mt.invoke(RAW_BEAN);
            Object of = mf.invoke(RAW_BEAN);
            if (ot instanceof Number && of instanceof Number) {
                long t = ((Number) ot).longValue();
                long f = ((Number) of).longValue();
                if (t > 0) return clamp(((double)(t - f) / t) * 100);
            }
        } catch (Throwable ignored) {}
        try {
            Runtime rt = Runtime.getRuntime();
            long u = rt.totalMemory() - rt.freeMemory();
            long m = rt.maxMemory();
            if (m > 0) return clamp(((double) u / m) * 100);
        } catch (Throwable ignored) {}
        return -1;
    }

    public static long getTotalPhysicalMemoryBytes() {
        try {
            if (SUN_BEAN != null) {
                long t = SUN_BEAN.getTotalPhysicalMemorySize();
                if (t > 0) return t;
            }
            Method m = RAW_BEAN.getClass().getMethod("getTotalPhysicalMemorySize");
            Object o = m.invoke(RAW_BEAN);
            if (o instanceof Number) return ((Number) o).longValue();
        } catch (Throwable ignored) {}
        return -1;
    }

    public static long getFreePhysicalMemoryBytes() {
        try {
            if (SUN_BEAN != null) {
                long f = SUN_BEAN.getFreePhysicalMemorySize();
                if (f >= 0) return f;
            }
            Method m = RAW_BEAN.getClass().getMethod("getFreePhysicalMemorySize");
            Object o = m.invoke(RAW_BEAN);
            if (o instanceof Number) return ((Number) o).longValue();
        } catch (Throwable ignored) {}
        return -1;
    }

    private static double clamp(double v) {
        if (v < 0) return 0;
        if (v > 100) return 100;
        return v;
    }

    private static String hr(long b) {
        if (b < 1024) return b + " B";
        int u = 1024;
        int e = (int) (Math.log(b) / Math.log(u));
        String p = "KMGTPE".charAt(Math.min(e - 1, 5)) + "i";
        double v = b / Math.pow(u, e);
        return String.format(Locale.ROOT, "%.2f %sB", v, p);
    }

    public static void printStats() {
        double cpu = getCpuPercent();
        double mem = getMemoryPercent();
        System.out.println("=== SYSTEM STATS ===");
        if (cpu < 0) System.out.println("CPU: unavailable");
        else System.out.printf(Locale.ROOT, "CPU Usage: %.2f%%%n", cpu);
        if (mem < 0) System.out.println("Memory: unavailable");
        else {
            System.out.printf(Locale.ROOT, "Memory Usage: %.2f%%%n", mem);
            long t = getTotalPhysicalMemoryBytes();
            long f = getFreePhysicalMemoryBytes();
            if (t > 0 && f >= 0)
                System.out.printf("Physical: total=%s free=%s%n", hr(t), hr(f));
        }
        System.out.println("====================");
    }
}
