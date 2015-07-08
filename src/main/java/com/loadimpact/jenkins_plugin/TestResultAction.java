package com.loadimpact.jenkins_plugin;

import com.loadimpact.util.StringUtils;
import hudson.model.AbstractBuild;
import hudson.model.Action;

/**
 * Provides the summary table and the link to the LoadImpact results page, at build instance.
 *
 * @author jens
 * @date 2013-09-10, 12:37
 */
@SuppressWarnings("UnusedDeclaration")
public class TestResultAction implements Action {
    /**
     * The build instance.
     */
    private final AbstractBuild<?, ?> build;

    /**
     * Its name.
     */
    private final String name;

    private       String testConfigId;
    /**
     * Its d.
     */
    private final String testRunId;

    /**
     * URL of the load-test target..
     */
    private final String targetUrl;

    /**
     * URL to the full results
     */
    private final String resultUrl;

    /**
     * Elapsed time for the load-test, in minutes.
     */
    private String elapsedTime;

    /**
     * Average response time for executing the script, in seconds.
     */
    private String responseTime;

    /**
     * Average requests per seconds.
     */
    private int requestCount, requestCountMax;

    /**
     * Average bandwidth, in Mbit/s.
     */
    private double bandwidth, bandwidthMax;

    /**
     * Max clients, during the test.
     */
    private int clientCount;

    public TestResultAction(AbstractBuild<?, ?> build, String name, String testConfigId, String testRunId, String targetUrl, String resultUrl, String elapsedTime, String responseTime, int requestCount, int requestCountMax, double bandwidth, double bandwidthMax, int clientCount) {
        this.build = build;
        this.name = name;
        this.testConfigId = testConfigId;
        this.testRunId = testRunId;
        this.targetUrl = targetUrl;
        this.resultUrl = resultUrl;
        this.elapsedTime = elapsedTime;
        this.responseTime = responseTime;
        this.requestCount = requestCount;
        this.requestCountMax = requestCountMax;
        this.bandwidth = bandwidth;
        this.bandwidthMax = bandwidthMax;
        this.clientCount = clientCount;
    }

    public AbstractBuild<?, ?> getBuild() {
        return build;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return testRunId;
    }

    public String getTestConfigurationUrl() {
        return "https://app.loadimpact.com/tests/" + testConfigId;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public String getResultUrl() {
        if (getHasResult()) {
            return resultUrl + "/embed";
        } else {
            //return "https://app.loadimpact.com/tests/" + testRunId;
            return getTestConfigurationUrl();
        }
    }

    public String getBandwidth() {
        return String.format("%.2f", bandwidth);
    }

    public String getBandwidthMax() {
        return String.format("%.2f", bandwidthMax);
    }

    public boolean getHasResult() {
        return !StringUtils.isBlank(resultUrl);
    }

    public String getElapsedTime() {
        return elapsedTime;
    }

    public String getResponseTime() {
        return responseTime;
    }

    public int getRequestCount() {
        return requestCount;
    }

    public int getRequestCountMax() {
        return requestCountMax;
    }

    public int getClientCount() {
        return clientCount;
    }

    public String getDisplayName() {
        return "Test Result";
    }

    public String getUrlName() {
        return "test-result";
    }

    public String getIconFileName() {
        return LoadImpactCore.imagePath("loadimpact-logo-24x24.png");
    }

    public String getLogo() {
        return LoadImpactCore.imagePath("loadimpact-full-logo-300x50.png");
    }

    public String getStyle() {
        return LoadImpactCore.cssPath("style.css");
    }

}
