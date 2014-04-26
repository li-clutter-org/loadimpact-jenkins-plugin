package com.loadimpact.jenkins_plugin;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Result;
import hudson.util.ListBoxModel;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import com.loadimpact.eval.Operator;
import java.util.Arrays;
import java.util.logging.Logger;

/**
 * Represents a single failure criteria threshold.
 *
 * @author jens
 * @date 2013-10-21, 08:19
 */
public class ThresholdView extends AbstractDescribableImpl<ThresholdView> {
    public final Metric   metric;
    public final Operator operator;
    public final Integer  value;
    public final Result   result;

    @DataBoundConstructor
    public ThresholdView(String metric, String operator, Integer value, String result) {
        this.metric = Metric.valueOf(metric != null ? metric : Metric.time.name());
        this.operator = Operator.valueOf(operator != null ? operator : Operator.lessThan.name());
        this.value = value != null ? value : 0;
        this.result = Result.fromString(result != null ? result : Result.UNSTABLE.toString());
    }

    @Override
    public String toString() {
        return "Threshold{" +
                "metric='" + metric + '\'' +
                ", operator=" + operator +
                ", result=" + result +
                ", value=" + value +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ThresholdView threshold = (ThresholdView) o;

        if (!metric.equals(threshold.metric)) return false;
        if (operator != threshold.operator) return false;
        if (!result.equals(threshold.result)) return false;
        if (!value.equals(threshold.value)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result1 = metric.hashCode();
        result1 = 31 * result1 + operator.hashCode();
        result1 = 31 * result1 + value.hashCode();
        result1 = 31 * result1 + result.hashCode();
        return result1;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<ThresholdView> {
        private final Logger log;

        public DescriptorImpl() {
            this.log = Logger.getLogger(getClass().getName());
        }

        @Override
        public String getDisplayName() {
            return "Threshold";
        }

        public ListBoxModel doFillMetricItems() {
            log.info("FILL metric");
            ListBoxModel items = new ListBoxModel();
            for (Metric m : Metric.values()) {
                items.add(m.label, m.name());
            }
            return items;
        }

        public ListBoxModel doFillOperatorItems() {
            log.info("FILL operator");
            ListBoxModel items = new ListBoxModel();
            for (Operator op : Operator.values()) {
                items.add(op.symbol, op.name());
            }
            return items;
        }

        public ListBoxModel doFillUnitItems(@QueryParameter String metric) {
            log.info("FILL unit: metric=" + metric);
            ListBoxModel items = new ListBoxModel();
            for (Metric m : Metric.values()) {
                if (m.name().equals(metric)) {
                    items.add(m.unit);
                }
            }
            return items;
        }

        public ListBoxModel doFillResultItems() {
            log.info("FILL result");
            ListBoxModel items = new ListBoxModel();
            for (Result r : Arrays.asList(Result.UNSTABLE, Result.FAILURE)) {
                items.add(r.toString());
            }
            return items;
        }

    }

}
