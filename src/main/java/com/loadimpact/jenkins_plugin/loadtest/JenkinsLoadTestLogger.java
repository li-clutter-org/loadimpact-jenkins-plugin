package com.loadimpact.jenkins_plugin.loadtest;

import com.loadimpact.eval.LoadTestLogger;

import java.io.PrintStream;

/**
 * Wrapper around the jenkins console logger
 *
 * @user jens
 * @date 2014-04-27
 */
public class JenkinsLoadTestLogger implements LoadTestLogger {
    private PrintStream stream;

    public JenkinsLoadTestLogger(PrintStream stream) {
        this.stream = stream;
    }

    public void started(String msg) {
        stream.println(msg);
    }

    public void finished(String msg) {
        stream.println(msg);
    }

    public void message(String msg) {
        stream.println(msg);
    }

    public void message(String fmt, Object... args) {
        stream.printf(fmt+"%n", args);
    }

    public void failure(String reason) {
        stream.println(reason);
    }
}
