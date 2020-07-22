package com.neotys.splunk.DataModel;

import com.neotys.ascode.swagger.client.model.CounterDefinition;


import java.util.stream.Collectors;

public class NeoLoadMonitoringData extends  NeoLoadCommonData {


    private String name ;


    private String path ;

    public void init_monitoringdata(CounterDefinition definition)
    {
        this.name=definition.getName();
        if(definition.getPath()!=null)
            this.path=definition.getPath().stream().collect(Collectors.joining("."));
        else
            this.path="";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
