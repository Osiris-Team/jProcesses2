package org.jutils.jprocesses;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.jutils.jprocesses.util.OS;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 *
 * @author javier
 */
public class JProcessesTest {
    JProcesses jProcesses = new JProcesses();

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
        List<JProcess> processesList = new JProcesses().fastMode().getProcesses();

        assertTrue(processesList != null && processesList.size() > 0);

        for (final JProcess JProcess : processesList) {
            System.out.println("Process PID: " + JProcess.pid);
            System.out.println("Process Name: " + JProcess.name);
            System.out.println("Process Time: " + JProcess.time);
            System.out.println("User: " + JProcess.user);
            System.out.println("Virtual Memory: " + JProcess.kbVirtualMemory);
            System.out.println("Physical Memory: " + JProcess.kbWorkingSet);
            System.out.println("CPU usage: " + JProcess.cpuUsage);
            System.out.println("Start Time: " + JProcess.startTime);
            System.out.println("Start DateTime: "
                    + JProcess.extraData.get("start_datetime"));
            System.out.println("Priority: " + JProcess.priority);
            System.out.println("Command: " + JProcess.command);
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
        if (OS.isWindows) {
            processToSearch += ".exe";
        }

        List<JProcess> processesList = jProcesses.getProcesses(processToSearch);

        assertTrue(processesList != null && processesList.size() > 0);

        for (final JProcess JProcess : processesList) {
            System.out.println("Process PID: " + JProcess.pid);
            System.out.println("Process Name: " + JProcess.name);
            System.out.println("Process Time: " + JProcess.time);
            System.out.println("User: " + JProcess.user);
            System.out.println("Virtual Memory: " + JProcess.kbVirtualMemory);
            System.out.println("Physical Memory: " + JProcess.kbWorkingSet);
            System.out.println("CPU usage: " + JProcess.cpuUsage);
            System.out.println("Start Time: " + JProcess.startTime);
            System.out.println("Priority: " + JProcess.priority);
            System.out.println("Command: " + JProcess.command);
            System.out.println("------------------");
        }

        //Compare list with a manually retrieved list
        List<JProcess> processesListFull = jProcesses.getProcesses();
        List<JProcess> processesListFound = new ArrayList<JProcess>();
        for (final JProcess process : processesListFull) {
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
        boolean ok = jProcesses.changePriority(3260, JProcessPriority.HIGH.windowsPriority).isSuccess();
        assertTrue(ok);

        JProcess process = jProcesses.getProcess(3260);
        assertTrue(String.valueOf(13).equals(process.priority));

        ok = jProcesses.changePriority(3260, JProcessPriority.NORMAL.windowsPriority).isSuccess();
        assertTrue(ok);

        process = jProcesses.getProcess(3260);
        assertTrue(String.valueOf(8).equals(process.priority));

        System.out.println("===============End test changePriority============");
    }
}
