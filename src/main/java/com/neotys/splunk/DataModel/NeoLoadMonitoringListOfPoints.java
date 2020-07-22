package com.neotys.splunk.DataModel;

import java.util.ArrayList;
import java.util.List;

public class NeoLoadMonitoringListOfPoints {
    List<NeoLoadMonitoringPoints> neoLoadMonitoringPointsList;

    public NeoLoadMonitoringListOfPoints()
    {
        neoLoadMonitoringPointsList=new ArrayList<>();
    }

    public void addPoints(NeoLoadMonitoringPoints points)
    {
        neoLoadMonitoringPointsList.add(points);
    }

    public List<NeoLoadMonitoringPoints> getNeoLoadMonitoringPointsList() {
        return neoLoadMonitoringPointsList;
    }

    public void setNeoLoadMonitoringPointsList(List<NeoLoadMonitoringPoints> neoLoadMonitoringPointsList) {
        this.neoLoadMonitoringPointsList = neoLoadMonitoringPointsList;
    }
}
