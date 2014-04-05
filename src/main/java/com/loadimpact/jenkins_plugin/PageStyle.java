package com.loadimpact.jenkins_plugin;

import hudson.Extension;
import hudson.model.PageDecorator;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Extension that allows us to squeeze in our own CSS.
 *
 * @author jens
 * @date 2013-09-30, 21:02
 */
@Extension
public class PageStyle extends PageDecorator {

    @Override
    public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
        return true;
    }

//    public String getPluginName() {
//        return LoadImpactCore.PLUGIN_NAME;
//    }

    public String getStyle() {
        return LoadImpactCore.cssPath("style.css");
    }
    
}
