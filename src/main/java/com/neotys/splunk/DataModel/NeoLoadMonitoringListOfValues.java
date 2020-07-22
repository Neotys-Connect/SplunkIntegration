package com.neotys.splunk.DataModel;

import java.util.ArrayList;
import java.util.List;

public class NeoLoadMonitoringListOfValues {
    List<NeoLoadMonitoringValues> neoLoadMonitoringValuesList;

    public NeoLoadMonitoringListOfValues()
    {
        neoLoadMonitoringValuesList=new ArrayList<>();
    }

    public void addValues(NeoLoadMonitoringValues values)
    {
        neoLoadMonitoringValuesList.add(values);
    }

    public List<NeoLoadMonitoringValues> getNeoLoadMonitoringValuesList() {
        return neoLoadMonitoringValuesList;
    }

    public void setNeoLoadMonitoringValuesList(List<NeoLoadMonitoringValues> neoLoadMonitoringValuesList) {
        this.neoLoadMonitoringValuesList = neoLoadMonitoringValuesList;
    }
}
