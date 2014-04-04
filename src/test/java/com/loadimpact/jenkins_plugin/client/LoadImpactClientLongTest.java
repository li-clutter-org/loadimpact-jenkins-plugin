package com.loadimpact.jenkins_plugin.client;

import hudson.AbortException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * 'Long running' integration tests of the LoadImpactClient REST invocations.
 *
 * @author jens
 * @date 2013-09-08, 17:59
 */
public class LoadImpactClientLongTest {
    private static final String API_KEY = "4abe6379050c34ed3f996785edd29e7943c56588fe8b755baba20fa4718758f1";
    private static final int TEST_CFG_ID = 1436348;
    private static final String TEST_CFG_NAME = "RMSE";
    private static final String TARGET_URL = "http://www.ribomation.se/";
    private LoadImpactClient target;

    @Before
    public void setUp() throws Exception {
        target = new LoadImpactClient(API_KEY, true);
    }
    
    @Test 
    @Ignore("Need to run this loooong test manually")
    public void runningOneTestShouldPass() throws Exception {
        final int id = TEST_CFG_ID;
        TestInstance result = target.runTest(id, 2, new RunningTestListener() {
            public void beforeTest(TestConfiguration t) {
                assertThat(t.name, is(TEST_CFG_NAME));
            }

            public void duringTest(TestInstance t, LoadImpactClient client) throws AbortException {
                assertThat(t.status.isProcessing(), is(true));
            }

//            public boolean updateStatus(Status status) {
//                assertThat(status.isProcessing(), is(true));
//                return true;
//            }

//            public void duringTest(Status status, LoadImpactClient client) throws AbortException {
//                
//            }

//            public boolean updateProgress(List<TestResult> progress) {
//                if (progress.isEmpty()) return true;
//
//                double percentage = progress.get(progress.size() - 1).value_decimal;
//                System.out.printf("Test progress: %s%n", Util.percentageBar(percentage));
//                return true;
//            }

            public void afterTest(TestInstance result) {
                assertThat(result.status.isCompleted(), is(true));
            }
        });

        assertThat(result, is(notNullValue()));
        assertThat(result.status.isCompleted(), is(true));
        assertThat(result.targetUrl, is(TARGET_URL));
    }
    
    
}
