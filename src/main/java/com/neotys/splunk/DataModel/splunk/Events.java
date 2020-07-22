package com.neotys.splunk.DataModel.splunk;

import com.neotys.splunk.DataModel.NeoLoadListEvents;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class Events {
    List<Event> eventList;

    public List<Event> getEventList() {
        return eventList;
    }

    public void setEventList(List<Event> eventList) {
        this.eventList = eventList;
    }

    public Events(NeoLoadListEvents events)
    {
        eventList=new ArrayList<>();
        events.getNeoLoadEventsList().stream().forEach(events1 -> {
            eventList.add(new Event(events1));
        });
    }
    public String toJsonArray()
    {
        if(eventList.size()>1)
        {
            io.vertx.core.json.JsonArray array=new JsonArray();
            eventList.stream().forEach(metric -> {
                array.add(metric);

            });
            return array.toString();
        }
        else
        {
            JsonObject jsonObject=new JsonObject();
            jsonObject=eventList.get(0).toJsonObjec();

            return jsonObject.toString();
        }

    }
}
