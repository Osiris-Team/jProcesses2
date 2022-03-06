package org.jutils.jprocesses;




import org.jutils.jprocesses.util.OS;

import java.io.*;
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
    public JProcess getThisProcess(List<JProcess> list){

        String name = ManagementFactory.getRuntimeMXBean().getName();
        int index = name.indexOf("@");
        if(index == -1) return null;
        String pid = name.substring(0, index);
        for (JProcess p :
                list) {
            if(p.pid.equals(pid))
                return p;
        }
        return null;
    }

    /**
     * Fetches the currently running processes.
     */
    public List<JProcess> getProcesses() throws IOException, InterruptedException {
        if(OS.isWindows) return fetchWindowsProcesses();
        else return fetchUnixProcesses();
    }

    private List<JProcess> fetchUnixProcesses() {
        return null; // TODO
    }

    private List<JProcess> fetchWindowsProcesses() throws IOException {
        Process process = new ProcessBuilder().command("wmic", "process", "get", "Name,Caption,ProcessId,ParentProcessId,UserModeTime," +
                "Priority,VirtualSize,WorkingSetSize,CommandLine,CreationDate", "/VALUE").start();
        List<JProcess> list = new ArrayList<>(50);
        String line = "";
        JProcess p = new JProcess();
        int countRead = 1;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            while((line = br.readLine()) != null){
                if(line.startsWith("Name")) {
                    p.name = line.substring(line.indexOf("=")+1);
                    if(countRead == 10) {
                        list.add(p);
                        p = new JProcess();
                        countRead = 0;
                    }
                    countRead++;
                } else if(line.startsWith("Caption")) {
                    p.caption = line.substring(line.indexOf("=")+1);
                    if(countRead == 10) {
                        list.add(p);
                        p = new JProcess();
                        countRead = 0;
                    }
                    countRead++;
                } else if(line.startsWith("ProcessId")) {
                    p.pid = line.substring(line.indexOf("=")+1);
                    if(countRead == 10) {
                        list.add(p);
                        p = new JProcess();
                        countRead = 0;
                    }
                    countRead++;
                } else if(line.startsWith("ParentProcessId")) {
                    p.parentPid = line.substring(line.indexOf("=")+1);
                    if(countRead == 10) {
                        list.add(p);
                        p = new JProcess();
                        countRead = 0;
                    }
                    countRead++;
                } else if(line.startsWith("UserModeTime")) {
                    p.user = line.substring(line.indexOf("=")+1);
                    if(countRead == 10) {
                        list.add(p);
                        p = new JProcess();
                        countRead = 0;
                    }
                    countRead++;
                } else if(line.startsWith("Priority")) {
                    p.priority = line.substring(line.indexOf("=")+1);
                    if(countRead == 10) {
                        list.add(p);
                        p = new JProcess();
                        countRead = 0;
                    }
                    countRead++;
                } else if(line.startsWith("VirtualSize")) {
                    p.kbVirtualMemory = line.substring(line.indexOf("=")+1);
                    if(countRead == 10) {
                        list.add(p);
                        p = new JProcess();
                        countRead = 0;
                    }
                    countRead++;
                } else if(line.startsWith("WorkingSetSize")) {
                    p.kbWorkingSet = line.substring(line.indexOf("=")+1);
                    if(countRead == 10) {
                        list.add(p);
                        p = new JProcess();
                        countRead = 0;
                    }
                    countRead++;
                } else if(line.startsWith("Commandline")) {
                    p.command = line.substring(line.indexOf("=")+1);
                    if(countRead == 10) {
                        list.add(p);
                        p = new JProcess();
                        countRead = 0;
                    }
                    countRead++;
                } else if(line.startsWith("CreationDate")) {
                    p.startTime = line.substring(line.indexOf("=")+1);
                    if(countRead == 10) {
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
    private void setParentChildProcesses(List<JProcess> processes){
        List<JProcess> pCopy = new ArrayList<>(processes);
        for (JProcess p0 : pCopy) {
            JProcess foundParentProcess = null;
            for (JProcess p1 : processes) { // Search for parent process
                if(!p1.pid.equals(p0.pid) && p1.pid.equals(p0.parentPid)){
                    foundParentProcess = p1;
                    break;
                }
            }
            if(foundParentProcess==null){
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

    public void printTree(List<JProcess> processes){
        printTree(System.out, processes);
    }

    public void printTree(PrintStream out, List<JProcess> processes){
        for (JProcess pEntryPoint :
                processes) {
            if(pEntryPoint.name.equals("Unknown"))
                printTree(out, pEntryPoint, 0);
        }
    }

    public void printTree(PrintStream out, JProcess p, int countSpaces){
        String spaces = "";
        for (int i = 0; i < countSpaces; i++) {
            spaces = spaces + "-";
        }
        out.println(spaces+p.name+" "+p.pid);
        for (JProcess pChild : p.childProcesses) {
            countSpaces++;
            printTree(out, pChild, countSpaces);
        }
    }
}
