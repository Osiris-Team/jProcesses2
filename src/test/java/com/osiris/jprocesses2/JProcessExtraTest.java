package com.osiris.jprocesses2;

import junit.framework.TestCase;

import java.io.IOException;

public class JProcessExtraTest extends TestCase {
    ProcessUtils processUtils = new ProcessUtils();

    public void testExtraInfo() throws IOException, InterruptedException {
        assertNotNull(processUtils.getThisProcess().getExtraInfo().threadCount);
        assertNotNull(processUtils.getThisProcess().getExtraInfo().minorPageFaults);
    }

}