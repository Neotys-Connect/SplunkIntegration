package com.neotys.splunk.DataModel.splunk;

import com.neotys.splunk.DataModel.NeoLoadEvents;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;

import static com.neotys.splunk.conf.Constants.*;

public class Event {
    //{
    //
    //	"time": 1595345018.000,
    //	"event": "neoload ERROR",
    //	"sourcetype": "neoload",
    //	"fields": {"device": "macbook", "users": ["joe", "bob"]}
    //
    //}

    long time;
    String event;
    String sourcetype;
    HashMap<String,String>  fields;

    public Event(long time, String event, String sourcetype) {
        this.time = time;
        this.event = event;
        this.sourcetype = sourcetype;
    }

    public Event(NeoLoadEvents events1) {
        time=events1.getEventTime();
        this.sourcetype=SOURCE_NEOLOAD+events1.getProjectname();
        this.event=events1.getCode();
        fields=new HashMap<>();
        fields.put("source",events1.getSource());
        fields.put("fullname",events1.getFullname());
        fields.put(AUTHOR,events1.getAuthor());
        fields.put(TEST_NAME,events1.getTestname());
        fields.put(TYPE , events1.getType());
        fields.put(SCENARIO,events1.getScenario());
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getSourcetype() {
        return sourcetype;
    }

    public void setSourcetype(String sourcetype) {
        this.sourcetype = sourcetype;
    }

    public HashMap<String, String> getFields() {
        return fields;
    }

    public void setFields(HashMap<String, String> fields) {
        this.fields = fields;
    }

    public io.vertx.core.json.JsonObject toJsonObjec()
    {
        io.vertx.core.json.JsonObject jsonObject=new io.vertx.core.json.JsonObject();
        jsonObject.put("time",getTime());
        jsonObject.put("sourcetype",getSourcetype());

        io.vertx.core.json.JsonObject jsonObjectFields=new JsonObject();

        getFields().forEach((s, s2) -> {
            jsonObjectFields.put(s,s2);
        });
        jsonObject.put("fields",jsonObjectFields);

        return jsonObject;

    }
}
