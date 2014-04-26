package com.loadimpact.jenkins_plugin;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.loadimpact.eval.DelayUnit;
import com.loadimpact.jenkins_plugin.client.*;
import com.loadimpact.util.ListUtils;
import com.loadimpact.util.StringUtils;
import hudson.AbortException;
import hudson.Functions;
import hudson.Launcher;
import hudson.PluginWrapper;
import hudson.model.*;
import jenkins.model.Jenkins;
import org.joda.time.format.ISODateTimeFormat;

import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.loadimpact.jenkins_plugin.client.ResultsCategory.*;

/**
 * Common parts of this plugin, used by both the build and post-build tasks.
 *
 * @author jens
 * @date 2013-10-21, 09:06
 */
public class LoadImpactCore {
    @Deprecated
    private static final String PLUGIN_NAME = "LoadImpact-Jenkins-plugin";

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
     * @param build     jenkins build instance
     * @param launcher  jenkins launcher
     * @param listener  jenkins listener
     * @return true if successful
     * @throws InterruptedException     if a waiting thread was interrupted
     * @throws IOException  if some I/O went wrong
     */
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        final PrintStream console = listener.getLogger();
        listener.started(Arrays.asList((Cause) new Cause.UserCause()));

        LoadImpactClient client = getLoadImpactClient();
        try {
            RunningTestListener progressMonitor = new RunningTestListenerImpl(this, build, listener);
            TestInstance testInstance = client.runTest(getLoadTestId(), pollInterval, progressMonitor);

            console.printf(Messages.LoadImpactCore_FetchingResult());
            Map<ResultsCategory, List<TestResult>> testResults = client.getTestResults(testInstance.id, user_load_time, requests_per_second, bandwidth, clients_active);
            TestResultAction testResultAction = new TestResultAction(build, testInstance, testResults);
            build.addAction(testResultAction);

            if (build.getResult() == null) {
                build.setResult(Result.SUCCESS);
            }
            return true;
        } catch (AbortException e) {
            listener.error(Messages.LoadImpactCore_Aborted(e.getMessage()));
            build.setResult(Result.ABORTED);
        } catch (Exception e) {
            log().log(Level.WARNING, String.format("Failed to execute HTTP REST call: %s", e), e);
            log().log(Level.WARNING, "Exception", e);

            listener.error(Messages.LoadImpactCore_Failed(e));
            build.setResult(Result.FAILURE);
        }

        return false;
    }

    /**
     * Creates and returns the load test meta data for the summary.
     * @param project  jenkins project
     * @return jenkins action
     */
    public Action getProjectAction(AbstractProject<?, ?> project) {
        if (loadTestHeader == null) {
            if (StringUtils.isBlank(getApiToken())) {
                log().warning("No API token defined");
                return new LoadTestHeader();
            }

            try {
                TestConfiguration cfg = getLoadImpactClient().getTestConfiguration(getLoadTestId());
                log().info(Messages.LoadImpactCore_FetchedConfig(cfg));

                String date = ISODateTimeFormat.date().print(cfg.date.getTime()) + " " + ISODateTimeFormat.timeNoMillis().print(cfg.date.getTime());
                int duration = ListUtils.reduce(cfg.schedules, 0, new ListUtils.ReduceClosure<Integer, TestConfiguration.LoadSchedule>() {
                    public Integer eval(Integer sum, TestConfiguration.LoadSchedule s) {
                        return sum + s.duration;
                    }
                });
                int clients = ListUtils.reduce(cfg.schedules, 0, new ListUtils.ReduceClosure<Integer, TestConfiguration.LoadSchedule>() {
                    public Integer eval(Integer max, TestConfiguration.LoadSchedule s) {
                        return Math.max(max, s.users);
                    }
                });
                List<LoadTestHeader.Zone> zones = new ArrayList<LoadTestHeader.Zone>();
                for (TestConfiguration.Track track : cfg.tracks) {
                    zones.add(new LoadTestHeader.Zone(track.zone, track.percent));
                }

                loadTestHeader = new LoadTestHeader(loadTestId, cfg.name, cfg.date, cfg.url, duration, clients, cfg.userType.label, zones);
            } catch (Exception e) {
                return new LoadTestHeader(loadTestId, "Error: "+e.toString(), new Date(), "", 0, 0, "", null);
            }
        }
        return loadTestHeader;
    }

    /**
     * Returns the load test description.
     * @return jenkins action
     */
    public LoadTestHeader getLoadTestHeader() {
        return loadTestHeader;
    }


    /**
     * Creates a LoadImpact REST client.
     * @return a client
     */
    private LoadImpactClient getLoadImpactClient() {
        return new LoadImpactClient(getApiToken(), isLogHttp());
    }

    /**
     * Helper function that returns a proper image URL.
     * @param imgName   name of image file
     * @return jenkins URL
     */
    @Deprecated
    public static String imgUrl(final String imgName) {
        return Functions.getResourcePath() + "/plugin/" + PLUGIN_NAME + "/img/" + imgName;
    }

    /**
     * Reads the proper plugin name from the MANIFEST
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
     * @param name its bare file name
     * @return its path
     */
    public static String imagePath(final String name) {
        return String.format("%s/plugin/%s/img/%s", Functions.getResourcePath(), getPluginName(), name);
    }

    /**
     * Returns a proper url path for a bundled style sheet.
     * @param name its bare file name
     * @return its path
     */
    public static String cssPath(final String name) {
        return String.format("%s/plugin/%s/css/%s", Functions.getResourcePath(), getPluginName(), name);
    }
    

    /**
     * Returns its logger.
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
