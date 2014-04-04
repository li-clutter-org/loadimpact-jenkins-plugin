package com.loadimpact.jenkins_plugin;

import com.loadimpact.jenkins_plugin.client.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * Units for the initial criteria delay.
 *
 * @author jens
 * @date 2013-10-21, 23:24
 */
public enum DelayUnit {
    seconds, users;

    public final String label;

    DelayUnit() {
        label = Util.toInitialCase(name());
    }
    
    public static List<String> names() {
        DelayUnit[] units = values();
        List<String> result = new ArrayList<String>(units.length);
        for (int i = 0; i < units.length; i++) {
            result.add(units[i].name());
        }
        return result;
    }
    
}
