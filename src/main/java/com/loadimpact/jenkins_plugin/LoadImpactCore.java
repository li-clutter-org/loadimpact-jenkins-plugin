package com.loadimpact.jenkins_plugin;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.loadimpact.ApiTokenClient;
import com.loadimpact.eval.DelayUnit;
import com.loadimpact.eval.LoadTestListener;
import com.loadimpact.jenkins_plugin.loadtest.JenkinsLoadTestLogger;
import com.loadimpact.jenkins_plugin.loadtest.JenkinsLoadTestParameters;
import com.loadimpact.jenkins_plugin.loadtest.JenkinsLoadTestResultListener;
import com.loadimpact.resource.Test;
import com.loadimpact.resource.TestConfiguration;
import com.loadimpact.resource.configuration.LoadClip;
import com.loadimpact.resource.configuration.LoadScheduleStep;
import com.loadimpact.resource.configuration.LoadTrack;
import com.loadimpact.resource.testresult.StandardMetricResult;
import com.loadimpact.util.ListUtils;
import com.loadimpact.util.StringUtils;
import hudson.Functions;
import hudson.Launcher;
import hudson.PluginWrapper;
import hudson.model.*;
import jenkins.model.Jenkins;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.logging.Logger;

import static com.loadimpact.resource.testresult.StandardMetricResult.Metrics.*;


/**
 * Common parts of this plugin, used by both the build and post-build tasks.
 *
 * @author jens
 * @date 2013-10-21, 09:06
 */
public class LoadImpactCore {

    /**
     * API token (credentials) ID.
     */
    private String apiTokenId;

    /**
     * LoadImpact load-test ID.
     */
    private int loadTestId;

    /**
     * Wait these number of seconds, before start evaluating progress status. 0=inactive.
     */
    private int criteriaDelayValue;

    /**
     * Only run evaluation if the the number of users are greater-or-equals to this. 0=inactive.
     */
    private DelayUnit criteriaDelayUnit;

    /**
     * How many values to base the current median value on.
     */
    private int criteriaDelayQueueSize;

    /**
     * Abort the test, if the job is marked as FAILED.
     */
    private boolean abortAtFailure;

    /**
     * Collection of thresholds used to evaluate as failure criteria.
     */
    private ThresholdView[] thresholdViews;

    /**
     * Interval (in seconds) of how often to poll for server status and criterion-data.
     */
    private int pollInterval;

    /**
     * If true, show the HTTP headers in the jenkins log.
     */
    private boolean logHttp;

    /**
     * If true, show the parsed JSON data in the jenkins log.
     */
    private boolean logJson;

    /**
     * Jenkins log stream.
     */
    private static transient Logger _log;

    /**
     * Provides a summary of the load-test job.
     */
    private transient LoadTestHeader loadTestHeader;

    private transient String agentRequestHeaderValue;


    public LoadImpactCore(String apiTokenId, int loadTestId, int criteriaDelayValue, String criteriaDelayUnit, int criteriaDelayQueueSize, boolean abortAtFailure, ThresholdView[] thresholdViews, int pollInterval, boolean logHttp, boolean logJson) {
        this.apiTokenId = apiTokenId;
        this.loadTestId = loadTestId;
        this.criteriaDelayValue = criteriaDelayValue;
        this.criteriaDelayUnit = DelayUnit.valueOf(criteriaDelayUnit);
        this.criteriaDelayQueueSize = criteriaDelayQueueSize;
        this.abortAtFailure = abortAtFailure;
        this.thresholdViews = thresholdViews;
        this.pollInterval = pollInterval;
        this.logHttp = logHttp;
        this.logJson = logJson;
    }

    public LoadImpactCore() {
        this.apiTokenId = null;
        this.loadTestId = 0;
        this.criteriaDelayValue = 0;
        this.criteriaDelayUnit = DelayUnit.seconds;
        this.criteriaDelayQueueSize = 1;
        this.abortAtFailure = false;
        this.thresholdViews = new ThresholdView[0];
        this.pollInterval = 5;
        this.logHttp = false;
        this.logJson = false;
    }

