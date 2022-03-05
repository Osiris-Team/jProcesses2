package org.jutils.jprocesses;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.jutils.jprocesses.model.ProcessInfo;
import org.jutils.jprocesses.model.WindowsPriority;
import org.jutils.jprocesses.util.OSDetector;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 *
 * @author javier
 */
public class JProcessesTest {
    JProcesses jProcesses = new JProcesses();
    private OSDetector os = new OSDetector();

    public JProcessesTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getProcessList method, of class JProcesses.
     */
    @Test
    public void testGetProcessList() {
        System.out.println("===============Testing getProcessList============");
        List<ProcessInfo> processesList = new JProcesses().fastMode().getProcesses();

        assertTrue(processesList != null && processesList.size() > 0);

        for (final ProcessInfo processInfo : processesList) {
            System.out.println("Process PID: " + processInfo.pid);
            System.out.println("Process Name: " + processInfo.name);
            System.out.println("Process Time: " + processInfo.time);
            System.out.println("User: " + processInfo.user);
            System.out.println("Virtual Memory: " + processInfo.virtualMemory);
            System.out.println("Physical Memory: " + processInfo.physicalMemory);
            System.out.println("CPU usage: " + processInfo.cpuUsage);
            System.out.println("Start Time: " + processInfo.startTime);
            System.out.println("Start DateTime: "
                    + processInfo.extraData.get("start_datetime"));
            System.out.println("Priority: " + processInfo.priority);
            System.out.println("Command: " + processInfo.command);
            System.out.println("------------------");
        }
        System.out.println("===============End test getProcessList============");
    }

    /**
     * Test of getProcessList method by name, of class JProcesses.
     */
    @Test
    public void testGetProcessListByName() {
        System.out.println("===============Testing getProcessList by name============");
        //JProcesses.fastMode = true;
        String processToSearch = "java";
        if (os.isWindows()) {
            processToSearch += ".exe";
        }

        List<ProcessInfo> processesList = jProcesses.getProcesses(processToSearch);

        assertTrue(processesList != null && processesList.size() > 0);

        for (final ProcessInfo processInfo : processesList) {
            System.out.println("Process PID: " + processInfo.pid);
            System.out.println("Process Name: " + processInfo.name);
            System.out.println("Process Time: " + processInfo.time);
            System.out.println("User: " + processInfo.user);
            System.out.println("Virtual Memory: " + processInfo.virtualMemory);
            System.out.println("Physical Memory: " + processInfo.physicalMemory);
            System.out.println("CPU usage: " + processInfo.cpuUsage);
            System.out.println("Start Time: " + processInfo.startTime);
            System.out.println("Priority: " + processInfo.priority);
            System.out.println("Command: " + processInfo.command);
            System.out.println("------------------");
        }

        //Compare list with a manually retrieved list
        List<ProcessInfo> processesListFull = jProcesses.getProcesses();
        List<ProcessInfo> processesListFound = new ArrayList<ProcessInfo>();
        for (final ProcessInfo process : processesListFull) {
            if (processToSearch.equals(process.name)) {
                processesListFound.add(process);
            }
        }

        assertTrue("Manually list differs from search founded " 
                + processesListFound.size() + " instead of " + processesList.size(), 
                processesList.size() == processesListFound.size()
        );
        
        System.out.println("===============End test getProcessList by name============");
    }

    /**
     * Test of getProcessList method by name, of class JProcesses.
     */
    //@Test
    public void testKill() {
        System.out.println("===============Testing killProcess============");
        boolean success = jProcesses.killProcess(3844).isSuccess();

        System.out.println("===============End test killProcess============");
    }

    /**
     * Test of getProcessList method by name, of class JProcesses.
     */
    //@Test
    public void testChangePriority() {
        System.out.println("===============Testing changePriority============");
        boolean ok = jProcesses.changePriority(3260, WindowsPriority.HIGH.value).isSuccess();
        assertTrue(ok);

        ProcessInfo process = jProcesses.getProcess(3260);
        assertTrue(String.valueOf(13).equals(process.priority));

        ok = jProcesses.changePriority(3260, WindowsPriority.NORMAL.value).isSuccess();
        assertTrue(ok);

        process = jProcesses.getProcess(3260);
        assertTrue(String.valueOf(8).equals(process.priority));

        System.out.println("===============End test changePriority============");
    }
}
