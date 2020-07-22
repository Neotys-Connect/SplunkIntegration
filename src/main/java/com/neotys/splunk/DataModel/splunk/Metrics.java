package com.neotys.splunk.DataModel.splunk;

import com.neotys.splunk.DataModel.*;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.neotys.splunk.conf.Constants.*;

public class Metrics {
    List<Metric> metricList;

    public Metrics(NeoLoadListOfElementPoints listOfElementPoints)
    {
        metricList=new ArrayList<>();
        listOfElementPoints.getNeoLoadElementsPoints().forEach(points -> {
            Metric metric=new Metric(points.getEventTime(),SOURCE_NEOLOAD+points.getProjectname(),points.getScenario());
            HashMap<String,String> standartfields=new HashMap<>();
            standartfields.put(PATH,points.getPath());
            standartfields.put(TYPE,points.getType());
            standartfields.put(TEST_NAME,points.getTestname());
            standartfields.put(AUTHOR,points.getAuthor());
            standartfields.put(SCENARIO,points.getScenario());
            metricList.addAll(getFields(standartfields,points,metric));
        });
    }

    private List<Metric> getFields(HashMap<String,String> standardFileds,NeoLoadElementsPoints points,Metric reference)
    {
        List<Metric> metricListpoints=new ArrayList<>();
        HashMap<String,String> field=new HashMap<>();
        field.putAll(standardFileds);
        //----element per second
        Fields fields=new Fields(points.getPath()+"."+ELEMENTPERSECOND,points.getELEMENTS_PER_SECOND());
        fields.setFields(field);
        Metric metric=reference;
        metric.setFields(fields);
        metricListpoints.add(metric);
        //---avg
        fields=new Fields(points.getPath()+"."+AVG,points.getAVG_DURATION());
        fields.setFields(field);
        metric=reference;
        metric.setFields(fields);
        metricListpoints.add(metric);

        //---avg
        fields=new Fields(points.getPath()+"."+AVG_TTFB,points.getAVG_TTFB());
        fields.setFields(field);
        metric=reference;
        metric.setFields(fields);
        metricListpoints.add(metric);

        //---error rate
        fields=new Fields(points.getPath()+"."+ERROR_RATE,points.getERROR_RATE());
        fields.setFields(field);
        metric=reference;
        metric.setFields(fields);
        metricListpoints.add(metric);

        //---error /s
        fields=new Fields(points.getPath()+"."+ERRORS_PER_SECOND,points.getERRORS_PER_SECOND());
        fields.setFields(field);
        metric=reference;
        metric.setFields(fields);
        metricListpoints.add(metric);

        //---MAX
        fields=new Fields(points.getPath()+"."+MAX_DURATION,points.getMAX_DURATION());
        fields.setFields(field);
        metric=reference;
        metric.setFields(fields);
        metricListpoints.add(metric);
        //---MIN
        fields=new Fields(points.getPath()+"."+MIN_DURATION,points.getMIN_DURATION());
        fields.setFields(field);
        metric=reference;
        metric.setFields(fields);
        metricListpoints.add(metric);

        return metricListpoints;
    }
    public Metrics(NeoLoadMonitoringListOfPoints monitoringListOfPoints)
    {
        metricList=new ArrayList<>();
        monitoringListOfPoints.getNeoLoadMonitoringPointsList().stream().forEach(points -> {
            Metric metric=new Metric(points.getEventTime(),SOURCE_NEOLOAD+points.getProjectname(),points.getScenario());
            HashMap<String,String> standartfields=new HashMap<>();
            standartfields.put(PATH,points.getPath());
            standartfields.put(TEST_NAME,points.getTestname());
            standartfields.put(AUTHOR,points.getAuthor());
            standartfields.put(SCENARIO,points.getScenario());
            Fields fields=new Fields(points.getPath()+"."+AVG,points.getAVG());
            fields.setFields(standartfields);

            metric.setFields(fields);

            metricList.add(metric);
        });
    }

