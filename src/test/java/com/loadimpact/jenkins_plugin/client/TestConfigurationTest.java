package com.loadimpact.jenkins_plugin.client;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.InputStream;
import java.io.Reader;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;


/**
 * DESCRIPTION
 *
 * @author jens
 * @date 2013-10-01, 22:12
 */
public class TestConfigurationTest {
    private JsonObject json;

    @Before
    public void setUp() throws Exception {
        InputStream in = this.getClass().getResourceAsStream("single-test-config.json");
        assertNotNull("Test input not found", in);
        JsonReader jsonReader = Json.createReader(in);
        json = jsonReader.readObject();
        assertNotNull("Failed to parse JSON object", json);
    }

    @Test
    public void getting_few_json_properties_should_pass() {
        assertThat(json.getInt("id"), is(83959));
        assertThat(json.getString("name"), is("Name of resource"));
        assertThat(json.getString("url"), is("http://www.example.com/"));
    }

    @Test
    public void getting_few_properties_from_target_should_pass() {
        TestConfiguration target = new TestConfiguration(json);
        assertThat(target.id, is(83959));
        assertThat(target.name, is("Name of resource"));
        assertThat(target.url, is("http://www.example.com/"));
        assertThat(target.userType, is(UserType.sbu));
    }

    @Test
    public void getting_schedules_should_pass() {
        TestConfiguration target = new TestConfiguration(json);
        assertThat("schedules.size", target.schedules.size(), is(1));
        assertThat("schedules[0].duration", target.schedules.get(0).duration, is(10));
        assertThat("schedules[0].users", target.schedules.get(0).users, is(50));
    }
    
    @Test
    public void getting_tracks_should_pass() {
        TestConfiguration target = new TestConfiguration(json);
        assertThat("schedules.size", target.tracks.size(), is(3));
        
        assertThat("tracks[0].zone", target.tracks.get(0).zone, is("amazon:us:ashburn"));
        assertThat("tracks[0].percent", target.tracks.get(0).percent, is(50));

        assertThat("tracks[1].zone", target.tracks.get(1).zone, is("amazon:us:palo-alto"));
        assertThat("tracks[1].percent", target.tracks.get(1).percent, is(25));
        
        assertThat("tracks[2].zone", target.tracks.get(2).zone, is("amazon:us:palo-alto"));
        assertThat("tracks[2].percent", target.tracks.get(2).percent, is(25));
    }
    
    
    
}
