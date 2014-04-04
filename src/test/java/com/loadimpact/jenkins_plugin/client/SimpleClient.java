package com.loadimpact.jenkins_plugin.client;

import org.glassfish.jersey.client.filter.HttpBasicAuthFilter;
import org.joda.time.format.ISODateTimeFormat;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

/**
 * Simple manual test client.
 *
 * @author jens
 * @date 2013-09-08, 14:56
 */
public class SimpleClient {
    static final String apiKey = "4abe6379050c34ed3f996785edd29e7943c56588fe8b755baba20fa4718758f1"; 
    static final String baseUrl = "https://api.loadimpact.com/v2";

    public static void main(String[] args) throws Exception {
        Client client = ClientBuilder.newClient();
        client.register(new HttpBasicAuthFilter(apiKey, ""));

        WebTarget tests = client.target(baseUrl).path("tests");
        JsonArray response = tests.request(MediaType.APPLICATION_JSON).get(JsonArray.class);
        System.out.printf("Response: %s%n", response);

        for (int i = 0; i < response.size(); ++i) {
            JsonObject tst = response.getJsonObject(i);
            System.out.printf("Started: %s (%s)%n", tst.getString("started"), 
                    ISODateTimeFormat.dateTimeNoMillis().parseDateTime(tst.getString("started")).toDate());
            System.out.printf("Ended: %s (%s)%n", tst.getString("ended"),
                    ISODateTimeFormat.dateTimeNoMillis().parseDateTime(tst.getString("ended")).toDate());
            System.out.printf("Status: %s%n", tst.getString("status_text"));
            System.out.printf("Url: %s%n", tst.getString("public_url"));
        }
        
    }
    
    
}
