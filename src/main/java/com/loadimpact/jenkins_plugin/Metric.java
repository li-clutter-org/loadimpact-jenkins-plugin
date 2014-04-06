package com.loadimpact.jenkins_plugin;

import com.loadimpact.jenkins_plugin.client.ResultsCategory;

/**
 * Defines all metrics that can be used as failure criteria thresholds.
 *
 * @author jens
 * @date 2013-10-20, 22:25
 */
@Deprecated
public enum Metric {
    time("User load time", "ms", ResultsCategory.user_load_time),
    count("Requests per second", "*/s", ResultsCategory.requests_per_second),
    bandwidth("Bandwidth", "bit/s", ResultsCategory.bandwidth),
    failures("Failure Rate", "%", ResultsCategory.failure_rate);

    public final String          label;
    public final String          unit;
    public final ResultsCategory category;

    Metric(String label, String unit, ResultsCategory category) {
        this.label = label;
        this.unit = unit;
        this.category = category;
    }

}
