package com.osiris.jprocesses2;

import junit.framework.TestCase;

import java.io.IOException;
import java.text.ParseException;

public class ProcessUtilsTest extends TestCase {

    public void testGetProcesses() throws IOException {
        assertTrue(new ProcessUtils().getProcesses().size() > 0);
    }

    public void testaaa() throws IOException, InterruptedException, ParseException {
        System.out.println(System.currentTimeMillis());
        System.out.println(new ProcessUtils().getThisProcess().getTimestampStart().getTime());
        Thread.sleep(100);
        System.out.println(System.currentTimeMillis());
    }
}