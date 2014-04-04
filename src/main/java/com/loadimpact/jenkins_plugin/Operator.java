package com.loadimpact.jenkins_plugin;

/**
 * Defines all operators that can be used in failure criteria thresholds.
 *
 * @author jens
 * @date 2013-10-20, 21:44
 */
public enum Operator {
    greaterThan(">"), lessThan("<");
    
    public final String symbol;

    Operator(String symbol) {
        this.symbol = symbol;
    }
    
}
