package com.loadimpact.jenkins_plugin;

import com.loadimpact.jenkins_plugin.client.ResultsCategory;
import com.loadimpact.jenkins_plugin.client.TestInstance;
import com.loadimpact.jenkins_plugin.client.TestResult;
import com.loadimpact.jenkins_plugin.client.Util;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.loadimpact.jenkins_plugin.client.ResultsCategory.*;

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


    public TestResultAction(AbstractBuild<?, ?> build, TestInstance test, Map<ResultsCategory, List<TestResult>> results) {
        this.build        = build;
        this.id           = String.valueOf(test.id);
        this.name         = test.name;
        this.targetUrl    = test.targetUrl;
        this.resultUrl    = test.resultUrl + "/embed";
        this.elapsedTime  = timeFmt().print(new Period(test.started.getTime(), test.ended.getTime()));
        this.responseTime = timeFmt().print(new Period((long)Util.average(Util.collectDecimals(results.get(user_load_time)))));
        this.clientCount  = Collections.max(Util.collectInts(results.get(clients_active)));

        List<Double> requestCounts = Util.collectDecimals(results.get(requests_per_second));
        this.requestCount          = (int) Util.average(requestCounts);
        this.requestCountMax       = Collections.max(requestCounts).intValue();

        List<Double> bandwidths    = Util.collectDecimals(results.get(ResultsCategory.bandwidth));
        this.bandwidth             = Util.average(bandwidths) / 1E6;
        this.bandwidthMax          = Collections.max(bandwidths) / 1E6;
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
        return resultUrl;
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
        return LoadImpactTestRunTask.imgUrl("loadimpact-logo-24x24.png");
    }

}
