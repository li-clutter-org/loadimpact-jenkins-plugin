package com.loadimpact.jenkins_plugin.client;

/**
 * DESCRIPTION
 *
 * @author jens
 * @date 2013-10-01, 13:02
 */
public enum UserType {
    sbu("Simulated Browser User"), vu("Virtual User");

    public final String label;

    private UserType(String label) {
        this.label = label;
    }
    
}
