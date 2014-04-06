package com.loadimpact.jenkins_plugin;

import com.loadimpact.jenkins_plugin.client.*;
import hudson.AbortException;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Result;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Monitors a running load test and takes care of console logging and criteria evaluation.
 *
 * @author jens
 * @date 2013-10-13, 13:25
 */
@Deprecated
public class RunningTestListenerImpl implements RunningTestListener {
    private final LoadImpactCore  job;
    private final AbstractBuild   build;
    private final List<Evaluator> evaluators;
    private final PrintStream     console;
    private       Status          status;
    private       String          resultsUrl;
    private       Double          lastPercentage;
    private       boolean         finishing;
    private       long            runningStartTime;
    private       boolean         runEvaluators;

    public RunningTestListenerImpl(LoadImpactCore job, AbstractBuild build, BuildListener listener) {
        this.job = job;
        this.build = build;
        this.console = listener.getLogger();
        this.evaluators = new ArrayList<Evaluator>();
        for (ThresholdView t : job.getThresholds()) {
            this.evaluators.add(new Evaluator(t, job.getCriteriaDelayQueueSize()));
        }
    }

    /**
     * Invoked before the load test has launched. 
     * @param c  the test config
     */
    public void beforeTest(TestConfiguration c) {
        console.printf("Creating load test based on test configuration [%d] %s%n", c.id, c.name);
        console.printf("Target is %s%n", c.url);
        console.printf("Expected duration: %s%n", job.getLoadTestHeader().getDuration());
    }

    /**
     * Invoked periodically during a load test.
     * @param t         the test instance
     * @param client    LIClient to use for fetching more status data
     * @throws AbortException   if the load test should be aborted
     */
    public void duringTest(TestInstance t, LoadImpactClient client) throws AbortException {
        if (status == null || status != t.status) {
            status = t.status;
            console.printf("Status: %s%n", t.status);

            if (status.isRunning()) {
                runningStartTime = now();
            }

            if (resultsUrl == null && Util.startsWith(t.resultUrl, "http")) {
                resultsUrl = t.resultUrl;
                console.printf("Starting load test [%d] %s%n", t.id, t.name);
                console.printf("Follow the progress at %s%n", t.resultUrl);
            }
        }

        List<TestResult> progress = client.getTestResultsSingle(t.id, ResultsCategory.progress_percent_total);
        if (job.isLogJson()) job.log().info("Progress: " + progress);
        if (!progress.isEmpty()) {
            double percentage = Util.last(progress).value.doubleValue();
            if (lastPercentage == null || lastPercentage != percentage) {
                lastPercentage = percentage;
                int totalMinutes = job.getLoadTestHeader().getDurationInMinutes();
                console.printf("Running: %s (~ %.1f minutes remaining)%n",
                        Util.percentageBar(percentage), totalMinutes * (100D - percentage) / 100D);
            } else if (!finishing && percentage >= 100D) {
                finishing = true;
                console.printf("Status: %s%n", "finishing");
            }
        }

        if (!runEvaluators && status.isRunning() && !finishing) {
            if (job.getCriteriaDelayUnit().equals(DelayUnit.users)) {
                List<TestResult> c = client.getTestResultsSingle(t.id, ResultsCategory.clients_active);
                int usersActive = c.isEmpty() ? 0 : Util.last(c).value.intValue();
                int usersThreshold = job.getCriteriaDelayValue();
                runEvaluators = (usersThreshold < usersActive);
                if (runEvaluators) {
                    console.printf("Start evaluating failure criteria: %d users > %d%n", usersActive, usersThreshold);
                }
            } else if (job.getCriteriaDelayUnit().equals(DelayUnit.seconds)) {
                int delaySeconds = job.getCriteriaDelayValue() * 1000;
                runEvaluators = (runningStartTime + delaySeconds) < now();
                if (runEvaluators) {
                    console.printf("Start evaluating failure criteria: %d s after start%n", delaySeconds / 1000);
                }
            }
        } else if (runEvaluators && finishing) {
            runEvaluators = false;
        }

        if (runEvaluators) {
            List<ThresholdView> thresholdViews = job.getThresholds();
            ResultsCategory[] categories = Util.map(thresholdViews, new Util.MapClosure<ThresholdView, ResultsCategory>() {
                public ResultsCategory eval(ThresholdView t) {
                    return t.metric.category;
                }
            }).toArray(new ResultsCategory[thresholdViews.size()]);

            Map<ResultsCategory, List<TestResult>> resultsPerCategory = client.getTestResults(t.id, categories);
            if (job.isLogJson()) job.log().info("Partial Results: " + resultsPerCategory);

            for (Evaluator e : evaluators) {
                List<TestResult> results = resultsPerCategory.get(e.getThreshold().metric.category);
                e.accumulate(results);
                
                final Logger log = Logger.getLogger(getClass().getName());
                log.info(String.format("[Evaluator] %s: value=%d, threshold=%d",
                        e.getThreshold().metric.label, e.getAggregatedValue(), e.getThreshold().value));

                if (e.isExceeded()) {
                    Result currentResult = build.getResult();
                    Result nextResult    = e.getThreshold().result;
                    log.info(String.format("[Evaluator] %s: Threshold exceeded. build.result=%s, threshold.result=%s",
                            e.getThreshold().metric.label, currentResult, nextResult));
                    
                    if (currentResult == null || currentResult == Result.ABORTED || nextResult.isWorseOrEqualTo(currentResult)) {
                        logThresholdViolation(e);
                        build.setResult(nextResult);
                        if (nextResult == Result.FAILURE && job.isAbortAtFailure()) {
                            throw new AbortException("Aborted because threshold exceeded");
                        }
                    }
                }
            }
        }
    }

    /**
     * Invoked after the load test.
     * @param result    test result
     */
    public void afterTest(TestInstance result) {
        console.printf("Status: %s%n", result.status);
        console.printf("Load Impact results at %s%n", result.resultUrl);
    }

    /**
     * Logs to the jenkins console info about the threshold violation.
     * @param e   an evaluator
     */
    private void logThresholdViolation(Evaluator e) {
        console.printf("Threshold '%s' value=%d %s %d => %s%n",
                e.getThreshold().metric.label,
                e.getAggregatedValue(),
                e.getThreshold().operator.symbol,
                e.getThreshold().value,
                e.getThreshold().result.toString());
    }

    /**
     * Returns the current time stamp.
     * @return now
     */
    private long now() {
        return System.currentTimeMillis();
    }

}
