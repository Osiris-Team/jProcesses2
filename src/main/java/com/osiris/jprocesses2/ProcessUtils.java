package com.osiris.jprocesses2;

import com.osiris.jprocesses2.util.OS;
import com.sun.jna.Platform;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.util.*;

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
        Process process = new ProcessBuilder().command("ps", "-ww", "-e", "-o", "pid,ruser,vsize,rss,lstart,nice,ppid,ucomm,command").start();
        int lUcomm, lPid, lRuser, lVsize, lRss, lLstart, lNice, lPpid; // l = length. Length of chars each of that columns takes. Last columns' length (command) doesn't matter.
        String pid, ruser, vsz, rss, started, ni, ppid;
        pid = "PID";
        ruser = "RUSER";
        vsz = "VSZ";
        rss = "RSS";
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
                p.timestampStart = line.substring(lRss, lLstart).trim();
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

    /**
     * REPL for fetching and displaying process information.
     */
    public void initCMDTool() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    PrintStream out = System.out;
                    Scanner scanner = new Scanner(System.in);
                    out.println("Initialised jProcess2 command line tool. To exit it enter 'exit', for a list of commands enter 'help'.");
                    out.println("Fetching processes...");
                    ProcessUtils processUtils = new ProcessUtils();
                    List<JProcess> processes = processUtils.getProcesses();
                    out.println("Done!");
                    boolean exit = false;
                    String command = null;
                    while (!exit) {
                        command = scanner.nextLine();
                        try {
                            if (command.equals("exit")) {
                                exit = true;
                            } else if (command.equals("help") || command.equals("h")) {
                                out.println("Available commands:");
                                out.println("help | Prints all available commands. (Shortcut: h)");
                                out.println("exit | Exit the jProcess2 command line tool. (e)");
                                out.println("fetch | Fetches all currently running processes details. (f)");
                                out.println("print | Prints a list with all processes details. (p)");
                                out.println("print tree | Prints a list with all processes details but also their parent/child relations. (pt)");
                            } else if (command.equals("fetch") || command.equals("f")) {
                                out.println("Fetching processes...");
                                processes = processUtils.getProcesses();
                                out.println("Done!");
                            } else if (command.equals("print") || command.equals("p")) {
                                out.println("Printing all processes...");
                                for (JProcess p : processes) {
                                    out.println(p.toPrintString());
                                }
                                out.println("Done!");
                            } else if (command.equals("print tree") || command.equals("pt")) {
                                out.println("Printing all processes tree...");
                                processUtils.printTree(processes);
                                out.println("Done!");
                            } else {
                                out.println("Unknown command. Enter 'help' or 'h' for a list of all commands.");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     *
     * @param exeFile The executable program. Example: "C:\Windows\notepad.exe". Must not be .exe, can be anything that is executable.
     * @param workingDir The working directory of the new process. If null current working directory is used.
     * @param enviornment
     * @param input
     * @param errorInput
     * @param output
     * @return
     * @throws Exception when the process could not be started.
     * @throws NullPointerException when a required parameter is null.
     */
    public JProcess startProcess(File exeFile, String args, File workingDir, Map<String, String> enviornment,
                                 InputStream input, InputStream errorInput, OutputStream output) throws Exception {
        Objects.requireNonNull(exeFile);
        String command = "\""+exeFile.getPath()+"\"";
        if(args!=null) command = command +" "+args;

        if(Platform.isWindows()){
            //TODO boolean success = Kernel32.INSTANCE.CreateProcess(command, null, );
            //if (!success) throw new Exception("Failed to start process with command '"+exeFile+"'!");
        } else{
            // TODO
        }
        return null;// TODO
    }
}
