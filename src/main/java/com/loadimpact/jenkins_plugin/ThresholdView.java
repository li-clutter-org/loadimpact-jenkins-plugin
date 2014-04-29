package com.loadimpact.jenkins_plugin;

import com.loadimpact.eval.LoadTestResult;
import com.loadimpact.resource.testresult.StandardMetricResult;
import com.loadimpact.util.StringUtils;
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
    public StandardMetricResult.Metrics metric;
    public final Operator operator;
    public final Integer  value;
    public LoadTestResult result;

    @DataBoundConstructor
    public ThresholdView(String metric, String operator, Integer value, String result) {
        this.metric   = asMetric(metric, StandardMetricResult.Metrics.USER_LOAD_TIME);
        this.operator = asOperator(operator, Operator.lessThan);
        this.value    = (value != null) ? value : 0;
        this.result   = asResult(result, LoadTestResult.unstable);
    }

    private StandardMetricResult.Metrics asMetric(String name, StandardMetricResult.Metrics defVal) {
        if (StringUtils.isBlank(name)) return defVal;
        try {
            return StandardMetricResult.Metrics.valueOf(name.toUpperCase());
        } catch (Exception e) {
            return defVal;
        }
    }

    private Operator asOperator(String name, Operator defVal) {
        if (StringUtils.isBlank(name)) return defVal;
        try {
            return Operator.valueOf(name);
        } catch (Exception e) {
            return defVal;
        }
    }

    private LoadTestResult asResult(String name, LoadTestResult defVal) {
        if (StringUtils.isBlank(name)) return defVal;
        try {
            return LoadTestResult.valueOf(name.toLowerCase());
        } catch (Exception e) {
            return defVal;
        }
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


    private static class MetricDescriptor {
        public StandardMetricResult.Metrics metric;
        public String                       label;
        public String                       unit;

        private MetricDescriptor(StandardMetricResult.Metrics metric, String label, String unit) {
            this.metric = metric;
            this.label = label;
            this.unit = unit;
        }
    }
    
    private static MetricDescriptor[] METRICS = {
            new MetricDescriptor(StandardMetricResult.Metrics.ACCUMULATED_LOAD_TIME     , "Accumulated Load Time"     , "ms"),
            new MetricDescriptor(StandardMetricResult.Metrics.BANDWIDTH                 , "Bandwidth"                 , "bits/s"),
            new MetricDescriptor(StandardMetricResult.Metrics.CONNECTIONS_ACTIVE        , "Connections Active"        , "*"),
            new MetricDescriptor(StandardMetricResult.Metrics.FAILURE_RATE              , "Failure Rate"              , "%"),
            new MetricDescriptor(StandardMetricResult.Metrics.LOADGEN_CPU_UTILIZATION   , "LoadGen CPU Utilization"   , "%"),
            new MetricDescriptor(StandardMetricResult.Metrics.LOADGEN_MEMORY_UTILIZATION, "LoadGen Memory Utilization", "%"),
            new MetricDescriptor(StandardMetricResult.Metrics.REPS_FAILED_PERCENT       , "Repetitions Failed"        , "%"),
            new MetricDescriptor(StandardMetricResult.Metrics.REPS_SUCCEEDED_PERCENT    , "Repetitions Succeeded"     , "%"),
            new MetricDescriptor(StandardMetricResult.Metrics.REQUESTS_PER_SECOND       , "Requests per Seconds"      , "*/s"),
            new MetricDescriptor(StandardMetricResult.Metrics.TOTAL_RX_BYTES            , "Total Data Received"       , "bytes"),
            new MetricDescriptor(StandardMetricResult.Metrics.TOTAL_REQUESTS            , "Total HTTP Requests"       , "*"),
            new MetricDescriptor(StandardMetricResult.Metrics.USER_LOAD_TIME            , "User Load Time"            , "ms")
    };

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
            for (MetricDescriptor md : METRICS) {
                items.add(md.label, md.metric.name());
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
            for (MetricDescriptor md : METRICS) {
                if (md.metric.name().equals(metric)) {
                    items.add(md.unit);
                }
            }
            return items;
        }

        public ListBoxModel doFillResultItems() {
            log.info("FILL result");
            ListBoxModel items = new ListBoxModel();
            for (LoadTestResult r : Arrays.asList(LoadTestResult.unstable, LoadTestResult.failed)) {
                items.add(r.toString());
            }
            return items;
        }

    }

}
