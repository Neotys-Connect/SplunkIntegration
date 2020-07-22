package com.neotys.splunk.DataModel.splunk;

import com.google.gson.JsonElement;
import io.vertx.core.json.JsonObject;

public class Metric {
    //{
    //	"time": 1594311946.000,
    //	"source":"disk",
    //	"host":"host_99",
    //	"fields":
    //		{
    //				"region":"us-west-1",
    //				"datacenter":"us-west-1a",
    //				"rack":"63",
    //				"os":"Ubuntu16.10",
    //				"arch":"x64",
    //				"team":"LON",
    //				"service":"6",
    //				"service_version":"0",
    //				"service_environment":"test",
    //				"path":"/dev/sda1",
    //				"fstype":"ext3",
    //				"_value":1099511627776,
    //				"metric_name": "test1.test2.ded.total"
    //
    //		}
    //
    //},
    Long time;
    String source;
    String host;
    Fields fields;

    public Metric(Long time, String source, String host) {
        this.time = time;
        this.source = source;
        this.host = host;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Fields getFields() {
        return fields;
    }

    public void setFields(Fields fields) {
        this.fields = fields;
    }

    public io.vertx.core.json.JsonObject toJsonObjec()
    {
        io.vertx.core.json.JsonObject jsonObject=new io.vertx.core.json.JsonObject();
        jsonObject.put("time",getTime());
        jsonObject.put("source",getSource());
        jsonObject.put("host",getHost());

        io.vertx.core.json.JsonObject jsonObjectFields=new JsonObject();
        jsonObjectFields.put("_value",getFields().get_value());
        jsonObjectFields.put("metric_name",getFields().metric_name);
        getFields().getFields().forEach((s, s2) -> {
            jsonObjectFields.put(s,s2);
        });
        jsonObject.put("fields",jsonObjectFields);

        return jsonObject;

    }
}
