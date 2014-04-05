package com.loadimpact.jenkins_plugin.client;

import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Integration tests of the LoadImpactClient REST invocations.
 *
 * @author jens
 * @date 2013-09-08, 17:59
 */
public class LoadImpactClientTest {
    private static final String API_KEY = "4abe6379050c34ed3f996785edd29e7943c56588fe8b755baba20fa4718758f1";
    private LoadImpactClient target;

    @Test
    public void properKeyShouldPass() throws Exception {
        target = new LoadImpactClient();
        target.checkApiKey(API_KEY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullKeyShouldFail() throws Exception {
        target = new LoadImpactClient();
        target.checkApiKey(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void emptyKeyShouldFail() throws Exception {
        target = new LoadImpactClient();
        target.checkApiKey("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void wrongSizedKeyShouldFail() throws Exception {
        target = new LoadImpactClient();
        target.checkApiKey(API_KEY.substring(10));
    }

    @Test(expected = IllegalArgumentException.class)
    public void notHexKeyShouldFail() throws Exception {
        target = new LoadImpactClient();
        target.checkApiKey(API_KEY.replaceAll("[0-9]", "X"));
    }

    @Test
    public void validKeyShouldPass() throws Exception {
        target = new LoadImpactClient(API_KEY);
        assertThat(target.isValidKey(), is(true));
    }
    
    @Test
    public void invalidKeyShouldFail() throws Exception {
        target = new LoadImpactClient(API_KEY.replace('4','9'));
        assertThat(target.isValidKey(), is(false));
    }

    @Test
    public void allTestConfigsShouldPass() throws Exception {
        target = new LoadImpactClient(API_KEY);
        Map<Integer,String> cfg = target.getTestConfigurationLabels();
        assertThat(cfg, is(notNullValue()));
        assertThat(cfg.size(), greaterThan(1)); 
    }

    @Test
    public void retrieveOneValidTestCfgShouldPass() throws Exception {
        target = new LoadImpactClient(API_KEY);
        int id = 1441273;
        TestConfiguration cfg = target.getTestConfiguration(id);
        assertThat(cfg, is(notNullValue()));
        assertThat(cfg.id, is(id));
        assertThat(cfg.name, is("PF"));
    }
    
    @Test
    public void retrieveOneTestShouldPass() throws Exception {
        target = new LoadImpactClient(API_KEY);
        int id = 1455781;
        TestInstance test = target.getTest(id);
        assertThat(test, is(notNullValue()));
        assertThat(test.id, is(id));
        assertThat(test.status, is(Status.finished));
        assertThat(test.targetUrl, is("http://www.ribomation.se/"));
    }

    @Test
    public void retrieveResultsClientsActiveShouldPass() throws Exception {
        target = new LoadImpactClient(API_KEY);
        int id = 1457155;

        List<TestResult> results = target.getTestResultsSingle(id, ResultsCategory.clients_active);
        assertThat(results, is(notNullValue()));
        assertThat(results.size(), is(30));
        assertThat(Collections.max(Util.collectInts(results)), is(10));
    }
    
    @Test
    public void retrieveResultsOfTwoCategoriesShouldPass() throws Exception {
        target = new LoadImpactClient(API_KEY);
        int id = 1457155;
        
        Map<ResultsCategory, List<TestResult>> results = target.getTestResults(id, ResultsCategory.clients_active, ResultsCategory.requests_per_second);
        assertThat(results, is(notNullValue()));
        assertThat(results.size(), is(2));

        List<TestResult> clientActive = results.get(ResultsCategory.clients_active);
        List<TestResult> requestsPerSeconds = results.get(ResultsCategory.requests_per_second);

        assertThat(clientActive.size(), is(30));
        assertThat(Collections.max(Util.collectInts(clientActive)), is(10));
        
        assertThat(requestsPerSeconds.size(), is(30));
    }
    
    
    
}