    @SuppressWarnings("UnusedDeclaration")
    private void dumpCredentials() {
        List<ApiTokenCredentials> tokens = CredentialsProvider.lookupCredentials(ApiTokenCredentials.class, (Item) null, null, (DomainRequirement) null);
        log().info("--- API Token Credentials ---");
        for (ApiTokenCredentials t : tokens) {
            log().info(t.toString());
        }
        log().info("--- END ---");
    }

    /**
     * Launches and monitors the load test.
     *
     * @param build    jenkins build instance
     * @param launcher jenkins launcher
     * @param listener jenkins listener
     * @return true if successful
     * @throws InterruptedException if a waiting thread was interrupted
     * @throws IOException          if some I/O went wrong
     */
    @SuppressWarnings("UnusedParameters")
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        final PrintStream console = listener.getLogger();
        listener.started(Arrays.asList((Cause) new Cause.UserCause()));

        ApiTokenClient                client           = getApiTokenClient();
        JenkinsLoadTestParameters     parameters       = new JenkinsLoadTestParameters(this);
        JenkinsLoadTestLogger         logger           = new JenkinsLoadTestLogger(console);
        JenkinsLoadTestResultListener resultListener   = new JenkinsLoadTestResultListener(build);
        LoadTestListener              loadTestListener = new LoadTestListener(parameters, logger, resultListener);

        TestConfiguration testConfiguration = client.getTestConfiguration(parameters.getTestConfigurationId());
        loadTestListener.onSetup(testConfiguration, client);

        logger.message("Launching the load test");
        int  testId = client.startTest(testConfiguration.id);
        Test test   = client.monitorTest(testId, parameters.getPollInterval(), loadTestListener);

        if (test != null && !resultListener.isNonSuccessful()) {
            logger.message(Messages.LoadImpactCore_FetchingResult());
            TestResultAction testResultAction = populateTestResults(testConfiguration, test, client, build);
            build.addAction(testResultAction);
            if (build.getResult() == null) build.setResult(Result.SUCCESS);

            return true;
        } else {
            listener.error(Messages.LoadImpactCore_Failed(""));
            build.setResult(Result.FAILURE);
        }

