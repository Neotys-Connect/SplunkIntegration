package com.neotys.splunk.DataModel;

import java.util.ArrayList;
import java.util.List;

public class NeoLoadListOfElementPoints {
    List<NeoLoadElementsPoints> neoLoadElementsPoints;

    public NeoLoadListOfElementPoints() {
        neoLoadElementsPoints=new ArrayList<>();

    }

    public void addPointst(NeoLoadElementsPoints points)
    {
        neoLoadElementsPoints.add(points);
    }

    public List<NeoLoadElementsPoints> getNeoLoadElementsPoints() {
        return neoLoadElementsPoints;
    }

    public void setNeoLoadElementsPoints(List<NeoLoadElementsPoints> neoLoadElementsPoints) {
        this.neoLoadElementsPoints = neoLoadElementsPoints;
    }
}
