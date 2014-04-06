package com.loadimpact.jenkins_plugin.client;

import hudson.AbortException;
import org.apache.commons.lang.StringUtils;
import org.glassfish.jersey.client.filter.HttpBasicAuthFilter;
import org.glassfish.jersey.filter.LoggingFilter;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Facade to all LoadImpact REST API calls.
 *
 * @author jens
 * @date 2013-09-08, 17:39
 */
@Deprecated
public class LoadImpactClient {
    private static final String     baseUri = "https://api.loadimpact.com/v2";
    private final String            apiKey;
    private final WebTarget         wsBase;
    private final Logger            log;


    {
        log = Logger.getLogger(this.getClass().getName());
    }
    
    /**
     * Empty constructor, used only for unit tests.
     */
    protected LoadImpactClient() {
        apiKey = null;
        wsBase = null;
    }

    public LoadImpactClient(String apiKey) {
        this(apiKey, false);
    }
    
    /**
     * Creates a client and initializes the REST client with the given API key.
     * @param apiKey    API key to use for authentication
     * @param debug     set to true for logging the REQ/RES headers and JSON result (max 1000 chars)
     */
    public LoadImpactClient(String apiKey, boolean debug) {
        checkApiKey(apiKey);
        this.apiKey = apiKey;

        Client client = ClientBuilder.newClient();
        client.register(new HttpBasicAuthFilter(this.apiKey, ""));
        if (debug) client.register(new LoggingFilter(log, 1000));
        this.wsBase = client.target(baseUri);
    }

    /**
     * Syntax checks the API key.
     * @param apiKey    key to check
     * @throws IllegalArgumentException     if invalid
     */
    protected void checkApiKey(String apiKey) {
        if (StringUtils.isBlank(apiKey)) throw new IllegalArgumentException("Empty key");
        if (apiKey.length() != 64) throw new IllegalArgumentException("Wrong length");
        if (!apiKey.matches("[a-fA-F0-9]+")) throw new IllegalArgumentException("Not a HEX value");
    }

    /**
     * Returns true if we can successfully logon and fetch some test-configs.
     * @return true     if can logon
     */
    public boolean isValidKey() {
        try {
            Response response = wsBase.path("test-configs").request(MediaType.APPLICATION_JSON).get();
            return response.getStatus() == HttpURLConnection.HTTP_OK;
        } catch (Exception e) {
            log.info("API token validation failed: " + e);
        }
        return false;
    }

    /**
     * Retrieves a single test configuration
     * @param id    test-configuration ID
     * @return TestConfiguration
     */
    public TestConfiguration getTestConfiguration(int id) {
        JsonObject json = wsBase.path("test-configs").path(Integer.toString(id))
                .request(MediaType.APPLICATION_JSON).get(JsonObject.class);
        return new TestConfiguration(json);
    }

    /**
     * Retrieves all test configurations.
     * @return [TestConfiguration, ...]
     */
    public List<TestConfiguration> getTestConfigurations() {
        List<TestConfiguration> testConfigs = new ArrayList<TestConfiguration>();
        
        JsonArray testConfigsJson = wsBase.path("test-configs").request(MediaType.APPLICATION_JSON).get(JsonArray.class);
        for (int i = 0; i < testConfigsJson.size(); ++i) {
            JsonObject json = testConfigsJson.getJsonObject(i);
            testConfigs.add(new TestConfiguration(json));
        }

        return testConfigs;
    }

    /**
     * Retrieves all test configurations as {id,name}.
     * @return [{id,name}, ...]
     */
    public Map<Integer, String> getTestConfigurationLabels() {
        Map<Integer, String> labels = new TreeMap<Integer, String>();
        for (TestConfiguration c : getTestConfigurations()) {
            labels.put(c.id, c.name);
        }
        return labels;
    }

    /**
     * Starts a load-test (i.e. test-configuration).
     * @param id    test-configuration ID
     * @return id of the running test-instance (test)
     */
    public int startTest(int id) {
        JsonObject test = wsBase.path("test-configs").path(Integer.toString(id)).path("start").request(MediaType.APPLICATION_JSON).post(null, JsonObject.class);
        return test.getInt("id");
    }

    /**
     * Aborts a running test.
     * @param id    test-instance ID
     * @return true if successful
     */
    public boolean abortTest(int id) {
        Response response = wsBase.path("tests").path(Integer.toString(id)).path("abort").request().post(null);
        boolean success = response.getStatus() == HttpURLConnection.HTTP_NO_CONTENT;
        if (!success) log.info("Failed to abort running test. HTTP-Code=" + response.getStatus());
        return success;
    }
    
