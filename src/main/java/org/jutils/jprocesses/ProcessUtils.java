package org.jutils.jprocesses;

import org.jutils.jprocesses.util.OS;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

public class ProcessUtils {

    /**
     * See {@link #getThisProcess(List)} for details.
     */
    public JProcess getThisProcess() throws IOException, InterruptedException {
        return getThisProcess(getProcesses());
    }

    /**
     * Returns the process from where this code is running from, or null when the PID of the current process couldn't be retrieved
     * or wasn't found in the provided processes list. <br>
     * Could happen when using a JVM/JDK distro that changed the value this method relies on. <br>
     */
    public JProcess getThisProcess(List<JProcess> list) {

        String name = ManagementFactory.getRuntimeMXBean().getName();
        int index = name.indexOf("@");
        if (index == -1) return null;
        String pid = name.substring(0, index);
        for (JProcess p :
                list) {
            if (p.pid.equals(pid))
                return p;
        }
        return null;
    }

    /**
     * Fetches all the currently running processes.
     */
    public List<JProcess> getProcesses() throws IOException {
        if (OS.isWindows) return fetchWindowsProcesses();
        else return fetchUnixProcesses();
    }

    private List<JProcess> fetchUnixProcesses() throws IOException {
        // ps -e -o pid,ruser,vsize,rss,%cpu,lstart,cputime,nice,ucomm,command
        // The first returned line contains enables us to determine the column widths
        Process process = new ProcessBuilder().command("ps", "-ww", "-e", "-o", "pid,ruser,vsize,rss,%cpu,lstart,nice,ppid,ucomm,command").start();
        int lUcomm, lPid, lRuser, lVsize, lRss, lcpu, lLstart, lNice, lPpid; // l = length. Length of chars each of that columns takes. Last columns' length (command) doesn't matter.
        String pid, ruser, vsz, rss, cpu, started, ni, ppid;
        pid = "PID";
        ruser = "RUSER";
        vsz = "VSZ";
        rss = "RSS";
        cpu = "%CPU";
        started = "STARTED";
        ni = "NI";
        ppid = "PPID";
        List<JProcess> list = new ArrayList<>(50);
        String line = "";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String firstLine = br.readLine(); // Contains the table header and gives us info about the column lengths
            lPid = (firstLine.indexOf(pid) + pid.length()); // No -1 bc in .substring() the last index is excluded.
            lRuser = (firstLine.indexOf(ruser) + ruser.length());
            lVsize = (firstLine.indexOf(vsz) + vsz.length());
            lRss = (firstLine.indexOf(rss) + rss.length());
            lcpu = (firstLine.indexOf(cpu) + cpu.length());
            lLstart = (firstLine.indexOf(started) + started.length());
            lNice = (firstLine.indexOf(ni) + ni.length());
            lPpid = (firstLine.indexOf(ppid) + ppid.length());
            lUcomm = firstLine.lastIndexOf("COMMAND");
            while ((line = br.readLine()) != null) {
                JProcess p = new JProcess();
                p.pid = line.substring(0, lPid).trim();
                p.username = line.substring(lPid, lRuser).trim();
                p.usedVirtualMemoryInKB = line.substring(lRuser, lVsize).trim();
                p.usedMemoryInKB = line.substring(lVsize, lRss).trim();
                p.cpuUsage = line.substring(lRss, lcpu).trim();
                p.timestampStart = line.substring(lcpu, lLstart).trim();
                p.priority = line.substring(lLstart, lNice).trim();
                p.parentPid = line.substring(lNice, lPpid).trim();
                p.name = line.substring(lPpid, lUcomm).trim();
                p.command = line.substring(lUcomm, line.length() - 1).trim();
                list.add(p);
            }
        }
        setParentChildProcesses(list);
        return list;
    }

    private List<JProcess> fetchWindowsProcesses() throws IOException {
        Process process = new ProcessBuilder().command("wmic", "process", "get", "Name,Caption,ProcessId,ParentProcessId,UserModeTime," +
                "Priority,VirtualSize,WorkingSetSize,CommandLine,CreationDate", "/VALUE").start();
        List<JProcess> list = new ArrayList<>(50);
        String line = "";
        JProcess p = new JProcess();
        int countRead = 1;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            while ((line = br.readLine()) != null) {
                if (line.startsWith("Name")) {
                    p.name = line.substring(line.indexOf("=") + 1);
                    if (countRead == 10) {
                        list.add(p);
                        p = new JProcess();
                        countRead = 0;
                    }
                    countRead++;
                } else if (line.startsWith("Caption")) {
                    p.caption = line.substring(line.indexOf("=") + 1);
                    if (countRead == 10) {
                        list.add(p);
                        p = new JProcess();
                        countRead = 0;
                    }
                    countRead++;
                } else if (line.startsWith("ProcessId")) {
                    p.pid = line.substring(line.indexOf("=") + 1);
                    if (countRead == 10) {
                        list.add(p);
                        p = new JProcess();
                        countRead = 0;
                    }
                    countRead++;
                } else if (line.startsWith("ParentProcessId")) {
                    p.parentPid = line.substring(line.indexOf("=") + 1);
                    if (countRead == 10) {
                        list.add(p);
                        p = new JProcess();
                        countRead = 0;
                    }
                    countRead++;
                } else if (line.startsWith("UserModeTime")) {
                    p.username = line.substring(line.indexOf("=") + 1);
                    if (countRead == 10) {
                        list.add(p);
                        p = new JProcess();
                        countRead = 0;
                    }
                    countRead++;
                } else if (line.startsWith("Priority")) {
                    p.priority = line.substring(line.indexOf("=") + 1);
                    if (countRead == 10) {
                        list.add(p);
                        p = new JProcess();
                        countRead = 0;
                    }
                    countRead++;
                } else if (line.startsWith("VirtualSize")) {
                    p.usedVirtualMemoryInKB = line.substring(line.indexOf("=") + 1);
                    if (countRead == 10) {
                        list.add(p);
                        p = new JProcess();
                        countRead = 0;
                    }
                    countRead++;
                } else if (line.startsWith("WorkingSetSize")) {
                    p.usedMemoryInKB = line.substring(line.indexOf("=") + 1);
                    if (countRead == 10) {
                        list.add(p);
                        p = new JProcess();
                        countRead = 0;
                    }
                    countRead++;
                } else if (line.startsWith("Commandline")) {
                    p.command = line.substring(line.indexOf("=") + 1);
                    if (countRead == 10) {
                        list.add(p);
                        p = new JProcess();
                        countRead = 0;
                    }
                    countRead++;
                } else if (line.startsWith("CreationDate")) {
                    p.timestampStart = line.substring(line.indexOf("=") + 1);
                    if (countRead == 10) {
                        list.add(p);
                        p = new JProcess();
                        countRead = 0;
                    }
                    countRead++;
                }
            }
        }
        // TODO retrieve cpuUsage and time via:
        // TODO  wmic path Win32_PerfFormattedData_PerfProc_Process get PercentProcessorTime,ElapsedTime /VALUE
        // Only problem is that the command above is slow af
        setParentChildProcesses(list);
        return list;
    }

    /**
     * The provided list contains parent and child processes. <br>
     * This method assigns parent to child processes and child to parent processes. <br>
     */
    private void setParentChildProcesses(List<JProcess> processes) {
        List<JProcess> pCopy = new ArrayList<>(processes);
        for (JProcess p0 : pCopy) {
            JProcess foundParentProcess = null;
            for (JProcess p1 : processes) { // Search for parent process
                if (!p1.pid.equals(p0.pid) && p1.pid.equals(p0.parentPid)) {
                    foundParentProcess = p1;
                    break;
                }
            }
            if (foundParentProcess == null) {
                foundParentProcess = new JProcess();
                foundParentProcess.name = "Unknown";
                foundParentProcess.pid = p0.parentPid;
                processes.add(foundParentProcess);
            }
            p0.parentProcess = foundParentProcess;
            foundParentProcess.childProcesses.add(p0);
        }
    }

    /**
     * Prints the currently running processes as a tree. <br>
     * Shows the relation between parent/child processes. <br>
     */
    public void printTree() throws IOException, InterruptedException {
        printTree(System.out, getProcesses());
    }

    public void printTree(List<JProcess> processes) {
        printTree(System.out, processes);
    }

    public void printTree(PrintStream out, List<JProcess> processes) {
        for (JProcess pEntryPoint :
                processes) {
            if (pEntryPoint.name.equals("Unknown"))
                printTree(out, pEntryPoint, 0);
        }
    }

    public void printTree(PrintStream out, JProcess p, int countSpaces) {
        String spaces = "";
        for (int i = 0; i < countSpaces; i++) {
            spaces = spaces + "-";
        }
        out.println(spaces + p.name + " " + p.pid);
        for (JProcess pChild : p.childProcesses) {
            countSpaces++;
            printTree(out, pChild, countSpaces);
        }
    }
}
