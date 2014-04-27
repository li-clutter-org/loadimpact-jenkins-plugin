package com.loadimpact.jenkins_plugin.loadtest;

import com.loadimpact.eval.DelayUnit;
import com.loadimpact.eval.LoadTestParameters;
import com.loadimpact.eval.Threshold;
import com.loadimpact.jenkins_plugin.LoadImpactCore;

/**
 * DESCRIPTION
 *
 * @user jens
 * @date 2014-04-27
 */
public class JenkinsLoadTestParameters implements LoadTestParameters {
    private LoadImpactCore core;

    public JenkinsLoadTestParameters(LoadImpactCore core) {
        this.core = core;
    }

    public String getApiToken() {
        return core.getApiTokenId();
    }

    public int getTestConfigurationId() {
        return core.getLoadTestId();
    }

    public Threshold[] getThresholds() {
        return null; //todo
    }

    public DelayUnit getDelayUnit() {
        return core.getCriteriaDelayUnit();
    }

    public int getDelayValue() {
        return core.getCriteriaDelayValue();
    }

    public int getDelaySize() {
        return core.getCriteriaDelayQueueSize();
    }

    public boolean isAbortAtFailure() {
        return core.isAbortAtFailure();
    }

    public int getPollInterval() {
        return core.getPollInterval();
    }

    public boolean isLogHttp() {
        return core.isLogHttp();
    }

    public boolean isLogReplies() {
        return core.isLogJson();
    }

    public boolean isLogDebug() {
        return true;
    }
}
