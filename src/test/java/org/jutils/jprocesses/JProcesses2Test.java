package org.jutils.jprocesses;

import org.junit.*;
import org.jutils.jprocesses.model.ProcessInfo;
import org.jutils.jprocesses.util.OSDetector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 *
 * @author javier
 */
public class JProcesses2Test {
    JProcesses2 jp2 = new JProcesses2();
    private OSDetector os = new OSDetector();

    public JProcesses2Test() {
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


    @Test
    public void testGetProcessList() throws IOException, InterruptedException {
        long ms = System.currentTimeMillis();
        Process process = new ProcessBuilder().command("wmic", "process", "get", "Name,Caption,ProcessId,ParentProcessId,UserModeTime," +
                "Priority,VirtualSize,WorkingSetSize,CommandLine,CreationDate", "/VALUE").start();
        String name = "Name";
        List<ProcessInfo> list = new ArrayList<>(50);
        String line = "";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            while((line = br.readLine()) != null){
                if(line.startsWith(name)) {
                    ProcessInfo pInfo = new ProcessInfo();
                    list.add(pInfo);
                    pInfo.name = line.substring(line.indexOf("=")+1);
                    line = br.readLine();
                    line = br.readLine();
                    pInfo.caption = line.substring(line.indexOf("=")+1);
                    line = br.readLine();
                    line = br.readLine();
                    pInfo.pid = line.substring(line.indexOf("=")+1);
                    line = br.readLine();
                    line = br.readLine();
                    pInfo.parentPid = line.substring(line.indexOf("=")+1);
                    line = br.readLine();
                    line = br.readLine();
                    pInfo.user = line.substring(line.indexOf("=")+1);
                    line = br.readLine();
                    line = br.readLine();
                    pInfo.priority = line.substring(line.indexOf("=")+1);
                    line = br.readLine();
                    line = br.readLine();
                    pInfo.kbVirtualMemory = line.substring(line.indexOf("=")+1);
                    line = br.readLine();
                    line = br.readLine();
                    pInfo.kbWorkingSet = line.substring(line.indexOf("=")+1);
                    line = br.readLine();
                    line = br.readLine();
                    pInfo.command = line.substring(line.indexOf("=")+1);
                    line = br.readLine();
                    line = br.readLine();
                    pInfo.startTime = line.substring(line.indexOf("=")+1);
                }
            }
        }

        System.out.println("Fetching data took: "+(System.currentTimeMillis() - ms)+"ms");
        ms = System.currentTimeMillis();
        
        List<ProcessInfo> pCopy = new ArrayList<>(list);
        for (ProcessInfo parentP : pCopy) { // Set parent and child processes
            System.out.println(parentP.name+" "+parentP.pid+" "+parentP.parentPid);
            for (ProcessInfo childP : list) {
                if(!parentP.pid.equals(childP.pid) && parentP.pid.equals(childP.parentPid)){
                    childP.parentProcess = parentP;
                    parentP.childProcesses.add(childP);
                }
            }
        }

        System.out.println("Setting parent/child took: "+(System.currentTimeMillis() - ms)+"ms");
        ms = System.currentTimeMillis();

        ProcessInfo firstP = list.get(0);
        System.out.println(firstP.name);
        System.out.println(firstP.childProcesses.toString());
        printTree(firstP.childProcesses, 0);
    }

    private void printTree(List<ProcessInfo> list, int countSpaces){
        if(list.isEmpty()){

        } else{
            for (ProcessInfo p : list) {
                String spaces = "";
                for (int i = 0; i < countSpaces; i++) {
                    spaces = spaces + " ";
                }
                System.out.println(spaces+p.name);
                countSpaces++;
                printTree(p.childProcesses, countSpaces);
            }
        }
    }

}
