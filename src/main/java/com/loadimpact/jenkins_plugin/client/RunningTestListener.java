package com.loadimpact.jenkins_plugin.client;

import hudson.AbortException;

import java.util.List;

/**
* Listener invoked during a running load-test.
*
* @author jens
* @date 2013-09-11, 09:24
*/
public interface RunningTestListener {
    /**
     * Called once before the test starts.
     *
     * @param id        test configuration id
     * @param name      test name
     * @param targetUrl which url to hit
     */
    void beforeTest(TestConfiguration testConfiguration);

    /**
     * Called periodically during a running load-test.
     * @param testInstance    its current status
     * @param client    LIClient to use for fetching more status data
     * @throws AbortException   thrown if the load-test job should be cancelled
     */
    void duringTest(TestInstance testInstance, LoadImpactClient client) throws AbortException;

    /**
     * Called once after the test has finished.
     * @param result    test result
     */
    void afterTest(TestInstance testInstance);
}
