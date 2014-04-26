package com.loadimpact.jenkins_plugin;

import com.loadimpact.jenkins_plugin.client.ResultsCategory;
import com.loadimpact.jenkins_plugin.client.TestInstance;
import com.loadimpact.jenkins_plugin.client.TestResult;
import com.loadimpact.util.ListUtils;
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
        this.resultUrl    = test.resultUrl;
        this.elapsedTime  = timeFmt().print(new Period(test.started.getTime(), test.ended.getTime()));
//        this.responseTime = timeFmt().print(new Period((long) ListUtils.average(Util.collectDecimals(results.get(user_load_time)))));
        this.responseTime = timeFmt().print(new Period((long) ListUtils.average(ListUtils.map(results.get(user_load_time), new ListUtils.MapClosure<TestResult, Number>() {
            public Number eval(TestResult r) {
                return r.value.doubleValue();
            }
        }))));
        
//        this.clientCount  = Collections.max(Util.collectInts(results.get(clients_active)));
        this.clientCount  = Collections.max(ListUtils.map(results.get(clients_active), new ListUtils.MapClosure<TestResult, Integer>() {
            public Integer eval(TestResult r) {
                return r.value.intValue();
            }
        }));

//        List<Double> requestCounts = Util.collectDecimals(results.get(requests_per_second));
        List<Double> requestCounts = ListUtils.map(results.get(requests_per_second), new ListUtils.MapClosure<TestResult, Double>() {
            public Double eval(TestResult r) {
                return r.value.doubleValue();
            }
        });
        this.requestCount          = (int) ListUtils.average(requestCounts);
        this.requestCountMax       = Collections.max(requestCounts).intValue();

//        List<Double> bandwidths    = Util.collectDecimals(results.get(ResultsCategory.bandwidth));
        List<Double> bandwidths    = ListUtils.map(results.get(ResultsCategory.bandwidth), new ListUtils.MapClosure<TestResult, Double>() {
            public Double eval(TestResult r) {
                return r.value.doubleValue();
            }
        });
        this.bandwidth             = ListUtils.average(bandwidths) / 1E6;
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
