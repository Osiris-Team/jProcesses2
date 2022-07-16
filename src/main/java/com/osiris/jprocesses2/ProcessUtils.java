package com.osiris.jprocesses2;

import com.osiris.jprocesses2.util.OS;
import com.sun.jna.Platform;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.util.*;

public class ProcessUtils {

    /**
     * See {@link #getThis(List)} for details.
     */
    public JProcess getThis() throws IOException, InterruptedException {
        return getThis(getProcesses());
    }

    /**
     * Returns the process from where this code is running from, or null when the PID of the current process couldn't be retrieved
     * or wasn't found in the provided processes list. <br>
     * Could happen when using a JVM/JDK distro that changed the value this method relies on. <br>
     */
    public JProcess getThis(List<JProcess> list) {

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

    public JProcess getByPID(String pid) throws IOException {
        for (JProcess process : getProcesses()) {
            if(Objects.equals(process.pid, pid)) return process;
        }
        return null;
    }

    public JProcess getByCommand(String command) throws IOException {
        for (JProcess process : getProcesses()) {
            if(Objects.equals(process.command, command)) return process;
        }
        return null;
    }

    public JProcess getByName(String name) throws IOException {
        for (JProcess process : getProcesses()) {
            if(Objects.equals(process.name, name)) return process;
        }
        return null;
    }

    public List<JProcess> getForUser(String username) throws IOException {
        List<JProcess> list = new ArrayList<>();
        for (JProcess process : getProcesses()) {
            if(Objects.equals(process.username, username)) list.add(process);
        }
        return list;
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
        List<JProcess> processesList = new ArrayList<>(50);
        String line = "";
        JProcess p = new JProcess();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            while ((line = br.readLine()) != null) {
                p = new JProcess();
                List<String> list = splitBySpaces(line);
                p.pid = list.get(0);
                p.username = list.get(1);
                p.usedVirtualMemoryInKB = list.get(2);
                p.usedMemoryInKB = list.get(3);
                p.timestampStart = list.get(4) +" "+ list.get(5)+" "+list.get(6)+" "+list.get(7)+" "+list.get(8);
                p.priority = list.get(9);
                p.parentPid = list.get(10);
                p.name = list.get(11);
                for (int i = 12; i < list.size(); i++) {
                    p.command += list.get(i)+" ";
                }
                p.command.trim();
                processesList.add(p);
            }
        } catch (Exception e){
            throw new RuntimeException("Failed at process: \n"+p.toString()+"\n raw-line:\n"+line+"\n", e);
        }
        setParentChildProcesses(processesList);
        return processesList;
    }

    /**
     * Additionally removes empty strings.
     */
    private List<String> splitBySpaces(String s){
        List<String> list = new ArrayList<>();
        for (String s1 : s.split(" ")) {
            if(!s1.isEmpty()) list.add(s1);
        }
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
    public void initCMDTool(final String _command) {
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
                    while (!exit) {
                        String command = _command;
                        if(command != null){
                            exit = true;
                        } else
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
