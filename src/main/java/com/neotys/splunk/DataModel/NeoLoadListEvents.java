package com.neotys.splunk.DataModel;

import java.util.ArrayList;
import java.util.List;

public class NeoLoadListEvents {
    List<NeoLoadEvents> neoLoadEventsList;

    public NeoLoadListEvents()
    {
        neoLoadEventsList=new ArrayList<>();
    }

    public List<NeoLoadEvents> getNeoLoadEventsList() {
        return neoLoadEventsList;
    }

    public void setNeoLoadEventsList(List<NeoLoadEvents> neoLoadEventsList) {
        this.neoLoadEventsList = neoLoadEventsList;
    }

    public void addevent(NeoLoadEvents events)
    {
        neoLoadEventsList.add(events);
    }
}