        return false;
    }

    TestResultAction populateTestResults(TestConfiguration testConfig, Test testRun, ApiTokenClient client, AbstractBuild<?, ?> build) {
        String   name              = testRun.title;
        String   testConfigId      = String.valueOf(testConfig.id);
        String   testRunId         = String.valueOf(testRun.id);
        String   targetUrl         = toString(testRun.url);
        String   resultUrl         = toString(testRun.publicUrl);
        String   elapsedTime       = computeElapsedTime(testRun);
        String   responseTime      = computeResponseTime(testRun, client);
        int      clientCount       = computeClientsCount(testRun, client);
        int[]    reqCnt            = computeRequestsCount(testRun, client);
        int      requestCount      = reqCnt[0];
        int      requestCountMax   = reqCnt[1];
        double[] bw                = computeBandwidth(testRun, client);
        double   bandwidthValue    = bw[0];
        double   bandwidthValueMax = bw[1];

        return new TestResultAction(build, name, testConfigId, testRunId, targetUrl, resultUrl,
                elapsedTime, responseTime, requestCount, requestCountMax, bandwidthValue, bandwidthValueMax, clientCount);
    }

    String toString(Object s) {
        if (s == null) return "";
        return s.toString();
    }

    String computeElapsedTime(Test tst) {
        if (tst.started != null && tst.ended != null) {
            return timeFmt().print(new Period(tst.started.getTime(), tst.ended.getTime()));
        }
        return "";
    }

    @SuppressWarnings("unchecked")
    String computeResponseTime(Test tst, ApiTokenClient client) {
        List<StandardMetricResult> results = (List<StandardMetricResult>) client.getStandardMetricResults(tst.id, USER_LOAD_TIME, null, null);
        List<Double> values = ListUtils.map(results, new ListUtils.MapClosure<StandardMetricResult, Double>() {
            public Double eval(StandardMetricResult r) {
                return r.value.doubleValue();
            }
        });
        return timeFmt().print(new Period((long) ListUtils.average(values)));
    }

    @SuppressWarnings("unchecked")
    Integer computeClientsCount(Test tst, ApiTokenClient client) {
        List<StandardMetricResult> results = (List<StandardMetricResult>) client.getStandardMetricResults(tst.id, CLIENTS_ACTIVE, null, null);
        List<Integer> values = ListUtils.map(results, new ListUtils.MapClosure<StandardMetricResult, Integer>() {
            public Integer eval(StandardMetricResult r) {
                return r.value.intValue();
            }
        });
        return Collections.max(values);
    }

    @SuppressWarnings("unchecked")
    int[] computeRequestsCount(Test tst, ApiTokenClient client) {
        List<StandardMetricResult> results = (List<StandardMetricResult>) client.getStandardMetricResults(tst.id, REQUESTS_PER_SECOND, null, null);
        List<Double> values = ListUtils.map(results, new ListUtils.MapClosure<StandardMetricResult, Double>() {
            public Double eval(StandardMetricResult r) {
                return r.value.doubleValue();
            }
        });
        int avg = (int) ListUtils.average(values);
        int max = Collections.max(values).intValue();
        return new int[]{avg, max};
    }

    @SuppressWarnings("unchecked")
    double[] computeBandwidth(Test tst, ApiTokenClient client) {
        List<StandardMetricResult> results = (List<StandardMetricResult>) client.getStandardMetricResults(tst.id, BANDWIDTH, null, null);
        List<Double> values = ListUtils.map(results, new ListUtils.MapClosure<StandardMetricResult, Double>() {
            public Double eval(StandardMetricResult r) {
                return r.value.doubleValue();
            }
        });
        double avg = ListUtils.average(values) / 1E6;
        double max = Collections.max(values).intValue() / 1E6;
        return new double[]{avg, max};
    }

    PeriodFormatter timeFmt() {
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

    /**
     * Creates and returns the load test meta data for the summary.
     *
     * @param project jenkins project
     * @return jenkins action
     */
    @SuppressWarnings("UnusedParameters")
    public Action getProjectAction(AbstractProject<?, ?> project) {
        if (loadTestHeader == null) {
            if (StringUtils.isBlank(getApiToken())) {
                log().warning("No API token defined");
                return new LoadTestHeader();
            }

            try {
                TestConfiguration tstCfg = getApiTokenClient().getTestConfiguration(getLoadTestId());
                log().info(Messages.LoadImpactCore_FetchedConfig(tstCfg));

                int duration = ListUtils.reduce(tstCfg.loadSchedule, 0, new ListUtils.ReduceClosure<Integer, LoadScheduleStep>() {
                    public Integer eval(Integer sum, LoadScheduleStep s) {
                        return sum + s.duration;
                    }
                });
                int clients = ListUtils.reduce(tstCfg.loadSchedule, 0, new ListUtils.ReduceClosure<Integer, LoadScheduleStep>() {
                    public Integer eval(Integer max, LoadScheduleStep s) {
                        return Math.max(max, s.users);
                    }
                });

                List<LoadTestHeader.Zone> zones = ListUtils.map(tstCfg.tracks, new ListUtils.MapClosure<LoadTrack, LoadTestHeader.Zone>() {
                    public LoadTestHeader.Zone eval(LoadTrack t) {
                        Integer percentage = ListUtils.reduce(t.clips, 0, new ListUtils.ReduceClosure<Integer, LoadClip>() {
                            public Integer eval(Integer sum, LoadClip loadClip) {
                                return sum + loadClip.percent;
                            }
                        });
                        return new LoadTestHeader.Zone(t.zone, percentage);
                    }
                });

                loadTestHeader = new LoadTestHeader(loadTestId, tstCfg.name, tstCfg.updated, toString(tstCfg.url), duration, clients, tstCfg.userType.label, zones);
            } catch (Exception e) {
                return new LoadTestHeader(loadTestId, "Error: " + e.toString(), new Date(), "", 0, 0, "", null);
            }
        }
        return loadTestHeader;
    }

    /**
     * Returns the load test description.
     *
     * @return jenkins action
     */
    public LoadTestHeader getLoadTestHeader() {
        return loadTestHeader;
    }

    private ApiTokenClient getApiTokenClient() {
        ApiTokenClient client = new ApiTokenClient(getApiToken());
        client.setDebug(isLogHttp());
        client.setAgentRequestHeaderValue(getAgentRequestHeaderValue());
        return client;
    }

    public Properties getMavenPomData() {
        Properties  p       = new Properties();
        String      pomFile = "/META-INF/maven/com.loadimpact/LoadImpact-Jenkins-plugin/pom.properties";
        InputStream is      = getClass().getResourceAsStream(pomFile);
        if (is != null) {
            try {
                p.load(is);
            } catch (IOException ignore) {
            }
        }
        return p;
    }

    public String getAgentRequestHeaderValue() {
        if (agentRequestHeaderValue == null) {
            String pluginVersion = getMavenPomData().getProperty("version", "0.0.0");
            String jenkinsVersion = Jenkins.getVersion().toString();
            agentRequestHeaderValue = String.format("LoadImpactJenkinsPlugin/%s Jenkins/%s", pluginVersion, jenkinsVersion);
        }
        return agentRequestHeaderValue;
    }

    /**
     * Reads the proper plugin name from the MANIFEST
     *
     * @return its name
     */
    public static String getPluginName() {
        try {
            PluginWrapper plugin = Jenkins.getInstance().getPluginManager().whichPlugin(LoadImpactCore.class);
            return plugin.getShortName();
        } catch (Exception e) {
            log().severe("Failed to get plugin object. " + e);
            return "NO_PLUGIN_NAME";
        }
    }

    /**
     * Returns a proper url path for a bundled image.
     *
     * @param name its bare file name
     * @return its path
     */
    public static String imagePath(final String name) {
        return String.format("%s/plugin/%s/img/%s", Functions.getResourcePath(), getPluginName(), name);
    }

    /**
     * Returns a proper url path for a bundled style sheet.
     *
     * @param name its bare file name
     * @return its path
     */
    public static String cssPath(final String name) {
        return String.format("%s/plugin/%s/css/%s", Functions.getResourcePath(), getPluginName(), name);
    }


    /**
     * Returns its logger.
     *
     * @return logger
     */
    public static Logger log() {
        if (_log == null) {
            _log = Logger.getLogger(LoadImpactCore.class.getName());
        }
        return _log;
    }

    @Override
    public String toString() {
        return "LoadImpact{" +
                "apiTokenId=" + apiTokenId +
                ", loadTestId=" + loadTestId +
                ", thresholds=" + Arrays.toString(thresholdViews) +
                ", criteriaDelayQueueSize=" + criteriaDelayQueueSize +
                ", criteriaDelayValue=" + criteriaDelayValue +
                ", criteriaDelayUnit='" + criteriaDelayUnit + '\'' +
                ", abortAtFailure=" + abortAtFailure +
                ", pollInterval=" + pollInterval +
                ", logHttp=" + logHttp +
                ", logJson=" + logJson +
                '}';
    }

    private List<ApiTokenCredentials> getAllTokens() {
        return CredentialsProvider.lookupCredentials(ApiTokenCredentials.class, (Item) null, null, (DomainRequirement) null);
    }

    private ApiTokenCredentials getToken(String id) {
        for (ApiTokenCredentials t : getAllTokens()) {
            if (t.getId().equals(id)) return t;
        }
        return null;
    }

    private String getApiToken() {
        try {
            return getToken(getApiTokenId()).getApiToken().getPlainText();
        } catch (Exception e) {
            return null;
        }
    }

    public String getApiTokenId() {
        return apiTokenId;
    }

    public int getLoadTestId() {
        return loadTestId;
    }

    public int getCriteriaDelayValue() {
        return criteriaDelayValue;
    }

    public DelayUnit getCriteriaDelayUnit() {
        return criteriaDelayUnit;
    }

    public int getCriteriaDelayQueueSize() {
        return criteriaDelayQueueSize;
    }

    public boolean isAbortAtFailure() {
        return abortAtFailure;
    }

    public List<ThresholdView> getThresholds() {
        if (thresholdViews == null) {
            thresholdViews = new ThresholdView[0];
        }
        return Collections.unmodifiableList(Arrays.asList(thresholdViews));

    }

    public int getPollInterval() {
        return pollInterval;
    }

    public boolean isLogHttp() {
        return logHttp;
    }

    public boolean isLogJson() {
        return logJson;
    }
}
