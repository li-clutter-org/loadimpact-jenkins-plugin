package com.loadimpact.jenkins_plugin;

import com.cloudbees.plugins.credentials.*;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;
import com.loadimpact.ApiTokenClient;
import com.loadimpact.util.StringUtils;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.util.FormValidation;
import hudson.util.Secret;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

/**
 * Credentials implementation for API Tokens.
 *
 * @author jens
 * @date 2013-10-21, 17:13
 */
@NameWith(value = ApiTokenCredentials.NameProvider.class, priority = 32)
public class ApiTokenCredentials extends BaseStandardCredentials implements Credentials {

    @NonNull
    private final Secret apiToken;

    @DataBoundConstructor
    public ApiTokenCredentials(@CheckForNull CredentialsScope scope, @CheckForNull String id, @CheckForNull String description, @CheckForNull String apiToken) {
        super(scope, id, description);
        this.apiToken = Secret.fromString(apiToken);
    }

    @NonNull
    public Secret getApiToken() {
        return apiToken;
    }

    @Override
    public String toString() {
        return "ApiToken{description=" + getDescription() 
                + ", token=" + getApiToken().getPlainText().substring(0,12) 
                + ", id="+getId()
                +"}";
    }

    @SuppressWarnings("UnusedDeclaration")
    @Extension
    public static class DescriptorImpl extends CredentialsDescriptor {
        @Override
        public String getDisplayName() {
            return Messages.ApiTokenCredentials_DisplayName();
        }

        public FormValidation doCheckApiToken(@QueryParameter String value) {
            if (StringUtils.isBlank(value)) {
                return FormValidation.error(Messages.ApiTokenCredentials_CannotBeBlank());
            }
            if (value.length() != 64) {
                return FormValidation.error(Messages.ApiTokenCredentials_WrongSize(value.length()));
            }
            if (!value.matches("[a-fA-F0-9]+")) {
                return FormValidation.error(Messages.ApiTokenCredentials_NotHEX());
            }
            return FormValidation.ok();
        }
    
        public FormValidation doValidateApiToken(@QueryParameter("apiToken") final String apiToken) {
            try {
                ApiTokenClient apiTokenClient = new ApiTokenClient(apiToken);
                if (apiTokenClient.isValidToken()) {
                    return FormValidation.ok(Messages.ApiTokenCredentials_TokenOK());
                }
                return FormValidation.error(Messages.ApiTokenCredentials_TokenNotOK());
            } catch (Exception e) {
                return FormValidation.error(Messages.ApiTokenCredentials_FailedWhenValidating(e));
            }
        }
    }

    public static class NameProvider extends CredentialsNameProvider<ApiTokenCredentials> {
        @NonNull
        @Override
        public String getName(@NonNull ApiTokenCredentials c) {
            String description = c.getDescription();
            if (!StringUtils.isBlank(description)) return description;
            return Messages.ApiTokenCredentials_ShortName();
        }
    }
    
}
