package com.neotys.splunk.Webhook;


import com.neotys.ascode.swagger.client.ApiClient;
import com.neotys.ascode.swagger.client.ApiException;
import com.neotys.ascode.swagger.client.api.ResultsApi;
import com.neotys.ascode.swagger.client.model.*;

import com.neotys.splunk.DataModel.*;
import com.neotys.splunk.DataModel.splunk.Events;
import com.neotys.splunk.DataModel.splunk.Metrics;
import com.neotys.splunk.Logger.NeoLoadLogger;
import com.neotys.splunk.conf.NeoLoadException;
import com.neotys.splunk.httpclient.Httpclient;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.neotys.splunk.conf.Constants.*;


public class NeoLoadHttpHandler {
    private String testid;
    private Optional<String> neoload_Web_Url;
    private Optional<String> neoload_API_Url;
    private String neoload_API_key;
    private NeoLoadLogger logger;
    private ApiClient apiClient;
    private Optional<String> splunkHost;
    private Optional<String> splunkport;
    private Optional<String> splunkAuthMETRICToken;
    private Optional<String> splunkAuthEVENTToken;
    private ResultsApi resultsApi;
    private String projectName;
    private String scenarioName;
    private String testname;
    private long testStart;
    private long testEnd;
    private String status;
    private TestStatistics statistics;
    private String maxVu;
    private String testoverviewpng;
    private boolean ssl;
    private Httpclient httpclientmetric;
    private Httpclient httpclientevent;

    public NeoLoadHttpHandler(String testid) throws NeoLoadException {
        this.testid=testid;
        logger=new NeoLoadLogger(this.getClass().getName());
        logger.setTestid(testid);
        getEnvVariables();


        generateApiUrl();

        apiClient=new ApiClient();
        apiClient.setBasePath(HTTPS+neoload_API_Url.get());
        apiClient.setApiKey(neoload_API_key);
        resultsApi=new ResultsApi(apiClient);


    }



