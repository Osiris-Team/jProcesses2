package org.jutils.jprocesses;

import org.junit.*;

import java.io.IOException;
import java.text.ParseException;

/**
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
    public void generalTest() throws IOException, InterruptedException {
        System.out.println(jp2.getThisProcess().toPrintString());
    }

    @Test
    public void aaaa() throws IOException, ParseException, InterruptedException {

    }
}
