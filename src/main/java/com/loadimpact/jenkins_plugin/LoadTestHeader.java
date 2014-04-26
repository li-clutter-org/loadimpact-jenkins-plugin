package com.loadimpact.jenkins_plugin;

import com.loadimpact.util.StringUtils;
import hudson.model.ProminentProjectAction;
import org.joda.time.format.ISODateTimeFormat;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Populates the job page with a description of the load-test.
 *
 * @author jens
 * @date 2013-09-12, 07:50
 */
public class LoadTestHeader implements ProminentProjectAction {
    public static class Zone implements Serializable {
        public String name;
        public int percentage;

        public Zone(String name, int percentage) {
            this.name = name;
            this.percentage = percentage;
        }
    }
    
    
    private String id, name, targetUrl, date, userType;
    private int durationInMinutes, numClients;
    private Date       lastUpdated;
    private List<Zone> zones;

    @DataBoundConstructor
    public LoadTestHeader(int id, String name, Date date, String targetUrl, int duration, int clients, String userType, List<Zone> zones) {
        this.id = String.valueOf(id);
        this.name = name;
        this.lastUpdated = date;
        this.targetUrl = targetUrl;
        this.durationInMinutes = duration;
        this.numClients = clients;
        this.userType = userType;
        this.zones = zones;
    }

    public LoadTestHeader() {
        this.id = null; // no API token
    }

    public boolean isHasApiToken() {
        return !isUndefinedApiToken();
    }

    public boolean isUndefinedApiToken() {
        return StringUtils.isBlank(id);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public String getTestConfigurationUrl() {
        return "https://loadimpact.com/test/config/edit/" + getId();
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public String getDate() {
        return ISODateTimeFormat.date().print(getLastUpdated().getTime()) + " " + ISODateTimeFormat.timeNoMillis().print(getLastUpdated().getTime());
    }

    public int getDurationInMinutes() {
        return durationInMinutes;
    }

    public String getDuration() {
        int minutes = getDurationInMinutes();
        return String.format("%d minute" + (minutes != 1 ? "s" : ""), minutes);
    }

    public int getNumClients() {
        return numClients;
    }

    public String getClients() {
        return String.format("Max %d clients", getNumClients());
    }

    public String getUserType() {
        return userType;
    }

    public List<Zone> getZones() {
        return zones;
    }

    public String getDisplayName() {
        return "Load Test";
    }

    public String getUrlName() {
        return "loadTestHeader";
    }

    public String getIconFileName() {
        return null;
    }

    public String getLogo() {
        return LoadImpactCore.imagePath("loadimpact-full-logo-300x50.png");
    }

    public String getPluginName() {
        return LoadImpactCore.getPluginName();
    }
    
}