    /**
     * Starts a load-test and monitors its progress using the given {@link RunningTestListener}.
     * @param id            test-configuration ID
     * @param pollSeconds   interval in seconds, for how often to poll for updates
     * @param listener      {@link RunningTestListener} that will be invoked for each poll
     * @return the last returned {@link TestInstance} object
     */
    public TestInstance runTest(final int id, int pollSeconds, final RunningTestListener listener) throws Exception {
        if (id <= 0) throw new IllegalArgumentException("Negative ID: " + id);
        if (listener == null) throw new NullPointerException("No test-listener");
        if (pollSeconds <= 0) throw new IllegalArgumentException("Non-positive poll-seconds: " + pollSeconds);

        log.info(String.format("[runTest] starting id=%d, poll-interval=%d", id, pollSeconds));
        final TestConfiguration c = getTestConfiguration(id);
        listener.beforeTest(c);

        final int testId = startTest(id);
        log.info(String.format("[runTest] started id=%d", testId));

        ProgressPoller poller = new ProgressPoller(testId, c.name, listener);
        Timer timer = new Timer("Test-result poller");
        timer.schedule(poller, 0, pollSeconds * 1000);
        
        poller.waitForCompletion();
        if (poller.shouldAbort()) {
            log.info("Aborting running test ID=" + testId);
            try {
                abortTest(testId);
            } catch (Exception e) {
                log.info(String.format("Failed to abort the running test: %s", e));
            }

            if (poller.exception instanceof AbortException || poller.exception instanceof InterruptedException) {
                throw new AbortException(poller.exception.getMessage());
            } else {
                throw new RuntimeException(poller.exception.toString());
            } 
        }

        return poller.lastTestStatus;
    }
    
    private class ProgressPoller extends TimerTask {
        final int                   testId;
        final String                testName;
        final RunningTestListener   listener;
        final CountDownLatch        polling = new CountDownLatch(1);
        TestInstance                lastTestStatus = null;
        int                         logPrintCount  = 0;
        Exception                   exception;

        private ProgressPoller(int testId, String testName, RunningTestListener listener) {
            this.testId   = testId;
            this.testName = testName;
            this.listener = listener;
        }

        @Override
        public void run() {
            try {
                TestInstance r = getTest(testId);
                log.fine(String.format("[runTest::poller] %d: %s", ++logPrintCount, r));
                if (r.status.isCompleted()) {
                    success(r);
                    return;
                }

                listener.duringTest(r, LoadImpactClient.this);
            } catch (AbortException e) {
                log.info(String.format("Load test job aborted: %s", e));
                failure(e);
            } catch (Exception e) {
                log.log(Level.WARNING, "Load test job failed", e);
                failure(e);
            }
        }

        public void waitForCompletion() throws Exception {
            try {
                polling.await();
            } catch (InterruptedException ignore) {
                log.info("Load test job aborted by user");
                failure(ignore);
            } catch (Exception error) {
                log.log(Level.WARNING, "Load test job failed", error);     
                failure(error);
            }
        }

        public boolean shouldAbort() {
            return exception != null;
        }

        private void success(TestInstance r) {
            cancel();
            lastTestStatus = r;
            polling.countDown();
            listener.afterTest(lastTestStatus);
        }
        
        private void failure(Exception e) {
            cancel();
            exception = e;
            polling.countDown();
        }
        
    }



    /**
     * Retrieves a test-instance, which contains its status and result-url.
     * @param id    test (result) ID
     * @return TestResult
     */
    public TestInstance getTest(int id) {
        JsonObject r = wsBase.path("tests").path(Integer.toString(id)).request(MediaType.APPLICATION_JSON).get(JsonObject.class);

        return new TestInstance(r.getInt("id"),
                r.getString("title"),
                r.getString("url"),
                r.getString("started"),
                r.getString("ended"),
                r.getInt("status"),
                r.getString("public_url")
        );
    }

    /**
     * Retrieves a single test-result.
     * @param id            test id
     * @param category      what to fetch 
     * @return [{@link TestResult}]
     */
    public List<TestResult> getTestResultsSingle(int id, ResultsCategory category) {
        Map<ResultsCategory, List<TestResult>> results = getTestResults(id, category);
        return results.get(category);
    }

    /**
     * Retrieves all test-result items for a given set of {@link ResultsCategory}.
     * @param id        test ID
     * @param categories  one or more {@link ResultsCategory} to fetch
     * @return [cat1:result1, cat2:result2, ...]
     */
    public Map<ResultsCategory, List<TestResult>> getTestResults(int id, ResultsCategory... categories) {
        List<String> categoryIds = Util.map(Arrays.asList(categories), new Util.MapClosure<ResultsCategory, String>() {
            public String eval(ResultsCategory c) { return c.param; }
        });

        JsonObject response = wsBase.path("tests").path(Integer.toString(id)).path("results")
                                    .queryParam("ids", Util.join(categoryIds, ","))
                                    .request(MediaType.APPLICATION_JSON).get(JsonObject.class);

        Map<ResultsCategory, List<TestResult>> results = new TreeMap<ResultsCategory, List<TestResult>>();
        for (ResultsCategory category : categories) {
            List<TestResult> categoryResults = new ArrayList<TestResult>();
            results.put(category, categoryResults);

            JsonArray data = response.getJsonArray(category.param);
            for (int i = 0; i < data.size(); ++i) {
                JsonObject item = data.getJsonObject(i);
                categoryResults.add(new TestResult(category, item));
            }
        }

        return results;
    }



}
