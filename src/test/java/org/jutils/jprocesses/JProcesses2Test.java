package org.jutils.jprocesses;

import org.junit.*;
import org.jutils.jprocesses.model.ProcessInfo;
import org.jutils.jprocesses.util.OSDetector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    public void testParentChildStuff() throws IOException, InterruptedException {
        System.out.println(jp2.getThisProcess().toPrintString());
    }



}
