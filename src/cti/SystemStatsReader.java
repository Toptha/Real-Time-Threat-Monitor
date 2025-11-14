package cti;

import com.sun.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;

public class SystemStatsReader {

    private static final OperatingSystemMXBean osBean = getOsBean();

    private static OperatingSystemMXBean getOsBean() {
        try {
            return (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        } catch (Throwable t) {
            return null;
        }
    }

    public static double getCpuPercent() {
        if (osBean == null) return -1;
        double v = osBean.getSystemCpuLoad(); 
        return v < 0 ? -1 : v * 100.0;
    }

    public static double getMemoryPercent() {
        if (osBean == null) return -1;
        long total = osBean.getTotalMemorySize();
        long free = osBean.getFreeMemorySize();
        if (total <= 0) return -1;
        return ((total - free) * 100.0) / total;
    }

    public static void printStats() {
        System.out.println("=== SYSTEM STATS ===");
        double cpu = getCpuPercent();
        if (cpu < 0) System.out.println("CPU: unavailable on this JVM/platform");
        else System.out.printf("CPU Usage: %.2f%%%n", cpu);

        double mem = getMemoryPercent();
        if (mem < 0) System.out.println("Memory: unavailable on this JVM/platform");
        else System.out.printf("Memory Usage: %.2f%%%n", mem);

        System.out.println("====================");
    }
}
