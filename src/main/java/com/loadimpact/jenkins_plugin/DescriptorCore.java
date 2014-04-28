package com.loadimpact.jenkins_plugin;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.loadimpact.ApiTokenClient;
import com.loadimpact.jenkins_plugin.client.LoadImpactClient;
import com.loadimpact.eval.DelayUnit;
import com.loadimpact.resource.TestConfiguration;
import com.loadimpact.util.StringUtils;
import hudson.model.Item;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import org.kohsuke.stapler.QueryParameter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Common DESCRIPTOR parts of this plugin, used by both the build and post-build tasks.
 *
 * @author jens
 * @date 2013-10-21, 13:41
 */
public class DescriptorCore {
    private transient final Logger log;

    public DescriptorCore() {
        log = Logger.getLogger(getClass().getName());
    }

    private List<ApiTokenCredentials> getAllTokenCredentials() {
        return CredentialsProvider.lookupCredentials(ApiTokenCredentials.class, (Item) null, null, (DomainRequirement) null);
    }

    private ApiTokenCredentials getTokenCredentials(String id) {
        for (ApiTokenCredentials t : getAllTokenCredentials()) {
            if (t.getId().equals(id)) return t;
        }
        return null;
    }

    private String getApiToken(String id) {
        try {
            return getTokenCredentials(id).getApiToken().getPlainText();
        } catch (Exception e) {
            return null;
        }
    }

    public ListBoxModel doFillApiTokenIdItems() {
        log.info("FILL ApiTokenId");
        
        ListBoxModel items = new ListBoxModel();
        for (ApiTokenCredentials t : getAllTokenCredentials()) {
            String label = t.getDescription();
            if (StringUtils.isBlank(label)) {
                String txt = t.getApiToken().getPlainText();
                label = "API Token " + txt.substring(0, 12) + "..." + txt.substring(txt.length() - 4);
            }
            items.add(label, t.getId());
        }
        return items;
    }

    public FormValidation doCheckApiTokenId(@QueryParameter String value) {
        log.info("CHECK apiTokenId=" + value);

        if (getAllTokenCredentials().isEmpty()) {
            return FormValidation.error(Messages.DescriptorCore_MissingToken());
        }
        if (StringUtils.isBlank(value)) {
            return FormValidation.ok();
        }

        ApiTokenCredentials c = getTokenCredentials(value);
        String token = c.getApiToken().getPlainText();
        if (StringUtils.isBlank(token)) {
            return FormValidation.error(Messages.DescriptorCore_BlankToken());
        }
        if (token.length() != 64) {
            return FormValidation.error(Messages.DescriptorCore_WrongSize(value.length()));
        }
        if (!token.matches("[a-fA-F0-9]+")) {
            return FormValidation.error(Messages.DescriptorCore_NotHEX());
        }

        return FormValidation.ok();
    }    
    
    /**
     * Populates the load-test chooser drop-list of the job configuration.
     *
     * @return drop-list model
     */
    public ListBoxModel doFillLoadTestIdItems(@QueryParameter String apiTokenId) {
        log.info("FILL loadTestId: apiTokenId=" + apiTokenId);

        ListBoxModel items = new ListBoxModel();
        if (getAllTokenCredentials().isEmpty()) return items;
        
        String apiToken = getApiToken(apiTokenId);
        if (apiToken == null) return items;

        try {
//            LoadImpactClient client = new LoadImpactClient(apiToken);
            ApiTokenClient client = new ApiTokenClient(apiToken);
            
            List<TestConfiguration> testConfigurations = client.getTestConfigurations();
            for (TestConfiguration cfg : testConfigurations) {
                items.add(cfg.name, String.valueOf(cfg.id));
            }

//            Map<Integer, String> tests = client.getTestConfigurationLabels();
//            for (Map.Entry<Integer, String> t : tests.entrySet()) {
//                items.add(t.getValue(), t.getKey().toString());
//            }
        } catch (Exception e) {
            log.warning(String.format("Failed to retrieve test-configurations: %s", e));
        }
        return items;
    }

    public FormValidation doCheckLoadTestId(@QueryParameter String value) {
        log.info("CHECK loadTest=" + value);
        if (StringUtils.isBlank(value)) {
            return FormValidation.error(Messages.DescriptorCore_BlankLoadTestId());
        }
        return checkForPositiveNumber(value);
    }

    public FormValidation doCheckCriteriaDelayValue(@QueryParameter String value) {
        log.info("CHECK criteriaDelayValue=" + value);
        return checkForNonNegativeNumber(value);
    }

    public ListBoxModel doFillCriteriaDelayUnitItems() {
        log.info("FILL criteriaDelayUnit");
        ListBoxModel items = new ListBoxModel();
        for (DelayUnit unit : DelayUnit.values()) {
            items.add(unit.label, unit.name());
        }
        return items;
    }

    public FormValidation doCheckCriteriaDelayUnit(@QueryParameter String value) {
        log.info("CHECK criteriaDelayUnit=" + value);
        if (DelayUnit.names().contains(value)) {
            return FormValidation.ok();
        }
        return FormValidation.error("Invalid selection: " + value);
    }

    public FormValidation doCheckCriteriaQueueSize(@QueryParameter String value) {
        log.info("CHECK criteriaQueueSize=" + value);
        return checkForPositiveNumber(value);
    }

    public FormValidation doCheckAbortAtFailure(@QueryParameter String value) {
        log.info("CHECK abortAtFailure=" + value);
        return FormValidation.ok();
    }

    public FormValidation doCheckPollInterval(@QueryParameter String value) {
        log.info("CHECK pollInterval=" + value);
        return checkForPositiveNumber(value);
    }

    private FormValidation checkForPositiveNumber(String txt) {
        try {
            int value = Integer.parseInt(txt);
            if (value <= 0) return FormValidation.error(Messages.DescriptorCore_NotPositiveInteger());
        } catch (NumberFormatException e) {
            return FormValidation.error("Not an integer");
        }
        return FormValidation.ok();
    }

    private FormValidation checkForNonNegativeNumber(String txt) {
        try {
            int value = Integer.parseInt(txt);
            if (value < 0) return FormValidation.error(Messages.DescriptorCore_NotNonNegativeInteger());
        } catch (NumberFormatException e) {
            return FormValidation.error("Not an integer");
        }
        return FormValidation.ok();
    }
    
}
