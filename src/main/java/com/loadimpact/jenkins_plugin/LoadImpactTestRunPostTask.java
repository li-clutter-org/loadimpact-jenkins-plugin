package com.loadimpact.jenkins_plugin;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import com.loadimpact.eval.DelayUnit;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Runs a load test as a post-build task.
 * All task logic is performed by its {@link com.loadimpact.jenkins_plugin.LoadImpactCore} delegate.
 * 
 * @author jens
 * @date 2013-10-19, 09:30
 */
@SuppressWarnings("UnusedDeclaration")
public class LoadImpactTestRunPostTask extends Recorder {
    private LoadImpactCore _core;
    private transient Logger _log;


    @DataBoundConstructor
    public LoadImpactTestRunPostTask(String apiTokenId, int loadTestId, int criteriaDelayValue, String criteriaDelayUnit, int criteriaDelayQueueSize, boolean abortAtFailure, ThresholdView[] thresholdViews, int pollInterval, boolean logHttp, boolean logJson) {
        this._core = new LoadImpactCore(apiTokenId, loadTestId, criteriaDelayValue, criteriaDelayUnit, criteriaDelayQueueSize, abortAtFailure, thresholdViews, pollInterval, logHttp, logJson);
        log().info("properties: " + core().toString());
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        return core().perform(build, launcher, listener);
    }

    @Override
    public Action getProjectAction(AbstractProject<?, ?> project) {
        return core().getProjectAction(project);
    }

    public LoadTestHeader getLoadTestHeader() {
        return core().getLoadTestHeader();
    }

    private Logger log() {
        if (_log == null) {
            _log = Logger.getLogger(getClass().getName());
        }
        return _log;
    }

    private LoadImpactCore core() {
        if (_core == null) {
            _core = new LoadImpactCore();
        }
        return _core;
    }

    public String getApiTokenId() {
        return core().getApiTokenId();
    }

    public int getLoadTestId() {
        return core().getLoadTestId();
    }

    public int getCriteriaDelayValue() {
        return core().getCriteriaDelayValue();
    }

    public DelayUnit getCriteriaDelayUnit() {
        return core().getCriteriaDelayUnit();
    }

    public int getCriteriaDelayQueueSize() {
        return core().getCriteriaDelayQueueSize();
    }

    public boolean isAbortAtFailure() {
        return core().isAbortAtFailure();
    }

    public List<ThresholdView> getThresholds() {
        return core().getThresholds();
    }

    public int getPollInterval() {
        return core().getPollInterval();
    }

    public boolean isLogHttp() {
        return core().isLogHttp();
    }

    public boolean isLogJson() {
        return core().isLogJson();
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }
    

    @Override
    public BuildStepDescriptor getDescriptor() {
        return DESCRIPTOR;
    }

    @Extension
    public final static DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    /**
     * Descriptor for global configuration.
     * Contains also validators for all input-fields, including the job-configuration ones.
     * Accessed via: http://HOST:PORT/jenkins/configure
     */
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        private transient final Logger         log;
        private transient final DescriptorCore delegate;

        public DescriptorImpl() {
            log = Logger.getLogger(this.getClass().getName());
            delegate = new DescriptorCore();
        }

        public ListBoxModel doFillApiTokenIdItems() {
            return delegate.doFillApiTokenIdItems();
        }

        public FormValidation doCheckApiTokenId(@QueryParameter String value) {
            return delegate.doCheckApiTokenId(value);
        }
        
        public ListBoxModel doFillLoadTestIdItems(@QueryParameter String apiTokenId) {
            return delegate.doFillLoadTestIdItems(apiTokenId);
        }

        public FormValidation doCheckLoadTestId(@QueryParameter String value) {
            return delegate.doCheckLoadTestId(value);
        }

        public FormValidation doCheckCriteriaDelayValue(@QueryParameter String value) {
            return delegate.doCheckCriteriaDelayValue(value);
        }

        public ListBoxModel doFillCriteriaDelayUnitItems() {
            return delegate.doFillCriteriaDelayUnitItems();
        }

        public FormValidation doCheckCriteriaDelayUnit(@QueryParameter String value) {
            return delegate.doCheckCriteriaDelayUnit(value);
        }

        public FormValidation doCheckCriteriaQueueSize(@QueryParameter String value) {
            return delegate.doCheckCriteriaQueueSize(value);
        }

        public FormValidation doCheckAbortAtFailure(@QueryParameter String value) {
            return delegate.doCheckAbortAtFailure(value);
        }

        public FormValidation doCheckPollInterval(@QueryParameter String value) {
            return delegate.doCheckPollInterval(value);
        }

        @Override
        public String getDisplayName() {
            return Messages.LoadImpactCore_DisplayName();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }
    }

}
