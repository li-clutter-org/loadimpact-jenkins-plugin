package com.loadimpact.jenkins_plugin.client;

import java.util.Arrays;
import java.util.List;

/**
* DESCRIPTION
*
* @author jens
* @date 2013-09-11, 09:23
*/
public enum Status {
    created, queued, initializing, running, finished, timed_out, 
    aborting_by_user, aborted_by_user, aborting_by_system, aborted_by_system, failed;

    private static List<Status> NOT_COMPLETED = Arrays.asList(created, queued, initializing, running);
    
    public boolean isProcessing() {
        return NOT_COMPLETED.contains(this);
    }

    public boolean isRunning() {
        return this == running;
    }
    
    public boolean isCompleted() {
        return !isProcessing();
    }
    
    public static Status valueOf(int status) {
        return values()[status + 1];
    }
}
