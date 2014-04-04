package com.loadimpact.jenkins_plugin.client;

import org.joda.time.format.ISODateTimeFormat;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Contains response data for a test-configuration.
 *
 * @author jens
 * @date 2013-09-11, 09:24
 */
public class TestConfiguration implements Serializable {

    public static class LoadSchedule {
        public final int duration;
        public final int users;

        public LoadSchedule(int duration, int users) {
            this.duration = duration;
            this.users = users;
        }

        @Override
        public String toString() {
            return "LoadSchedule{" +
                    "duration=" + duration +
                    ", users=" + users +
                    '}';
        }
    }
    
    public static class Track {
        public final String zone;
        public final int percent;
        public final int scenarioId;

        public Track(String zone, int percent, int scenarioId) {
            this.zone = zone;
            this.percent = percent;
            this.scenarioId = scenarioId;
        }

        @Override
        public String toString() {
            return "Track{" +
                    "percent=" + percent +
                    ", scenarioId=" + scenarioId +
                    ", zone='" + zone + '\'' +
                    '}';
        }
    }

    public final int id;
    public final String name;
    public final String url;
    public final Date date;
    public final UserType userType;
    public final List<LoadSchedule> schedules = new ArrayList<LoadSchedule>();
    public final List<Track> tracks = new ArrayList<Track>();

    @Deprecated
    public TestConfiguration(int id, String name, String url, String date) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.date = ISODateTimeFormat.dateTimeNoMillis().parseDateTime(date).toDate();
        this.userType = UserType.sbu;
    }


    public TestConfiguration(JsonObject json) {
        this.id = json.getInt("id");
        this.name = json.getString("name");
        this.date = ISODateTimeFormat.dateTimeNoMillis().parseDateTime(json.getString("updated")).toDate();
        this.url = json.getString("url");
        
        JsonObject configJson = json.getJsonObject("config");
        this.userType = UserType.valueOf(configJson.getString("user_type"));

        JsonArray schedulesJson = configJson.getJsonArray("load_schedule");
        for (int k = 0; k < schedulesJson.size(); ++k) {
            JsonObject s = schedulesJson.getJsonObject(k);
            schedules.add(new LoadSchedule(s.getInt("duration"), s.getInt("users")));
        }

        JsonArray tracksJson = configJson.getJsonArray("tracks");
        for (int k = 0; k < tracksJson.size(); ++k) {
            JsonObject trackJson = tracksJson.getJsonObject(k);
            JsonArray clipsJson = trackJson.getJsonArray("clips");
            for (int j = 0; j < clipsJson.size(); ++j) {
                JsonObject clipJson = clipsJson.getJsonObject(j);
                tracks.add(new Track(trackJson.getString("loadzone"), clipJson.getInt("percent"), clipJson.getInt("user_scenario_id")));
            }
        }
    }

    @Override
    public String toString() {
        return "TestConfiguration{" +
                "date=" + date +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", schedules=" + schedules +
                ", tracks=" + tracks +
                ", url='" + url + '\'' +
                ", userType=" + userType +
                '}';
    }
}
