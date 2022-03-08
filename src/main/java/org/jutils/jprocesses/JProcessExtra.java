package org.jutils.jprocesses;

/**
 * Extra, more detailed information about the {@link JProcess}.
 */
public class JProcessExtra {
    /**
     * Returns true if the process is currently running.
     */
    public boolean isAlive;
    //public String cpuUsage; // TODO not available on Windows via wmic command, thus removed. Find performant alternative.
    /**
     * The amount of threads created by this process.
     */
    public String threadCount;
    //public String majorPageFaults; // TODO not available on Windows via wmic command, thus removed. Find performant alternative.
    /**
     * The amount of minor page faults for this process.
     */
    public String minorPageFaults;

    public JProcessExtra() {
    }

    public JProcessExtra(boolean isAlive, String threadCount, String minorPageFaults) {
        this.isAlive = isAlive;
        this.threadCount = threadCount;
        this.minorPageFaults = minorPageFaults;
    }
}
