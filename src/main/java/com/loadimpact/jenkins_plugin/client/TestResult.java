package com.loadimpact.jenkins_plugin.client;

import com.loadimpact.util.DateUtils;

import javax.json.JsonNumber;
import javax.json.JsonObject;
import java.io.Serializable;
import java.util.Date;

/**
 * Contains a test-status result JSON value.
 * REST: /tests/{id}/results?ids=category.param
 *
 * @author jens
 * @date 2013-09-11, 10:50
 */
public class TestResult implements Serializable, Comparable<TestResult> {
    public final  ResultsCategory category;
    public final  Date            timestamp;
    public final  int             offset;
    public final Number           value;

    public TestResult(ResultsCategory category, Date timestamp, int offset, Number value) {
        this.category  = category;
        this.timestamp = timestamp;
        this.offset    = offset;
        this.value     = value;
    }
    
    public TestResult(ResultsCategory category, JsonObject json) {
        this.category  = category;
        this.timestamp = DateUtils.toDateFromTimestamp((json.getJsonNumber("timestamp").longValue()));
        this.offset    = json.getInt("offset");
        JsonNumber   n = json.getJsonNumber(category.valueName);
        this.value     = n.isIntegral() ? n.longValue() : n.doubleValue();
    }

    @Override
    public String toString() {
        return "TestResult{" +
                "category=" + category +
                ", offset=" + offset +
                ", timestamp=" + timestamp +
                ", value=" + value.doubleValue() +
                '}';
    }

    public int compareTo(TestResult that) {
        return this.timestamp.compareTo(that.timestamp);
    }
    
}
