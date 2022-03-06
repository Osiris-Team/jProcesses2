package org.jutils.jprocesses;

import org.junit.*;
import org.jutils.jprocesses.util.OS;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 *
 * @author javier
 */
public class ProcessUtilsTest {
    ProcessUtils jp2 = new ProcessUtils();

    public ProcessUtilsTest() {
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
