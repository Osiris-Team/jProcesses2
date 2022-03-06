package org.jutils.jprocesses;




import org.jutils.jprocesses.model.ProcessInfo;
import org.jutils.jprocesses.util.OSDetector;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class JProcesses2 {
    public OSDetector os = new OSDetector();

    /**
     * See {@link #getThisProcess(List)} for details.
     */
    public ProcessInfo getThisProcess() throws IOException, InterruptedException {
        return getThisProcess(getProcesses());
    }

    /**
     * Returns the process from where this code is running from, or null when the PID of the current process couldn't be retrieved
     * or wasn't found in the provided processes list. <br>
     * Could happen when using a JVM/JDK distro that changed the value this method relies on. <br>
     */
    public ProcessInfo getThisProcess(List<ProcessInfo> list){
        String name = ManagementFactory.getRuntimeMXBean().getName();
        int index = name.indexOf("@");
        if(index == -1) return null;
        String pid = name.substring(0, index);
        for (ProcessInfo p :
                list) {
            if(p.pid.equals(pid))
                return p;
        }
        return null;
    }

    /**
     * Fetches the currently running processes.
     */
    public List<ProcessInfo> getProcesses() throws IOException, InterruptedException {
        if(os.isWindows()) return fetchWindowsProcesses();
        else return fetchUnixProcesses();
    }

    private List<ProcessInfo> fetchUnixProcesses() {
        return null; // TODO
    }

    private List<ProcessInfo> fetchWindowsProcesses() throws IOException {
        Process process = new ProcessBuilder().command("wmic", "process", "get", "Name,Caption,ProcessId,ParentProcessId,UserModeTime," +
                "Priority,VirtualSize,WorkingSetSize,CommandLine,CreationDate", "/VALUE").start();
        List<ProcessInfo> list = new ArrayList<>(50);
        String line = "";
        ProcessInfo p = new ProcessInfo();
        int countRead = 1;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            while((line = br.readLine()) != null){
                if(line.startsWith("Name")) {
                    p.name = line.substring(line.indexOf("=")+1);
                    if(countRead == 10) {
                        list.add(p);
                        p = new ProcessInfo();
                        countRead = 0;
                    }
                    countRead++;
                } else if(line.startsWith("Caption")) {
                    p.caption = line.substring(line.indexOf("=")+1);
                    if(countRead == 10) {
                        list.add(p);
                        p = new ProcessInfo();
                        countRead = 0;
                    }
                    countRead++;
                } else if(line.startsWith("ProcessId")) {
                    p.pid = line.substring(line.indexOf("=")+1);
                    if(countRead == 10) {
                        list.add(p);
                        p = new ProcessInfo();
                        countRead = 0;
                    }
                    countRead++;
                } else if(line.startsWith("ParentProcessId")) {
                    p.parentPid = line.substring(line.indexOf("=")+1);
                    if(countRead == 10) {
                        list.add(p);
                        p = new ProcessInfo();
                        countRead = 0;
                    }
                    countRead++;
                } else if(line.startsWith("UserModeTime")) {
                    p.user = line.substring(line.indexOf("=")+1);
                    if(countRead == 10) {
                        list.add(p);
                        p = new ProcessInfo();
                        countRead = 0;
                    }
                    countRead++;
                } else if(line.startsWith("Priority")) {
                    p.priority = line.substring(line.indexOf("=")+1);
                    if(countRead == 10) {
                        list.add(p);
                        p = new ProcessInfo();
                        countRead = 0;
                    }
                    countRead++;
                } else if(line.startsWith("VirtualSize")) {
                    p.kbVirtualMemory = line.substring(line.indexOf("=")+1);
                    if(countRead == 10) {
                        list.add(p);
                        p = new ProcessInfo();
                        countRead = 0;
                    }
                    countRead++;
                } else if(line.startsWith("WorkingSetSize")) {
                    p.kbWorkingSet = line.substring(line.indexOf("=")+1);
                    if(countRead == 10) {
                        list.add(p);
                        p = new ProcessInfo();
                        countRead = 0;
                    }
                    countRead++;
                } else if(line.startsWith("Commandline")) {
                    p.command = line.substring(line.indexOf("=")+1);
                    if(countRead == 10) {
                        list.add(p);
                        p = new ProcessInfo();
                        countRead = 0;
                    }
                    countRead++;
                } else if(line.startsWith("CreationDate")) {
                    p.startTime = line.substring(line.indexOf("=")+1);
                    if(countRead == 10) {
                        list.add(p);
                        p = new ProcessInfo();
                        countRead = 0;
                    }
                    countRead++;
                }
            }
        }
        // TODO
        setParentChildProcesses(list);
        return list;
    }

    /**
     * The provided list contains parent and child processes. <br>
     * This method assigns parent to child processes and child to parent processes. <br>
     */
    private void setParentChildProcesses(List<ProcessInfo> processes){
        List<ProcessInfo> pCopy = new ArrayList<>(processes);
        for (ProcessInfo p0 : pCopy) {
            ProcessInfo foundParentProcess = null;
            for (ProcessInfo p1 : processes) { // Search for parent process
                if(!p1.pid.equals(p0.pid) && p1.pid.equals(p0.parentPid)){
                    foundParentProcess = p1;
                    break;
                }
            }
            if(foundParentProcess==null){
                foundParentProcess = new ProcessInfo();
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

    public void printTree(List<ProcessInfo> processes){
        printTree(System.out, processes);
    }

    public void printTree(PrintStream out, List<ProcessInfo> processes){
        for (ProcessInfo pEntryPoint :
                processes) {
            if(pEntryPoint.name.equals("Unknown"))
                printTree(out, pEntryPoint, 0);
        }
    }

    public void printTree(PrintStream out, ProcessInfo p, int countSpaces){
        String spaces = "";
        for (int i = 0; i < countSpaces; i++) {
            spaces = spaces + "-";
        }
        out.println(spaces+p.name+" "+p.pid);
        for (ProcessInfo pChild : p.childProcesses) {
            countSpaces++;
            printTree(out, pChild, countSpaces);
        }
    }
}
