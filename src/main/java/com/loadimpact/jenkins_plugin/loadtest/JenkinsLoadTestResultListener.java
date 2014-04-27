package com.loadimpact.jenkins_plugin.loadtest;

import com.loadimpact.eval.LoadTestResult;
import com.loadimpact.eval.LoadTestResultListener;

/**
 * DESCRIPTION
 *
 * @user jens
 * @date 2014-04-27
 */
public class JenkinsLoadTestResultListener implements LoadTestResultListener {
    
    public void markAs(LoadTestResult result, String reason) {
        
    }

    public void stopBuild() {

    }

    public LoadTestResult getResult() {
        return null;
    }

    public String getReason() {
        return null;
    }

    public boolean isFailure() {
        return false;
    }

    public boolean isNonSuccessful() {
        return false;
    }
}