    public Future<Boolean> syncTestData( Vertx vertx)
    {
        //#TODO Send values and points to the api every x s and remove from the list
        httpclientmetric =new Httpclient(vertx,splunkHost.get(),splunkport.get(),SPLUNK_HTTP_COLLECTOR_METRIC_PATH, splunkAuthMETRICToken.get(),ssl);
        httpclientevent=new Httpclient(vertx,splunkHost.get(),splunkport.get(),SPLUNK_HTTP_COLLECTOR_METRIC_PATH,splunkAuthEVENTToken.get(),ssl);
        Future<Boolean> future_results= Future.future();
        String test_status=null;
        AtomicReference<Integer> offset_events= new AtomicReference<Integer>( 0);
        AtomicReference<Long> offset_elements=new AtomicReference<Long>((long) 0);
        AtomicReference<Long> offset_monitor=new AtomicReference<Long>((long) 0);

        List<String> errorStrings=new ArrayList<>();
        try {
            logger.debug("Start to extrat data from "+ testid);

            TestDefinition testDefinition = resultsApi.getTest(testid);
            test_status=testDefinition.getStatus().getValue();
            logger.debug("Test test has the current status "+ test_status);


            while (!test_status.equalsIgnoreCase(NEOLOAD_ENDSTATUS))
            {

                logger.debug("Test test has the current status "+ test_status);
                testDefinition = resultsApi.getTest(testid);
                test_status=testDefinition.getStatus().getValue();
                TestDefinition finalTestDefinition2 = testDefinition;
                logger.debug("parsing the test "+testDefinition.getName());
                ELEMENT_LIST_CATEGORY.stream().parallel().forEach(category ->
                {
                    try {
                        logger.debug("Start element parsing for category "+category);
                        resultsApi.getTestElements(testid, category).forEach(elementDefinition ->
                        {
                                logger.debug("looking at the element "+elementDefinition.getName());
                            //----for each element-----
                            try {
                                NeoLoadListOfElementPoints neoLoadListOfElementPoints=new NeoLoadListOfElementPoints();

                                resultsApi.getTestElementsPoints(testid, elementDefinition.getId(), ELEMENT_STATISTICS).forEach(point ->
                                {
                                    logger.debug("Foudn te element points withe offset "+point.getFrom() +" with ref "+offset_elements.get());
                                    //----store the points----
                                    if(point.getFrom()>=offset_elements.get())
                                    {
                                        logger.debug("Storing the element point "+point.getFrom());
                                        NeoLoadElementsPoints elementsPoints=new NeoLoadElementsPoints(finalTestDefinition2,elementDefinition,point);
                                        try {

                                            neoLoadListOfElementPoints.addPointst(elementsPoints);
                                            logger.debug(" point stored " + point.getFrom());
                                        }
                                        catch (NullPointerException e)
                                        {
                                            logger.debug(" null pointer execption on "+ elementsPoints.toString());
                                        }
                                    }
                                    //-----------------------
                                    offset_elements.set(point.getFrom());
                                });
                                Metrics elementpointsMetrics=new Metrics(neoLoadListOfElementPoints);
                                //----send the data
                                Future<JsonObject> jsonObjectFuture= httpclientmetric.sendJsonObject(elementpointsMetrics.toJsonArray());
                                jsonObjectFuture.setHandler(jsonObjectAsyncResult -> {
                                    if(jsonObjectAsyncResult.succeeded())
                                    {
                                        logger.error("Data Received : "+jsonObjectAsyncResult.result().toString());

                                    }
                                    else
                                    {
                                        logger.error("Issue to receive response",jsonObjectAsyncResult.cause());
                                    }
                                });
                                //----------------

                            } catch (ApiException e) {
                                if(e.getCode()==NL_API_LIMITE_CODE)
                                {
                                   getRetryFromHeader(e);
                                }
                                else {
                                    logger.error("Error parsing the element for id " + elementDefinition.getId() + " with code " + e.getCode() + "with hearder" + e.getResponseHeaders().toString(), e);
                                    errorStrings.add("Error parsing the element for id " + elementDefinition.getId() + " -" + e.getMessage());
                                }
                            }

                        });
                    } catch (ApiException e) {
                        if(e.getCode()==NL_API_LIMITE_CODE)
                        {
                            getRetryFromHeader(e);
                        }
                        else {
                            logger.error("Error parseing results" + e.getCode() + " hearders" + e.getResponseHeaders().toString(), e);
                            errorStrings.add("Error parseing results " + e.getMessage());
                        }
                    }
                });

               try{
                   NeoLoadListOfElementPoints neoLoadListOfElementPointsRequest=new NeoLoadListOfElementPoints();

                   ElementDefinition elementDefinition=resultsApi.getTestElementDefinition(testid,ALL_REQUEST);
                        resultsApi.getTestElementsPoints(testid, ALL_REQUEST, ELEMENT_STATISTICS).forEach(point ->
                        {
                            logger.debug("Foudn te element points withe offset "+point.getFrom() +" with ref "+offset_elements.get());
                            //----store the points----
                            if(point.getFrom()>=offset_elements.get())
                            {
                                logger.debug("Storing the element point "+point.getFrom());
                                NeoLoadElementsPoints elementsPoints=new NeoLoadElementsPoints(finalTestDefinition2,elementDefinition,point);
                                try {
                                    neoLoadListOfElementPointsRequest.addPointst(elementsPoints);
                                    logger.debug(" point stored " + point.getFrom());
                                }
                                catch (NullPointerException e)
                                {
                                    logger.debug(" null pointer execption on "+ elementsPoints.toString());
                                }
                            }
                            //-----------------------
                            offset_elements.set(point.getFrom());
                        });
                   Metrics elementpointsMetrics=new Metrics(neoLoadListOfElementPointsRequest);
                   //----send the data
                   Future<JsonObject> jsonObjectFuture= httpclientmetric.sendJsonObject(elementpointsMetrics.toJsonArray());
                   jsonObjectFuture.setHandler(jsonObjectAsyncResult -> {
                       if(jsonObjectAsyncResult.succeeded())
                       {
                           logger.error("Data Received : "+jsonObjectAsyncResult.result().toString());

                       }
                       else
                       {
                           logger.error("Issue to receive response",jsonObjectAsyncResult.cause());
                       }
                   });
                   //-----

               } catch (ApiException e) {
                        if(e.getCode()==NL_API_LIMITE_CODE)
                        {
                            getRetryFromHeader(e);
                        }
                        else {
                            logger.error("Error parsing the element", e);
                            errorStrings.add("Error parsing the element -" + e.getMessage());
                        }
                    }
                try{
                //----query the coutners$
                NeoLoadMonitoringListOfPoints neoLoadMonitoringListOfPoints=new NeoLoadMonitoringListOfPoints();

                    resultsApi.getTestMonitors(testid).forEach(counterDefinition ->
                {
                    logger.debug("parsing the counter "+counterDefinition.getName());

                    try {

                        resultsApi.getTestMonitorsPoints(testid, counterDefinition.getId()).forEach(point ->

                        {
                            logger.debug("parsing the point with offset"+point.getFrom() +" the current offset reference is "+offset_monitor.get());

                            //------store in the database------
                            if(point.getFrom()>=offset_monitor.get())
                            {
                                logger.debug("Storing the point with offset"+point.getFrom());
                                NeoLoadMonitoringPoints monitoringPoints=new NeoLoadMonitoringPoints(finalTestDefinition2,counterDefinition,point);
                                try {
                                    neoLoadMonitoringListOfPoints.addPoints(monitoringPoints);
                                    logger.debug(" the point with offset stored"+point.getFrom());
                                }
                                catch (NullPointerException e)
                                {
                                    logger.debug(" null pointer execption on "+ monitoringPoints.toString());
                                }

                            }
                            offset_monitor.set(point.getFrom());
                            //-------------------------------
                        });
                        Metrics elementpointsmonitoringMetrics=new Metrics(neoLoadMonitoringListOfPoints);
                        //---send data
                        Future<JsonObject> jsonObjectFuture= httpclientmetric.sendJsonObject(elementpointsmonitoringMetrics.toJsonArray());
                        jsonObjectFuture.setHandler(jsonObjectAsyncResult -> {
                            if(jsonObjectAsyncResult.succeeded())
                            {
                                logger.error("Data Received : "+jsonObjectAsyncResult.result().toString());

                            }
                            else
                            {
                                logger.error("Issue to receive response",jsonObjectAsyncResult.cause());
                            }
                        });
                        //----------

                    }
                    catch (ApiException e)
                    {
                        if(e.getCode()==NL_API_LIMITE_CODE)
                        {
                            getRetryFromHeader(e);
                        }
                        else {
                            logger.error("unable to qery the points of the counter : " + counterDefinition.getId() + " with code " + e.getCode() + "header " + e.getResponseHeaders().toString(), e);
                            errorStrings.add("unable to qery the points of the counter : " + counterDefinition.getId() + " - " + e.getMessage());
                        }
                    }
                });
                }
                catch (ApiException e)
                {
                    if(e.getCode()==NL_API_LIMITE_CODE)
                    {
                        getRetryFromHeader(e);
                    }
                    else {
                        logger.error("unable to qery counter  with code " + e.getCode(), e);
                        errorStrings.add("unable to qery counter " + e.getMessage());
                    }

                }



                NeoLoadListEvents neoLoadListEvents=new NeoLoadListEvents();

                HashMap<String,String> elements=new HashMap<>();
                TestDefinition finalTestDefinition = testDefinition;
                try{
                    resultsApi.getTestEvents(testid,null,200, offset_events.get(),"+offset_events").forEach(eventDefinition ->
                    {
                        logger.debug("parsing the event  "+eventDefinition.getFullname());

                        String elementname=elements.get(eventDefinition.getElementid().toString());
                        if(elementname==null)
                        {
                            try
                            {
                                elementname=resultsApi.getTestElementDefinition(testid,eventDefinition.getElementid().toString()).getName();
                                elements.put(eventDefinition.getElementid().toString(),elementname);
                            }
                            catch (ApiException e)
                            {
                                if(e.getCode()==NL_API_LIMITE_CODE)
                                {
                                    getRetryFromHeader(e);
                                }
                                else {
                                    logger.error("Unable to find the element " + eventDefinition.getElementid().toString());
                                    errorStrings.add("unable to find the element  : " + eventDefinition.getId() + " - " + e.getMessage());

                                }
                            }
                            logger.debug("parsing on the element   "+elementname);

                        }
                        //----store the event---------------
                        logger.debug("Storing the event  "+eventDefinition.getFullname());

                        NeoLoadEvents neoLoadEvents=new NeoLoadEvents(finalTestDefinition,eventDefinition,elementname);
                        try {
                            neoLoadListEvents.addevent(neoLoadEvents);
                        }
                        catch (NullPointerException e)
                        {
                            logger.debug(" null pointer execption on "+ neoLoadEvents.toString());
                        }
                        increment(offset_events);

                        //----------------------------------
                    });
                    Events splunkevent=new Events(neoLoadListEvents);
                    //---send the events
                    //----send the data----
                    Future<JsonObject> jsonObjectFuture= httpclientevent.sendJsonObject(splunkevent.toJsonArray());
                    jsonObjectFuture.setHandler(jsonObjectAsyncResult -> {
                        if(jsonObjectAsyncResult.succeeded())
                        {
                            logger.error("Data Received : "+jsonObjectAsyncResult.result().toString());

                        }
                        else
                        {
                            logger.error("Issue to receive response",jsonObjectAsyncResult.cause());
                        }
                    });
                    //-----------------
                }
                catch(ApiException e)
                {
                    if(e.getCode()==NL_API_LIMITE_CODE)
                    {
                        getRetryFromHeader(e);
                    }
                    else {
                        logger.error("Unable to get the evets " ,e);
                        errorStrings.add("unable to get the evets  "+e.getMessage());

                    }
                }
            }

            if(test_status.equalsIgnoreCase(NEOLOAD_ENDSTATUS))
            {
                NeoLoadListOfElementsValues neoLoadListOfElementsValues=new NeoLoadListOfElementsValues();

                Thread.sleep(500);
                logger.debug("Test is finished "+testDefinition.getName());

                //---get the values-----
                TestDefinition finalTestDefinition1 = testDefinition;
                ELEMENT_LIST_CATEGORY.stream().parallel().forEach(category ->
                {
                    try {
                        resultsApi.getTestElements(testid, category).forEach(elementDefinition ->
                        {
                            logger.debug("parsin element "+elementDefinition.getName());

                            //----for each element-----
                            try {
                                ElementValues values=resultsApi.getTestElementsValues(testid, elementDefinition.getId());
                                //----store the element value--
                                logger.debug("Storing value of  element "+elementDefinition.getName());

                                NeoLoadElementsValues neoLoadElementsValues=new NeoLoadElementsValues(finalTestDefinition1,elementDefinition,values);
                                try{
                                neoLoadListOfElementsValues.addValue(neoLoadElementsValues);
                                }
                                catch (NullPointerException e)
                                {
                                    logger.debug(" null pointer execption on "+ neoLoadElementsValues.toString());
                                }


                                //-----------------------------
                            } catch (ApiException e) {
                                if(e.getCode()==NL_API_LIMITE_CODE)
                                {
                                    getRetryFromHeader(e);
                                }
                                else {
                                    logger.error("Error parsing the element values for id " + elementDefinition.getId(), e);
                                    errorStrings.add("unable to find the element  : " + elementDefinition.getId() + " - " + e.getMessage());
                                }
                            }
                        });
                        Metrics ElementValuesTRansaction=new Metrics(neoLoadListOfElementsValues);

                        //----send the data----
                        Future<JsonObject> jsonObjectFuture= httpclientmetric.sendJsonObject(ElementValuesTRansaction.toJsonArray());
                        jsonObjectFuture.setHandler(jsonObjectAsyncResult -> {
                            if(jsonObjectAsyncResult.succeeded())
                            {
                                logger.error("Data Received : "+jsonObjectAsyncResult.result().toString());

                            }
                            else
                            {
                                logger.error("Issue to receive response",jsonObjectAsyncResult.cause());
                            }
                        });

                    } catch (ApiException e) {
                        if(e.getCode()==NL_API_LIMITE_CODE)
                        {
                            getRetryFromHeader(e);
                        }
                        else {
                            logger.error("Errror parseing results", e);
                            errorStrings.add("unable to parse results   - " + e.getMessage());
                        }

                    }
                });
                NeoLoadMonitoringListOfValues neoLoadMonitoringListOfValues=new NeoLoadMonitoringListOfValues();
                resultsApi.getTestMonitors(testid).forEach(counterDefinition -> {
                    try {
                        logger.debug("parsing counter  "+counterDefinition.getName());

                        CounterValues customMonitorValues=resultsApi.getTestMonitorsValues(testid, counterDefinition.getId());
                        NeoLoadMonitoringValues neoLoadMonitoringValues=new NeoLoadMonitoringValues(finalTestDefinition1,counterDefinition,customMonitorValues);
                        try {
                            neoLoadMonitoringListOfValues.addValues(neoLoadMonitoringValues);
                        }
                        catch (NullPointerException e)
                        {
                            logger.debug(" null pointer execption on "+ neoLoadMonitoringValues.toString());
                        }
                        //----store the monitoring value-------
                    }
                    catch (ApiException e)
                    {
                        if(e.getCode()==NL_API_LIMITE_CODE)
                        {
                            getRetryFromHeader(e);
                        }
                        else {
                            logger.error("unable to find counter " + counterDefinition.getId());
                            errorStrings.add("unable to find the counter  : " + counterDefinition.getId() + " - " + e.getMessage());
                        }
                    }
                });
                Metrics ElementValuesMonitoring=new Metrics(neoLoadMonitoringListOfValues);

                //----send the data
                Future<JsonObject> jsonObjectFuture= httpclientmetric.sendJsonObject(ElementValuesMonitoring.toJsonArray());
                jsonObjectFuture.setHandler(jsonObjectAsyncResult -> {
                    if(jsonObjectAsyncResult.succeeded())
                    {
                        logger.error("Data Received : "+jsonObjectAsyncResult.result().toString());

                    }
                    else
                    {
                        logger.error("Issue to receive response",jsonObjectAsyncResult.cause());
                    }
                });
                //-----




            }
            if(errorStrings.size()>0)
            {
                future_results.fail(errorStrings.stream().collect(Collectors.joining(",")));
                logger.error(errorStrings.stream().collect(Collectors.joining(",")));
            }
            else
            {
                future_results.complete(true);
            }

        }
        catch (ApiException e)
        {
            logger.error("Error during syncTestdata",e);

            future_results.fail(e);
        } catch (InterruptedException e) {
            e.printStackTrace();

            future_results.fail(e);
        }
        return future_results;
    }

