package com.neotys.splunk.DataModel.splunk;

import com.neotys.splunk.DataModel.*;
import com.neotys.splunk.Logger.NeoLoadLogger;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.neotys.splunk.conf.Constants.*;

public class Metrics {
    List<Metric> metricList;
    private NeoLoadLogger logger;

    public Metrics(NeoLoadListOfElementPoints listOfElementPoints,String testid)
    {
        logger=new NeoLoadLogger(this.getClass().getName());
        logger.setTestid(testid);
        metricList=new ArrayList<>();
        listOfElementPoints.getNeoLoadElementsPoints().forEach(points -> {
            Metric metric=new Metric(points.getEventTime(),SOURCE_NEOLOAD+points.getProjectname(),points.getScenario());
            HashMap<String,String> standartfields=new HashMap<>();
            standartfields.put(PATH,points.getPath());
            standartfields.put(TYPE,points.getType());
            standartfields.put(TEST_NAME,points.getTestname());
            standartfields.put(AUTHOR,points.getAuthor());
            standartfields.put(SCENARIO,points.getScenario());
      //      logger.debug("Adding points "+ points.getName());
            metricList.addAll(getFields(standartfields,points,metric));
        });
    }

    private Metric getGeneratedMetric(HashMap<String,String> standardFileds,String name,Double value,Metric reference)
    {
        HashMap<String,String> field=new HashMap<>();
        field.putAll(standardFileds);
        Fields fields=new Fields(name,value);
        fields.setFields(field);

        Metric metric=new Metric(reference);

        metric.setFields(fields);

        return metric;
    }

    private List<Metric> getFields(HashMap<String,String> standardFileds,NeoLoadElementsPoints points,Metric reference)
    {
        List<Metric> metricListpoints=new ArrayList<>();

        metricListpoints.add(getGeneratedMetric(standardFileds,points.getPath()+"."+ELEMENTPERSECOND,points.getELEMENTS_PER_SECOND(),reference));
        //---avg
        metricListpoints.add(getGeneratedMetric(standardFileds,points.getPath()+"."+AVG,points.getAVG_DURATION(),reference));
        //---avg²
        metricListpoints.add(getGeneratedMetric(standardFileds,points.getPath()+"."+AVG_TTFB,points.getAVG_TTFB(),reference));
        //---error rate
        metricListpoints.add(getGeneratedMetric(standardFileds,points.getPath()+"."+ERROR_RATE,points.getERROR_RATE(),reference));
        //---error /s
        metricListpoints.add(getGeneratedMetric(standardFileds,points.getPath()+"."+ERRORS_PER_SECOND,points.getERRORS_PER_SECOND(),reference));
        //---MAX
        metricListpoints.add(getGeneratedMetric(standardFileds,points.getPath()+"."+MAX_DURATION,points.getMAX_DURATION(),reference));
        //---MIN
        metricListpoints.add(getGeneratedMetric(standardFileds,points.getPath()+"."+MIN_DURATION,points.getMIN_DURATION(),reference));

        return metricListpoints;
    }
    public Metrics(NeoLoadMonitoringListOfPoints monitoringListOfPoints,String testid)
    {
        logger=new NeoLoadLogger(this.getClass().getName());
        logger.setTestid(testid);
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
      //      logger.debug("Adding points "+ points.getName());
            metricList.add(metric);
        });
    }

    public Metrics(NeoLoadListOfElementsValues neoLoadListOfElementsValues,String testid)
    {
        logger=new NeoLoadLogger(this.getClass().getName());
        logger.setTestid(testid);
        metricList=new ArrayList<>();
        neoLoadListOfElementsValues.getNeoLoadListOfElementsValuesList().stream().forEach(values -> {
            Metric metric=new Metric(values.getEventTime(),SOURCE_NEOLOAD+values.getProjectname(),values.getScenario());
            HashMap<String,String> standartfields=new HashMap<>();
            standartfields.put(PATH,values.getPath());
            standartfields.put(TEST_NAME,values.getTestname());
            standartfields.put(AUTHOR,values.getAuthor());
            standartfields.put(SCENARIO,values.getScenario());
       //     logger.debug("Adding points "+ values.getName());
            metricList.addAll(getFields(standartfields,values,metric));
        });
    }
    private List<Metric> getFields(HashMap<String,String> standardFileds,NeoLoadElementsValues points,Metric reference)
    {
        List<Metric> metricListpoints=new ArrayList<>();
       //----element per second
        metricListpoints.add(getGeneratedMetric(standardFileds,points.getPath()+"."+ELEMENTPERSECOND,points.getElementPerSecond(),reference));
        //---avg
        metricListpoints.add(getGeneratedMetric(standardFileds,points.getPath()+"."+AVG,points.getAvgDuration(),reference));
        //---avg TTFB
        metricListpoints.add(getGeneratedMetric(standardFileds,points.getPath()+"."+AVG_TTFB,points.getAvgTTFB(),reference));
        //---error rate
        metricListpoints.add(getGeneratedMetric(standardFileds,points.getPath()+"."+DOWNLOADEDBYTESPERSECOND,points.getDownloadedBytesPerSecond(),reference));
        //---error /s
        metricListpoints.add(getGeneratedMetric(standardFileds,points.getPath()+"."+FAILUREPERSECOND,points.getFailurePerSecond(),reference));

        //---MAX
        metricListpoints.add(getGeneratedMetric(standardFileds,points.getPath()+"."+FAILURERATE,points.getFailureRate(),reference));

        if(points.getType().equalsIgnoreCase("TRANSACTION")) {
            metricListpoints.add(getGeneratedMetric(standardFileds,points.getPath()+"."+PERCENTILE50,points.getPercentile50(),reference));
            metricListpoints.add(getGeneratedMetric(standardFileds,points.getPath()+"."+PERCENTILE90,points.getPercentile90(),reference));
            metricListpoints.add(getGeneratedMetric(standardFileds,points.getPath()+"."+PERCENTILE95,points.getPercentile95(),reference));
            metricListpoints.add(getGeneratedMetric(standardFileds,points.getPath()+"."+PERCENTILE99,points.getPercentile99(),reference));
        }

        return metricListpoints;
    }

    public Metrics(NeoLoadMonitoringListOfValues monitoringListOfValues,String testid)
    {
        logger=new NeoLoadLogger(this.getClass().getName());
        logger.setTestid(testid);
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
        //    logger.debug("Adding points "+ values.getName());
            metricList.add(metric);

        });
    }

    public String toJsonArray()
    {
       if(metricList.size()>1)
        {
            io.vertx.core.json.JsonArray array=new JsonArray();
            metricList.stream().forEach(metric -> {
                array.add(metric.toJsonObjec());

            });
          //  logger.debug("STRING Generated "+ array.toString());
            return array.toString();
        }
        else
        {
            JsonObject jsonObject=new JsonObject();
            jsonObject=metricList.get(0).toJsonObjec();
          //  logger.debug("STRING Generated "+ jsonObject.toString());
            return jsonObject.toString();
        }

    }
}
