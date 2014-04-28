package com.loadimpact.jenkins_plugin;

import hudson.model.AbstractBuild;
import hudson.model.Action;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

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

    /**
     * Its id.
     */
    private final String id;

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

    public TestResultAction(AbstractBuild<?, ?> build, String name, String id, String targetUrl, String resultUrl, String elapsedTime, String responseTime, int requestCount, int requestCountMax, double bandwidth, double bandwidthMax, int clientCount) {
        this.build = build;
        this.name = name;
        this.id = id;
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

    private PeriodFormatter timeFmt() {
        return new PeriodFormatterBuilder()
                .minimumPrintedDigits(0)
                .printZeroNever()
                .appendHours()
                .appendSeparator("h ")
                .appendMinutes()
                .appendSeparator("m ")
                .appendSeconds()
                .appendSuffix("s")
                .toFormatter();
    }

    public AbstractBuild<?, ?> getBuild() {
        return build;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public String getTestConfigurationUrl() {
        return "https://loadimpact.com/test/config/edit/" + getId();
    }

    public String getBandwidth() {
        return String.format("%.2f", bandwidth);
    }

    public String getBandwidthMax() {
        return String.format("%.2f", bandwidthMax);
    }

    public String getResultUrl() {
        return resultUrl + "/embed";
    }

    public boolean getHasResult() {
        return resultUrl != null;
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
