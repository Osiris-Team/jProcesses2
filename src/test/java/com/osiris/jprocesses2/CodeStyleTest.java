package com.osiris.jprocesses2;

import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

public class CodeStyleTest {

    @Test
    public void test() throws IOException, ParseException {
        ProcessUtils processUtils = new ProcessUtils();
        List<JProcess> list = processUtils.getProcesses();
        for (JProcess process : list) {
            // Collect process details:
            System.out.println(process.name);
            System.out.println(process.pid);
            System.out.println(process.command);
            System.out.println(process.usedMemoryInKB);
            // etc...

            // Parent/Child processes:
            JProcess parent = process.parentProcess;
            List<JProcess> childProcesses = process.childProcesses;

            // Cross-platform priorities and timestamps:
            JProcessPriority priority = process.getPriority();
            Date timestampStart = process.getTimestampStart();

            // Interact with the process:
            process.changePriority(JProcessPriority.REAL_TIME);
            process.stop();
            process.kill();
        }

        // Easy access to the running JVM process:
        JProcess thisProcess = processUtils.getThis(list);

        // Print parent/child processes in a tree:
        processUtils.printTree(list);

        // Easily start a new child process
        //TODO JProcess childProcess = processUtils.startProcess();
    }
}
