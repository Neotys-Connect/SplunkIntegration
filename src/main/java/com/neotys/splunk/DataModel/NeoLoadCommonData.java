package com.neotys.splunk.DataModel;

import com.neotys.ascode.swagger.client.model.TestDefinition;


import java.time.Instant;

public class NeoLoadCommonData {
    String testname;
    String projectname;
    String scenario;
    String author;
    Long startDate;
    Long endDate;
    Instant time;
    String testid;

    public void initialize(TestDefinition definition)
    {
        this.testid=definition.getId();
        this.author=definition.getAuthor();
        if(definition.getEndDate()!=null)
            this.endDate=definition.getEndDate();
        else
            this.endDate= Long.valueOf(0);

        this.testname=definition.getName();
        this.projectname=definition.getProject();
        this.scenario=definition.getScenario();
        this.startDate=definition.getStartDate();
    }

    public Double setValue(Float value)
    {
        if(value!=null)
            return Double.valueOf(value);
        else
            return Double.valueOf(0);
    }

    public Long getEventTime() {
        return time.toEpochMilli();
    }

    public void setTime(Instant time) {
        this.time = time;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Long getStartDate() {
        return startDate;
    }

    public void setStartDate(Long startDate) {
        this.startDate = startDate;
    }

    public Long getEndDate() {
        return endDate;
    }

    public void setEndDate(Long endDate) {
        this.endDate = endDate;
    }

    public String getTestname() {
        return testname;
    }

    public void setTestname(String testname) {
        this.testname = testname;
    }

    public String getProjectname() {
        return projectname;
    }

    public void setProjectname(String projectname) {
        this.projectname = projectname;
    }

    public String getScenario() {
        return scenario;
    }

    public void setScenario(String scenario) {
        this.scenario = scenario;
    }
}
