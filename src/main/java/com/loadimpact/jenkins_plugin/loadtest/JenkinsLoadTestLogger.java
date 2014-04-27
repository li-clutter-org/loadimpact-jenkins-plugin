package com.loadimpact.jenkins_plugin.loadtest;

import com.loadimpact.eval.LoadTestLogger;

import java.io.PrintStream;

/**
 * DESCRIPTION
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
        stream.printf(fmt, args);
    }

    public void failure(String reason) {
        stream.println(reason);
    }
}
