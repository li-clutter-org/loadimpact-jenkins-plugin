package com.loadimpact.jenkins_plugin.loadtest;

import com.loadimpact.eval.LoadTestResult;
import com.loadimpact.eval.LoadTestResultListener;
import hudson.model.AbstractBuild;
import hudson.model.Result;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Arrays;

/**
 * DESCRIPTION
 *
 * @user jens
 * @date 2014-04-27
 */
public class JenkinsLoadTestResultListener implements LoadTestResultListener {
    
    private static class CurrentResult {
        public LoadTestResult loadTestResult;
        public Result jenkinsResult;
        public String reason;

        private CurrentResult(LoadTestResult loadTestResult, Result jenkinsResult, String reason) {
            this.loadTestResult = loadTestResult;
            this.jenkinsResult = jenkinsResult;
            this.reason = reason;
        }
    }
    
    private AbstractBuild<?, ?> build;
    private CurrentResult currentResult;

    public JenkinsLoadTestResultListener(AbstractBuild<?, ?> build) {
        this.build = build;
        this.currentResult = new CurrentResult(null, Result.SUCCESS, "");
    }

    public void markAs(LoadTestResult loadTestResult, String reason) {

        if (loadTestResult == LoadTestResult.failed) {
            currentResult=new CurrentResult(loadTestResult, Result.FAILURE, reason);
        } else if (loadTestResult == LoadTestResult.unstable) {
            currentResult = new CurrentResult(loadTestResult, Result.UNSTABLE, reason);
        }

    }

    public void stopBuild() {
        try {
            build.doStop();
        } catch (IOException ignore) {
            
        } catch (ServletException ignore) {
            
        }
    }

    public LoadTestResult getResult() {
        return currentResult.loadTestResult;
    }

    public String getReason() {
        return currentResult.reason;
    }

    public boolean isFailure() {
        return currentResult.loadTestResult == LoadTestResult.failed;
    }

    public boolean isNonSuccessful() { 
        return Arrays.asList(Result.UNSTABLE, Result.FAILURE, Result.ABORTED).contains(currentResult.jenkinsResult);
    }
    
}
