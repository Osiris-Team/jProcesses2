package org.jutils.jprocesses;

import junit.framework.TestCase;

import java.io.IOException;

public class ProcessUtilsTest extends TestCase {

    public void testGetProcesses() throws IOException {
        assertTrue(new ProcessUtils().getProcesses().size() > 0);
    }
}