    private void getRetryFromHeader(ApiException e)
    {
        if(e.getResponseHeaders().containsKey(RETRY_AFTER))
        {
            List<String> retry=e.getResponseHeaders().get(RETRY_AFTER);
            int wait=Integer.parseInt(retry.get(0));
            logger.info("Requires to wait "+wait +" ms");
            try {
                Thread.sleep(wait);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
    }
    private void increment(AtomicReference<Integer> counter) {
        while(true) {
            int existingValue = counter.get();
            int newValue = existingValue + 1;
            if(counter.compareAndSet(existingValue, newValue)) {
                return;
            }
        }
    }
    private void generateApiUrl()
    {
        if(neoload_API_Url.isPresent())
        {
            if(!neoload_API_Url.get().contains(API_URL_VERSION))
                neoload_API_Url=Optional.of(neoload_API_Url.get()+API_URL_VERSION);
        }
    }
    private void getEnvVariables() throws NeoLoadException {

        logger.debug("retrieve the environement variables for neoload  neoload service ");
        neoload_API_key=System.getenv(SECRET_API_TOKEN);
        if(neoload_API_key==null)
        {
            logger.error("No API key defined");
            throw new NeoLoadException("No API key is defined");
        }
        neoload_API_Url= Optional.ofNullable(System.getenv(SECRET_NL_API_HOST)).filter(o->!o.isEmpty());
        if(!neoload_API_Url.isPresent())
            neoload_API_Url=Optional.of(DEFAULT_NL_SAAS_API_URL);

        neoload_Web_Url=Optional.ofNullable(System.getenv(SECRET_NL_WEB_HOST)).filter(o->!o.isEmpty());
        if(!neoload_Web_Url.isPresent())
            neoload_Web_Url=Optional.of(SECRET_NL_WEB_HOST);

        if(System.getenv(SECRET_SSL)!=null&& !System.getenv(SECRET_SSL).isEmpty())
        {
            ssl=Boolean.parseBoolean(System.getenv(SECRET_SSL));

        }
        else
            ssl=false;



        splunkHost =Optional.ofNullable(System.getenv(SECRET_SPLUNK_HOST)).filter(o->!o.isEmpty());
        if(!splunkHost.isPresent()) {
            throw new NeoLoadException("The Splunk Host is required");
        }
        else
            logger.debug("Splunk hostname is defined");

        splunkport =Optional.ofNullable(System.getenv(SECRET_SPLUNK_PORT)).filter(o->!o.isEmpty());
        if(!splunkport.isPresent())
            splunkport =Optional.of(DEFAULT_SPLUNK_PORT);

        splunkAuthMETRICToken =Optional.ofNullable(System.getenv(SECRET_SPLUNK_AUTHMETRIC_TOKEN)).filter(o->!o.isEmpty());
        if(!splunkAuthMETRICToken.isPresent())
            throw new NeoLoadException("Splunk HEC MECTRIC Token is required");

        splunkAuthEVENTToken =Optional.ofNullable(System.getenv(SECRET_SPLUNK_AUTHEVENT_TOKEN)).filter(o->!o.isEmpty());
        if(!splunkAuthEVENTToken.isPresent())
            throw new NeoLoadException("The Splunk HEC Event  Token is required ");





    }
}
