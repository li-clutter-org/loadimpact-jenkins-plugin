package com.loadimpact.jenkins_plugin;

import com.loadimpact.jenkins_plugin.client.TestResult;
import com.loadimpact.eval.Operator;
import com.loadimpact.eval.BoundedDroppingQueue;
import com.loadimpact.util.ListUtils;

import java.util.List;

/**
 * Computes if the aggregated value of the N last result values exceeds the given threshold. 
 *
 * @author jens
 * @date 2013-10-21, 11:57
 */
@Deprecated
public class Evaluator {
    private ThresholdView                 threshold;
    private BoundedDroppingQueue<Integer> values;
    private int                           lastOffset;
    private int                           aggregatedValue;

    /**
     * Creates an evaluator with the3 given threshold and using the last 'size' number of values.
     * @param threshold     the threshold
     * @param size          number of last values to bae the aggregation on
     */
    public Evaluator(ThresholdView threshold, int size) {
        this.threshold = threshold;
        this.values = new BoundedDroppingQueue<Integer>(size);
        this.lastOffset = -1;
        this.aggregatedValue = 0;
    }

    /**
     * Returns true if the threshold based on the latest aggregated value exceeds the given threshold value.
     * @param results   last batch of values
     * @return true if exceeded
     */
    @Deprecated
    public boolean isExceeded(List<TestResult> results) {
        if (results == null || results.isEmpty()) return false;

        for (TestResult r : results) {
            if (r.offset < lastOffset) continue;
            values.put(r.value.intValue());
            lastOffset = r.offset;
        }

        aggregatedValue = ListUtils.median(values.toList());
        return exceedsThreshold(aggregatedValue);
        
    }

    public Evaluator accumulate(List<TestResult> results) {
        if (results != null && results.size() > 0) {
            for (TestResult r : results) {
                if (r.offset < lastOffset) continue;
                values.put(r.value.intValue());
                lastOffset = r.offset;
            }
            aggregatedValue = ListUtils.median(values.toList());    
        }
        return this;
    }
    
    public boolean isExceeded() {
        return exceedsThreshold(getAggregatedValue());
    }

    /**
     * Checks if the aggregated value exceeds the threshold.
     * @param value     the value to compare
     * @return true if exceeded
     */
    private boolean exceedsThreshold(int value) {
        if (getThreshold().operator.equals(Operator.lessThan)) {
            return value < getThreshold().value;
        }
        if (getThreshold().operator.equals(Operator.greaterThan)) {
            return value > getThreshold().value;
        }
        return false;
    }

    public ThresholdView getThreshold() {
        return threshold;
    }

    public int getLastOffset() {
        return lastOffset;
    }

    public int getAggregatedValue() {
        return aggregatedValue;
    }
}
