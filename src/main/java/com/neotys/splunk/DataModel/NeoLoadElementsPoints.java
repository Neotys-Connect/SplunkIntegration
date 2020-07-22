package com.neotys.splunk.DataModel;

import com.neotys.ascode.swagger.client.model.ElementDefinition;
import com.neotys.ascode.swagger.client.model.Point;
import com.neotys.ascode.swagger.client.model.TestDefinition;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class NeoLoadElementsPoints extends NeoLoadElementData {
    private Long from ;

    private Long to ;

    private Double AVG_DURATION ;

    private Double MIN_DURATION ;

    private Double MAX_DURATION ;

    private Double COUNT ;

    private Double THROUGHPUT ;

    private Double ELEMENTS_PER_SECOND ;

    private Double ERRORS ;

    private Double ERRORS_PER_SECOND ;

    private Double ERROR_RATE ;

    private Double AVG_TTFB ;

    private Double MIN_TTFB ;

    private Double MAX_TTFB ;




    public NeoLoadElementsPoints(TestDefinition testDefinition, ElementDefinition elementDefinition, Point point)
    {
       this.initialize(testDefinition);
       this.initElement(elementDefinition);
       this.to=point.getTo();
       this.from=point.getFrom();
       this.AVG_DURATION= setValue(point.getAVGDURATION());
       this.AVG_TTFB= setValue(point.getAVGTTFB());
       this.COUNT= setValue(point.getCOUNT());
       this.ELEMENTS_PER_SECOND= setValue(point.getELEMENTSPERSECOND());
       this.ERROR_RATE= setValue(point.getERRORRATE());
       this.ERRORS= setValue(point.getERRORS());
       this.ERRORS_PER_SECOND= setValue(point.getERRORSPERSECOND());
       this.MAX_DURATION= setValue(point.getMAXDURATION());
       this.MIN_DURATION= setValue(point.getMINDURATION());
       this.MAX_TTFB= setValue(point.getMAXTTFB());
       this.MIN_TTFB= setValue(point.getMINTTFB());
       this.THROUGHPUT=setValue(point.getTHROUGHPUT());
       this.time= Instant.ofEpochMilli(testDefinition.getStartDate()+this.to);

    }

    public String toString()
    {
        StringBuilder result=new StringBuilder();
        result.append("Monitroing Points :");
        result.append("from:"+from);
        result.append(" to:"+to);
        result.append(" AVG:"+AVG_DURATION);
        result.append(" AVG_TTFB:"+AVG_TTFB);
        result.append(" COUNT:"+COUNT);
        result.append(" ELEMENTS_PER_SECOND:"+ELEMENTS_PER_SECOND);

        result.append(" ERROR_RATE:"+ERROR_RATE);
        result.append(" ERRORS_PER_SECOND:"+ERRORS_PER_SECOND);
        result.append(" MAX_DURATION:"+MAX_DURATION);
        result.append(" MIN_DURATION:"+MIN_DURATION);
        result.append(" MAX_TTFB:"+MAX_TTFB);
        result.append(" THROUGHPUT:"+THROUGHPUT);
        result.append(" author :"+this.author);
        result.append(" tesname :"+this.testname);
        result.append(" project :"+this.projectname);
        result.append(" scenario :"+this.scenario);
        result.append(" enddate :"+this.endDate);
        result.append(" startdate :"+this.startDate);
        result.append(" path :"+this.getPath());
        result.append(" metricname :"+this.getName());
        result.append(" time :"+this.time);
        return result.toString();
    }


    public Long getFrom() {
        return from;
    }

    public void setFrom(Long from) {
        this.from = from;
    }

    public Long getTo() {
        return to;
    }

    public void setTo(Long to) {
        this.to = to;
    }

    public Double getAVG_DURATION() {
        return AVG_DURATION;
    }

    public void setAVG_DURATION(Double AVG_DURATION) {
        this.AVG_DURATION = AVG_DURATION;
    }

    public Double getMIN_DURATION() {
        return MIN_DURATION;
    }

    public void setMIN_DURATION(Double MIN_DURATION) {
        this.MIN_DURATION = MIN_DURATION;
    }

    public Double getMAX_DURATION() {
        return MAX_DURATION;
    }

    public void setMAX_DURATION(Double MAX_DURATION) {
        this.MAX_DURATION = MAX_DURATION;
    }

    public Double getCOUNT() {
        return COUNT;
    }

    public void setCOUNT(Double COUNT) {
        this.COUNT = COUNT;
    }

    public Double getTHROUGHPUT() {
        return THROUGHPUT;
    }

    public void setTHROUGHPUT(Double THROUGHPUT) {
        this.THROUGHPUT = THROUGHPUT;
    }

    public Double getELEMENTS_PER_SECOND() {
        return ELEMENTS_PER_SECOND;
    }

    public void setELEMENTS_PER_SECOND(Double ELEMENTS_PER_SECOND) {
        this.ELEMENTS_PER_SECOND = ELEMENTS_PER_SECOND;
    }

    public Double getERRORS() {
        return ERRORS;
    }

    public void setERRORS(Double ERRORS) {
        this.ERRORS = ERRORS;
    }

    public Double getERRORS_PER_SECOND() {
        return ERRORS_PER_SECOND;
    }

    public void setERRORS_PER_SECOND(Double ERRORS_PER_SECOND) {
        this.ERRORS_PER_SECOND = ERRORS_PER_SECOND;
    }

    public Double getERROR_RATE() {
        return ERROR_RATE;
    }

    public void setERROR_RATE(Double ERROR_RATE) {
        this.ERROR_RATE = ERROR_RATE;
    }

    public Double getAVG_TTFB() {
        return AVG_TTFB;
    }

    public void setAVG_TTFB(Double AVG_TTFB) {
        this.AVG_TTFB = AVG_TTFB;
    }

    public Double getMIN_TTFB() {
        return MIN_TTFB;
    }

    public void setMIN_TTFB(Double MIN_TTFB) {
        this.MIN_TTFB = MIN_TTFB;
    }

    public Double getMAX_TTFB() {
        return MAX_TTFB;
    }

    public void setMAX_TTFB(Double MAX_TTFB) {
        this.MAX_TTFB = MAX_TTFB;
    }


}