    public Metrics(NeoLoadListOfElementsValues neoLoadListOfElementsValues)
    {
        metricList=new ArrayList<>();
        neoLoadListOfElementsValues.getNeoLoadListOfElementsValuesList().stream().forEach(values -> {
            Metric metric=new Metric(values.getEventTime(),SOURCE_NEOLOAD+values.getProjectname(),values.getScenario());
            HashMap<String,String> standartfields=new HashMap<>();
            standartfields.put(PATH,values.getPath());
            standartfields.put(TEST_NAME,values.getTestname());
            standartfields.put(AUTHOR,values.getAuthor());
            standartfields.put(SCENARIO,values.getScenario());

            metricList.addAll(getFields(standartfields,values,metric));
        });
    }
    private List<Metric> getFields(HashMap<String,String> standardFileds,NeoLoadElementsValues points,Metric reference)
    {
        List<Metric> metricListpoints=new ArrayList<>();
        HashMap<String,String> field=new HashMap<>();
        field.putAll(standardFileds);
        //----element per second
        Fields fields=new Fields(points.getPath()+"."+ELEMENTPERSECOND,points.getElementPerSecond());
        fields.setFields(field);
        Metric metric=reference;
        metric.setFields(fields);
        metricListpoints.add(metric);
        //---avg
        fields=new Fields(points.getPath()+"."+AVG,points.getAvgDuration());
        fields.setFields(field);
        metric=reference;
        metric.setFields(fields);
        metricListpoints.add(metric);

        //---avg
        fields=new Fields(points.getPath()+"."+AVG_TTFB,points.getAvgTTFB());
        fields.setFields(field);
        metric=reference;
        metric.setFields(fields);
        metricListpoints.add(metric);

        //---error rate
        fields=new Fields(points.getPath()+"."+DOWNLOADEDBYTESPERSECOND,points.getDownloadedBytesPerSecond());
        fields.setFields(field);
        metric=reference;
        metric.setFields(fields);
        metricListpoints.add(metric);

        //---error /s
        fields=new Fields(points.getPath()+"."+FAILUREPERSECOND,points.getFailurePerSecond());
        fields.setFields(field);
        metric=reference;
        metric.setFields(fields);
        metricListpoints.add(metric);

        //---MAX
        fields=new Fields(points.getPath()+"."+FAILURERATE,points.getFailureRate());
        fields.setFields(field);
        metric=reference;
        metric.setFields(fields);
        metricListpoints.add(metric);

        if(points.getType().equalsIgnoreCase("TRANSACTION")) {
            fields=new Fields(points.getPath()+"."+PERCENTILE50,points.getPercentile50());
            fields.setFields(field);
            metric=reference;
            metric.setFields(fields);
            metricListpoints.add(metric);

            fields=new Fields(points.getPath()+"."+PERCENTILE90,points.getPercentile90());
            fields.setFields(field);
            metric=reference;
            metric.setFields(fields);
            metricListpoints.add(metric);

            fields=new Fields(points.getPath()+"."+PERCENTILE95,points.getPercentile95());
            fields.setFields(field);
            metric=reference;
            metric.setFields(fields);
            metricListpoints.add(metric);

            fields=new Fields(points.getPath()+"."+PERCENTILE99,points.getPercentile99());
            fields.setFields(field);
            metric=reference;
            metric.setFields(fields);
            metricListpoints.add(metric);
        }

        return metricListpoints;
    }

    public Metrics(NeoLoadMonitoringListOfValues monitoringListOfValues)
    {
        metricList=new ArrayList<>();
        monitoringListOfValues.getNeoLoadMonitoringValuesList().stream().forEach(values -> {
            Metric metric=new Metric(values.getEventTime(),SOURCE_NEOLOAD+values.getProjectname(),values.getScenario());
            HashMap<String,String> standartfields=new HashMap<>();
            standartfields.put(PATH,values.getPath());
            standartfields.put(TEST_NAME,values.getTestname());
            standartfields.put(AUTHOR,values.getAuthor());
            standartfields.put(SCENARIO,values.getScenario());
            Fields fields=new Fields(values.getPath()+"."+AVG,values.getAvg());
            fields.setFields(standartfields);

            metric.setFields(fields);

            metricList.add(metric);

        });
    }

    public String toJsonArray()
    {
       if(metricList.size()>1)
        {
            io.vertx.core.json.JsonArray array=new JsonArray();
            metricList.stream().forEach(metric -> {
                array.add(metric);

            });
            return array.toString();
        }
        else
        {
            JsonObject jsonObject=new JsonObject();
            jsonObject=metricList.get(0).toJsonObjec();

            return jsonObject.toString();
        }

    }
}
