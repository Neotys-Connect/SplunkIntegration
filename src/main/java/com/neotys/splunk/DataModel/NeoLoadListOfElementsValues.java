package com.neotys.splunk.DataModel;

import java.util.ArrayList;
import java.util.List;

public class NeoLoadListOfElementsValues {
    List<NeoLoadElementsValues> neoLoadListOfElementsValuesList;

    public NeoLoadListOfElementsValues() {
       neoLoadListOfElementsValuesList=new ArrayList<>();
    }

    public void addValue(NeoLoadElementsValues values)
    {
        neoLoadListOfElementsValuesList.add(values);

    }

    public List<NeoLoadElementsValues> getNeoLoadListOfElementsValuesList() {
        return neoLoadListOfElementsValuesList;
    }

    public void setNeoLoadListOfElementsValuesList(List<NeoLoadElementsValues> neoLoadListOfElementsValuesList) {
        this.neoLoadListOfElementsValuesList = neoLoadListOfElementsValuesList;
    }
}
