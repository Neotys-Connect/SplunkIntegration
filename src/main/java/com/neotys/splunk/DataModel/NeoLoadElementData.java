package com.neotys.splunk.DataModel;


import com.neotys.ascode.swagger.client.model.ElementDefinition;


import java.util.stream.Collectors;

public class NeoLoadElementData extends NeoLoadCommonData {
    private String name = "";

    private String path ="";

    private String type = "";

    public void initElement(ElementDefinition elementDefinition)
    {
        this.name=elementDefinition.getName();

        if(elementDefinition.getPath()!=null)
            this.path=elementDefinition.getPath().stream().collect(Collectors.joining("."));
        else
            this.path="";

        this.type=elementDefinition.getType();
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
