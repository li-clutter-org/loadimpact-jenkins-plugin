package com.loadimpact.jenkins_plugin.client;

import java.io.Serializable;
import java.util.Date;

/**
* DESCRIPTION
*
* @author jens
* @date 2013-09-11, 09:24
*/
public class TestInstance implements Serializable {
    public final int id;
    public final String name, targetUrl, resultUrl;
    public final Date started, ended;
    public final Status status;
    public final String error;

    public TestInstance(int id, String name, String targetUrl, String started, String ended, int status, String resultUrl) {
        this.id = id;
        this.name = name;
        this.targetUrl = targetUrl;
        this.resultUrl = resultUrl;
        this.started = Util.toDateFromIso8601(started);
        this.ended = Util.toDateFromIso8601(ended);
        this.status = Status.valueOf(status);
        this.error = null;
    }
    
    public TestInstance(int id, String name, Status status, String error) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.error = error;
        this.resultUrl = this.targetUrl = null;
        this.started = this.ended = new Date();
    }

    @Override
    public String toString() {
        return "TestResult{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", started=" + started +
                ", ended=" + ended +
                ", targetUrl='" + targetUrl + '\'' +
                ", resultUrl='" + resultUrl + '\'' +
                ", error='" + error + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TestInstance that = (TestInstance) o;

        if (id != that.id) return false;
        if (!ended.equals(that.ended)) return false;
        if (!error.equals(that.error)) return false;
        if (!name.equals(that.name)) return false;
        if (!resultUrl.equals(that.resultUrl)) return false;
        if (!started.equals(that.started)) return false;
        if (status != that.status) return false;
        if (!targetUrl.equals(that.targetUrl)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + name.hashCode();
        result = 31 * result + targetUrl.hashCode();
        result = 31 * result + resultUrl.hashCode();
        result = 31 * result + started.hashCode();
        result = 31 * result + ended.hashCode();
        result = 31 * result + status.hashCode();
        result = 31 * result + error.hashCode();
        return result;
    }
}
