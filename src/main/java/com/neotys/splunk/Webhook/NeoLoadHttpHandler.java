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
    private boolean neoloadSsl;


    public NeoLoadHttpHandler(String testid) throws NeoLoadException {
        this.testid = testid;
        logger = new NeoLoadLogger(this.getClass().getName());
        logger.setTestid(testid);
        getEnvVariables();

        generateApiUrl();

        apiClient = new ApiClient();

        if (neoloadSsl){
            apiClient.setBasePath(HTTPS + neoload_API_Url.get());
        } else {
            apiClient.setBasePath(HTTP + neoload_API_Url.get());
            apiClient.setVerifyingSsl(false);
        }

        apiClient.setApiKey(neoload_API_key);
        resultsApi = new ResultsApi(apiClient);
    }


    public Future<Boolean> sync(Vertx vertx) {
        Future<Boolean> future = Future.future();

        SyncResultTimer syncResultTimer = new SyncResultTimer(MIN_WAIT_DURATION);
        vertx.setTimer(2000, h -> {
            send(vertx, syncResultTimer, future);
        });

        return future;
    }


    private void send(final Vertx vertx, SyncResultTimer syncResultTimer, Future<Boolean> futureboolean) {

        Future<SyncResultTimer> future = Future.future();
        if (!syncResultTimer.getTest_status().equalsIgnoreCase(NEOLOAD_ENDSTATUS)) {
            logger.info("Running for points and events - status " + syncResultTimer.getTest_status());
            future.setHandler(syncResultTimerAsyncResult -> {
                if (syncResultTimerAsyncResult.succeeded()) {
                    SyncResultTimer syncResultTimer1 = syncResultTimerAsyncResult.result();
                    long wait = syncResultTimer1.getWait();
                    syncResultTimer1.setWait(MIN_WAIT_DURATION);
                    vertx.setTimer(wait, h -> {
                        send(vertx, syncResultTimer1, futureboolean);
                    });


                } else {
                    logger.error("Error ", syncResultTimerAsyncResult.cause());
                }
            });
            syncTestData(vertx, syncResultTimer, future);
        } else {
            logger.info("Running Values");
            if (!syncResultTimer.isALLElementSynchronized() && !syncResultTimer.isALLMonitoringSynchronized()) {
                future.setHandler(syncResultTimerAsyncResult -> {
                    if (syncResultTimerAsyncResult.succeeded()) {
                        SyncResultTimer syncResultTimer1 = syncResultTimerAsyncResult.result();
                        long wait = syncResultTimer1.getWait();
                        syncResultTimer1.setWait(MIN_WAIT_DURATION);
                        vertx.setTimer(wait, h -> {
                            send(vertx, syncResultTimer1, futureboolean);
                        });

                    } else {
                        logger.error("Error ", syncResultTimerAsyncResult.cause());
                    }
                });
                syncValues(vertx, syncResultTimer, future);
            } else {
                futureboolean.complete(true);
            }
            //---Values
        }

    }

    public void getMonitoringPoints(Vertx vertx, SyncResultTimer syncResultTimer, TestDefinition testDefinition) {
        //----query the coutners$
        NeoLoadMonitoringListOfPoints neoLoadMonitoringListOfPoints = new NeoLoadMonitoringListOfPoints();
        try {

            ArrayOfCounterDefinition arrayOfCounterDefinition = resultsApi.getTestMonitors(testid);
            for (int i = 0; i < arrayOfCounterDefinition.size(); i++) {
                CounterDefinition counterDefinition = arrayOfCounterDefinition.get(i);
                //  logger.debug("Parsing the counter "+counterDefinition.getName());

                try {

                    resultsApi.getTestMonitorsPoints(testid, counterDefinition.getId()).forEach(point ->
                    {
                        //       logger.debug("Parsing the point with offset"+point.getFrom() +" the current offset reference is "+offset_monitor.get());

                        //------store in the database------
                        if (point.getFrom() > syncResultTimer.getOffsetFromMonitoringid(counterDefinition.getId()).get() || syncResultTimer.getOffsetFromElementid(counterDefinition.getId()).get() == 0) {
                            //     logger.debug("Storing the point with offset"+point.getFrom());
                            NeoLoadMonitoringPoints monitoringPoints = new NeoLoadMonitoringPoints(testDefinition, counterDefinition, point);
                            try {
                                neoLoadMonitoringListOfPoints.addPoints(monitoringPoints);
                                ///          logger.debug("The point with offset stored"+point.getFrom());
                            } catch (Exception e) {
                                logger.error("Null pointer exception on " + monitoringPoints.toString(), e);
                            }

                        }
                        syncResultTimer.setOffsetFromMonitoringid(counterDefinition.getId(), point.getFrom());
                        //-------------------------------
                    });
                    //    logger.debug("Number of element :" +String.valueOf(neoLoadMonitoringListOfPoints.getNeoLoadMonitoringPointsList().size()));

                    if (neoLoadMonitoringListOfPoints.getNeoLoadMonitoringPointsList().size() > 0) {
                        try {


                            Metrics elementpointsmonitoringMetrics = new Metrics(neoLoadMonitoringListOfPoints, testid);
                            //---send data
                            Httpclient httpclientmetric = new Httpclient(vertx, splunkHost.get(), splunkport.get(), SPLUNK_HTTP_COLLECTOR_METRIC_PATH, splunkAuthMETRICToken.get(), ssl, testid);
                            Future<JsonObject> jsonObjectFuture = Future.future();
                            jsonObjectFuture.setHandler(jsonObjectAsyncResult -> {
                                if (jsonObjectAsyncResult.succeeded()) {
                                    logger.debug("Data Received : " + jsonObjectAsyncResult.result().toString());

                                } else {
                                    logger.error("Issue receiving response", jsonObjectAsyncResult.cause());
                                }
                            });
                            httpclientmetric.sendJsonObject(elementpointsmonitoringMetrics.toJsonArray(), jsonObjectFuture);

                        } catch (Exception e) {
                            logger.error("Issue sending data ", e);
                        }
                    }
                    //----------

                } catch (ApiException e) {
                    if (e.getCode() == NL_API_LIMITE_CODE) {
                        syncResultTimer.setWait(getRetryFromHeader(e));
                        syncResultTimer.setTest_status(testDefinition.getStatus().getValue());
                        break;
                    } else {
                        logger.error("Unable to query the points of the counter : " + counterDefinition.getId() + " with code " + e.getCode() + " header " + e.getResponseHeaders().toString(), e);
                    }
                }
            }
            syncResultTimer.setTest_status(testDefinition.getStatus().getValue());
        } catch (ApiException e) {
            if (e.getCode() == NL_API_LIMITE_CODE) {
                syncResultTimer.setWait(getRetryFromHeader(e));
                syncResultTimer.setTest_status(testDefinition.getStatus().getValue());

            } else {
                logger.error("Unable to query counter with code " + e.getCode(), e);
                syncResultTimer.setTest_status(testDefinition.getStatus().getValue());

            }
        }
    }


    public void getSpecificElememntsPoints(Vertx vertx, String category, SyncResultTimer syncResultTimer, TestDefinition testDefinition) {

        try {

            ArrayOfElementDefinition arrayOfElementDefinition = resultsApi.getTestElements(testid, category);
            for (int i = 0; i < arrayOfElementDefinition.size(); i++) {
                ElementDefinition elementDefinition = arrayOfElementDefinition.get(i);
                //    logger.debug("looking at the element "+elementDefinition.getName());
                //----for each element-----
                try {
                    NeoLoadListOfElementPoints neoLoadListOfElementPoints = new NeoLoadListOfElementPoints();
                    //#TODO get offset element -> check >0 && if ==-1 don't query it
                    resultsApi.getTestElementsPoints(testid, elementDefinition.getId(), ELEMENT_STATISTICS).forEach(point ->
                    {
                        //           logger.debug("Foudn te element points withe offset "+point.getFrom() +" with ref "+offset_elements.get());
                        //----store the points----
                        if (point.getFrom() > syncResultTimer.getOffsetFromElementid(elementDefinition.getId()).get() || syncResultTimer.getOffsetFromElementid(elementDefinition.getId()).get() == 0) {
                            //    logger.debug("Storing the element point "+point.getFrom());
                            NeoLoadElementsPoints elementsPoints = new NeoLoadElementsPoints(testDefinition, elementDefinition, point);
                            try {

                                neoLoadListOfElementPoints.addPointst(elementsPoints);
                                //logger.debug(" point stored " + point.getFrom());
                            } catch (Exception e) {
                                logger.error("Exception on " + elementsPoints.toString(), e);
                            }
                        }
                        //-----------------------
                        syncResultTimer.setOffsetFromElementid(elementDefinition.getId(), point.getFrom());
                    });
                    //#TODO if test terminated -> alors set -1 pour l'élément
                    //  logger.debug("Number of element :" +String.valueOf(neoLoadListOfElementPoints.getNeoLoadElementsPoints().size()));

                    if (neoLoadListOfElementPoints.getNeoLoadElementsPoints().size() > 0) {
                        try {

                            Metrics elementpointsMetrics = new Metrics(neoLoadListOfElementPoints, testid);
                            //----send the data
                            Httpclient httpclientmetric = new Httpclient(vertx, splunkHost.get(), splunkport.get(), SPLUNK_HTTP_COLLECTOR_METRIC_PATH, splunkAuthMETRICToken.get(), ssl, testid);


                            Future<JsonObject> jsonObjectFuture = Future.future();
                            jsonObjectFuture.setHandler(jsonObjectAsyncResult -> {
                                if (jsonObjectAsyncResult.succeeded()) {
                                    logger.debug("Data Received : " + jsonObjectAsyncResult.result().toString());

                                } else {
                                    logger.error("Issue receiving response", jsonObjectAsyncResult.cause());
                                }
                            });
                            httpclientmetric.sendJsonObject(elementpointsMetrics.toJsonArray(), jsonObjectFuture);
                        } catch (Exception e) {
                            logger.error("Issue sending data ", e);
                        }
                    }
                    //----------------


                } catch (ApiException e) {
                    if (e.getCode() == NL_API_LIMITE_CODE) {
                        syncResultTimer.setWait(getRetryFromHeader(e));
                        syncResultTimer.setTest_status(testDefinition.getStatus().getValue());

                        break;
                    } else {
                        logger.error("Error parsing the element for id " + elementDefinition.getId() + " with code " + e.getCode() + " and header " + e.getResponseHeaders().toString(), e);
                    }
                }

            }
            NeoLoadListOfElementPoints neoLoadListOfElementPointsRequest = new NeoLoadListOfElementPoints();


            ElementDefinition elementDefinition = resultsApi.getTestElementDefinition(testid, ALL_REQUEST);
            resultsApi.getTestElementsPoints(testid, ALL_REQUEST, ELEMENT_STATISTICS).forEach(point ->
            {
                //    logger.debug("Foudn te element points withe offset "+point.getFrom() +" with ref "+offset_elements.get());
                //----store the points----
                if (point.getFrom() >= syncResultTimer.getOffsetFromElementid(ALL_REQUEST).get()) {
                    //logger.debug("Storing the element point "+point.getFrom());
                    NeoLoadElementsPoints elementsPoints = new NeoLoadElementsPoints(testDefinition, elementDefinition, point);
                    try {
                        neoLoadListOfElementPointsRequest.addPointst(elementsPoints);
                        //   logger.debug(" point stored " + point.getFrom());
                    } catch (Exception e) {
                        logger.error("Null pointer exception on " + elementsPoints.toString(), e);
                    }

                }
                //-----------------------
                syncResultTimer.setOffsetFromElementid(ALL_REQUEST, point.getFrom());
            });
            if (neoLoadListOfElementPointsRequest.getNeoLoadElementsPoints().size() > 0) {
                try {
                    Metrics elementpointsMetrics = new Metrics(neoLoadListOfElementPointsRequest, testid);
                    //----send the data
                    Httpclient httpclientmetric = new Httpclient(vertx, splunkHost.get(), splunkport.get(), SPLUNK_HTTP_COLLECTOR_METRIC_PATH, splunkAuthMETRICToken.get(), ssl, testid);
                    Future<JsonObject> jsonObjectFuture = Future.future();
                    jsonObjectFuture.setHandler(jsonObjectAsyncResult -> {
                        if (jsonObjectAsyncResult.succeeded()) {
                            logger.debug("Data Received : " + jsonObjectAsyncResult.result().toString());

                        } else {
                            logger.error("Issue receiving response", jsonObjectAsyncResult.cause());
                        }
                    });
                    httpclientmetric.sendJsonObject(elementpointsMetrics.toJsonArray(), jsonObjectFuture);
                } catch (Exception e) {
                    logger.error("Issue sending data ", e);
                }
            }
            //-----

            syncResultTimer.setTest_status(testDefinition.getStatus().getValue());

        } catch (ApiException e) {
            syncResultTimer.setWait(getRetryFromHeader(e));
            syncResultTimer.setTest_status(testDefinition.getStatus().getValue());

        }

    }


    private void getEvents(Vertx vertx, SyncResultTimer syncResultTimer, TestDefinition testDefinition) {
        AtomicReference<Integer> offset_events = syncResultTimer.getOffset_events();
        HashMap<String, String> elements = new HashMap<>();
        TestDefinition finalTestDefinition = testDefinition;
        try {
            NeoLoadListEvents neoLoadListEvents = new NeoLoadListEvents();

            resultsApi.getTestEvents(testid, null, 200, offset_events.get(), "+offset_events").forEach(eventDefinition ->
            {
                // logger.debug("parsing the event  "+eventDefinition.getFullname());

                String elementname = elements.get(eventDefinition.getElementid().toString());
                if (elementname == null) {
                    try {
                        elementname = resultsApi.getTestElementDefinition(testid, eventDefinition.getElementid().toString()).getName();
                        elements.put(eventDefinition.getElementid().toString(), elementname);
                    } catch (ApiException e) {
                        if (e.getCode() == NL_API_LIMITE_CODE) {
                            getRetryFromHeader(e);
                        } else {
                            logger.error("Unable to find the element " + eventDefinition.getElementid().toString());

                        }
                    }
                    // logger.debug("parsing on the element   "+elementname);

                }
                //----store the event---------------
                logger.debug("Storing the event  " + eventDefinition.getFullname());

                NeoLoadEvents neoLoadEvents = new NeoLoadEvents(finalTestDefinition, eventDefinition, elementname);
                try {
                    neoLoadListEvents.addevent(neoLoadEvents);
                } catch (Exception e) {
                    logger.error("Null pointer exception on " + neoLoadEvents.toString(), e);
                }
                increment(offset_events);

                //----------------------------------
            });
            //     logger.debug("Number of element :" +String.valueOf(neoLoadListEvents.getNeoLoadEventsList().size()));
            syncResultTimer.setTest_status(testDefinition.getStatus().getValue());
            if (neoLoadListEvents.getNeoLoadEventsList().size() > 0) {
                try {

                    Events splunkevent = new Events(neoLoadListEvents, testid);
                    //---send the events
                    //----send the data----
                    Httpclient httpclientevent = new Httpclient(vertx, splunkHost.get(), splunkport.get(), SPLUNK_HTTP_COLLECTOR_METRIC_PATH, splunkAuthEVENTToken.get(), ssl, testid);
                    Future<JsonObject> jsonObjectFuture = Future.future();

                    jsonObjectFuture.setHandler(jsonObjectAsyncResult -> {
                        if (jsonObjectAsyncResult.succeeded()) {
                            logger.debug("Data Received : " + jsonObjectAsyncResult.result().toString());

                        } else {
                            logger.error("Issue receiving response", jsonObjectAsyncResult.cause());
                        }
                    });
                    httpclientevent.sendJsonObject(splunkevent.toJsonArray(), jsonObjectFuture);

                } catch (Exception e) {
                    logger.error("Issue sending data ", e);
                }
            }
            //-----------------
            syncResultTimer.setOffset_events(offset_events);
            syncResultTimer.setTest_status(testDefinition.getStatus().getValue());

        } catch (ApiException e) {
            if (e.getCode() == NL_API_LIMITE_CODE) {
                syncResultTimer.setWait(getRetryFromHeader(e));
                syncResultTimer.setOffset_events(offset_events);
                syncResultTimer.setTest_status(testDefinition.getStatus().getValue());

            } else {
                logger.error("Unable to get the events ", e);
                syncResultTimer.setOffset_events(offset_events);
                syncResultTimer.setTest_status(testDefinition.getStatus().getValue());

            }
        }

    }

    private void getMonitoringValuest(Vertx vertx, SyncResultTimer syncResultTimer) {
        NeoLoadMonitoringListOfValues neoLoadMonitoringListOfValues = new NeoLoadMonitoringListOfValues();
        try {
            TestDefinition testDefinition = resultsApi.getTest(testid);
            if (testDefinition.getStatus().getValue().equalsIgnoreCase(NEOLOAD_ENDSTATUS)) {
                ArrayOfCounterDefinition arrayOfCounterDefinition = resultsApi.getTestMonitors(testid);
                for (int i = 0; i < arrayOfCounterDefinition.size(); i++) {
                    CounterDefinition counterDefinition = arrayOfCounterDefinition.get(i);
                    if (!syncResultTimer.isMonitoringValuesSynchronized(counterDefinition.getId())) {

                        try {
                            logger.debug("parsing counter  " + counterDefinition.getName());
                            CounterValues customMonitorValues = resultsApi.getTestMonitorsValues(testid, counterDefinition.getId());

                            NeoLoadMonitoringValues neoLoadMonitoringValues = new NeoLoadMonitoringValues(testDefinition, counterDefinition, customMonitorValues);
                            try {
                                neoLoadMonitoringListOfValues.addValues(neoLoadMonitoringValues);
                                syncResultTimer.monitoringValuesSynchronized(counterDefinition.getId());
                            } catch (Exception e) {
                                logger.debug("Null pointer exception on " + neoLoadMonitoringValues.toString());
                            }
                            //----store the monitoring value-------
                        } catch (ApiException e) {
                            if (e.getCode() == NL_API_LIMITE_CODE) {
                                syncResultTimer.setWait(getRetryFromHeader(e));
                                break;
                            } else {
                                logger.error("unable to find counter " + counterDefinition.getId());
                            }
                        }
                    }
                }
                // logger.debug("Number of element :" +String.valueOf(neoLoadMonitoringListOfValues.getNeoLoadMonitoringValuesList().size()));

                if (neoLoadMonitoringListOfValues.getNeoLoadMonitoringValuesList().size() > 0) {
                    try {


                        Metrics elementValuesMonitoring = new Metrics(neoLoadMonitoringListOfValues, testid);

                        //----send the data
                        Httpclient httpclientmetric = new Httpclient(vertx, splunkHost.get(), splunkport.get(), SPLUNK_HTTP_COLLECTOR_METRIC_PATH, splunkAuthMETRICToken.get(), ssl, testid);
                        Future<JsonObject> jsonObjectFuture = Future.future();

                        jsonObjectFuture.setHandler(jsonObjectAsyncResult -> {
                            if (jsonObjectAsyncResult.succeeded()) {
                                logger.debug("Data Received : " + jsonObjectAsyncResult.result().toString());

                            } else {
                                logger.error("Issue receiving response", jsonObjectAsyncResult.cause());
                            }
                        });

                        httpclientmetric.sendJsonObject(elementValuesMonitoring.toJsonArray(), jsonObjectFuture);
                    } catch (Exception e) {
                        logger.error("Issue sending data ", e);
                    }
                }
                //-----

            }
        } catch (ApiException e) {
            if (e.getCode() == NL_API_LIMITE_CODE) {
                syncResultTimer.setWait(getRetryFromHeader(e));

            } else {
                logger.error("unable to find counter ", e);
            }
        }

    }

    private void getElementValues(Vertx vertx, SyncResultTimer syncResultTimer) {
        try {
            TestDefinition testDefinition = resultsApi.getTest(testid);
            String test_status = testDefinition.getStatus().getValue();

            if (test_status.equalsIgnoreCase(NEOLOAD_ENDSTATUS)) {
                NeoLoadListOfElementsValues neoLoadListOfElementsValues = new NeoLoadListOfElementsValues();


                logger.debug("Test is finished " + testDefinition.getName());

                //---get the values-----
                TestDefinition finalTestDefinition1 = testDefinition;
                ELEMENT_LIST_CATEGORY.stream().forEach(category ->
                {
                    try {
                        ArrayOfElementDefinition arrayOfElementDefinition = resultsApi.getTestElements(testid, category);
                        for (int i = 0; i < arrayOfElementDefinition.size(); i++) {
                            ElementDefinition elementDefinition = arrayOfElementDefinition.get(i);
                            logger.debug("Parsing element " + elementDefinition.getName());
                            if (!syncResultTimer.isElementValuesSynchronized(elementDefinition.getId())) {

                                //----for each element-----
                                try {
                                    ElementValues values = resultsApi.getTestElementsValues(testid, elementDefinition.getId());
                                    //----store the element value--
                                    //    logger.debug("Storing value of  element "+elementDefinition.getName());
                                    NeoLoadElementsValues neoLoadElementsValues = new NeoLoadElementsValues(finalTestDefinition1, elementDefinition, values);
                                    try {
                                        neoLoadListOfElementsValues.addValue(neoLoadElementsValues);
                                        syncResultTimer.elementValuesSynchronized(elementDefinition.getId());

                                    } catch (Exception e) {
                                        logger.debug("Null pointer exception on " + neoLoadElementsValues.toString());
                                    }


                                    //-----------------------------
                                } catch (ApiException e) {
                                    if (e.getCode() == NL_API_LIMITE_CODE) {
                                        syncResultTimer.setWait(getRetryFromHeader(e));
                                    } else {
                                        logger.error("Error parsing the element values for id " + elementDefinition.getId(), e);
                                    }
                                }
                            }
                        }

                        //   logger.debug("Number of element :" +String.valueOf(neoLoadListOfElementsValues.getNeoLoadListOfElementsValuesList().size()));
                        if (neoLoadListOfElementsValues.getNeoLoadListOfElementsValuesList().size() > 0) {
                            try {


                                Metrics elementValuesTRansaction = new Metrics(neoLoadListOfElementsValues, testid);

                                //----send the data----
                                Httpclient httpclientmetric = new Httpclient(vertx, splunkHost.get(), splunkport.get(), SPLUNK_HTTP_COLLECTOR_METRIC_PATH, splunkAuthMETRICToken.get(), ssl, testid);
                                Future<JsonObject> jsonObjectFuture = Future.future();

                                jsonObjectFuture.setHandler(jsonObjectAsyncResult -> {
                                    if (jsonObjectAsyncResult.succeeded()) {
                                        logger.debug("Data Received : " + jsonObjectAsyncResult.result().toString());

                                    } else {
                                        logger.error("Issue receiving response", jsonObjectAsyncResult.cause());
                                    }
                                });
                                httpclientmetric.sendJsonObject(elementValuesTRansaction.toJsonArray(), jsonObjectFuture);

                            } catch (Exception e) {
                                logger.error("Issue sending data ", e);
                            }
                        }

                    } catch (ApiException e) {
                        if (e.getCode() == NL_API_LIMITE_CODE) {
                            syncResultTimer.setWait(getRetryFromHeader(e));
                        } else {
                            logger.error("Error parsing results", e);
                        }

                    }
                });


            }
        } catch (ApiException e) {
            if (e.getCode() == NL_API_LIMITE_CODE) {
                syncResultTimer.setWait(getRetryFromHeader(e));
            } else {
                logger.error("Error parsing results", e);
            }

        }


    }

    private void syncValues(Vertx vertx, SyncResultTimer syncResultTimer, Future<SyncResultTimer> future) {


        if (!syncResultTimer.isALLElementSynchronized()) {
            getElementValues(vertx, syncResultTimer);
            if (syncResultTimer.getWait() > MIN_WAIT_DURATION)
                future.complete(syncResultTimer);
        }
        if (!syncResultTimer.isALLMonitoringSynchronized()) {
            getMonitoringValuest(vertx, syncResultTimer);
            if (syncResultTimer.getWait() > MIN_WAIT_DURATION)
                future.complete(syncResultTimer);
        }

        future.complete(syncResultTimer);

    }

    private void syncTestData(Vertx vertx, SyncResultTimer syncResultTimer, Future<SyncResultTimer> future_results) {

        String test_status = null;

        try {
            logger.debug("Starting to extract data from " + testid);

            TestDefinition testDefinition = resultsApi.getTest(testid);
            test_status = testDefinition.getStatus().getValue();
            logger.info("Test has the current status " + test_status);
            syncResultTimer.setTest_status(testDefinition.getStatus().getValue());

            if (!test_status.equalsIgnoreCase(NEOLOAD_ENDSTATUS)) { ///#TODO and offset of element /monitorng = -1

                logger.debug("Test has the current status " + test_status);
                testDefinition = resultsApi.getTest(testid);
                TestDefinition finalTestDefinition2 = testDefinition;
                logger.debug("Parsing the test " + testDefinition.getName());


                ELEMENT_LIST_CATEGORY.stream().forEach(category ->
                {
                    logger.debug("Start element parsing for category " + category);
                    getSpecificElememntsPoints(vertx, category, syncResultTimer, finalTestDefinition2);


                });
                if (syncResultTimer.getWait() > MIN_WAIT_DURATION)
                    future_results.complete(syncResultTimer);


                getMonitoringPoints(vertx, syncResultTimer, testDefinition);
                if (syncResultTimer.getWait() > MIN_WAIT_DURATION)
                    future_results.complete(syncResultTimer);

                getEvents(vertx, syncResultTimer, testDefinition);
                if (syncResultTimer.getWait() > MIN_WAIT_DURATION)
                    future_results.complete(syncResultTimer);

                future_results.complete(syncResultTimer);

            } else
                future_results.complete(syncResultTimer);
        } catch (Exception e) {
            logger.error("Technical Error ", e);
            future_results.fail(e);
        }

    }

    private int getRetryFromHeader(ApiException e) {
        if (e.getResponseHeaders().containsKey(RETRY_AFTER)) {
            List<String> retry = e.getResponseHeaders().get(RETRY_AFTER);
            int wait = Integer.parseInt(retry.get(0));
            logger.info("Requires to wait " + wait + " ms");
            return wait;

        } else
            return 0;
    }

    private void increment(AtomicReference<Integer> counter) {
        while (true) {
            int existingValue = counter.get();
            int newValue = existingValue + 1;
            if (counter.compareAndSet(existingValue, newValue)) {
                return;
            }
        }
    }

    private void generateApiUrl() {
        if (neoload_API_Url.isPresent()) {
            if (!neoload_API_Url.get().contains(API_URL_VERSION))
                neoload_API_Url = Optional.of(neoload_API_Url.get() + API_URL_VERSION);
        }
    }

    private void getEnvVariables() throws NeoLoadException {

        logger.debug("Retrieving the environment variables for neoload service ");
        neoload_API_key = System.getenv(SECRET_API_TOKEN);
        if (neoload_API_key == null) {
            logger.error("No API key defined");
            throw new NeoLoadException("No API key is defined");
        }

        neoload_API_Url = Optional.ofNullable(System.getenv(SECRET_NL_API_HOST)).filter(o -> !o.isEmpty());
        if (!neoload_API_Url.isPresent()) neoload_API_Url = Optional.of(DEFAULT_NL_SAAS_API_URL);

        neoload_Web_Url = Optional.ofNullable(System.getenv(SECRET_NL_WEB_HOST)).filter(o -> !o.isEmpty());
        if (!neoload_Web_Url.isPresent()) neoload_Web_Url = Optional.of(SECRET_NL_WEB_HOST);

        if (System.getenv(SECRET_SSL) != null && !System.getenv(SECRET_SSL).isEmpty()) {
            ssl = Boolean.parseBoolean(System.getenv(SECRET_SSL));
        } else {
            ssl = false;
        }

        if (System.getenv(NEOLOAD_SSL) != null && !System.getenv(NEOLOAD_SSL).isEmpty()) {
            neoloadSsl = Boolean.parseBoolean(System.getenv(NEOLOAD_SSL));
        } else {
            neoloadSsl = false;
        }

        splunkHost = Optional.ofNullable(System.getenv(SECRET_SPLUNK_HOST)).filter(o -> !o.isEmpty());
        if (!splunkHost.isPresent()) {
            throw new NeoLoadException("The Splunk Host is required");
        } else {
            logger.debug("Splunk hostname is defined");
        }

        splunkport = Optional.ofNullable(System.getenv(SECRET_SPLUNK_PORT)).filter(o -> !o.isEmpty());
        if (!splunkport.isPresent()) splunkport = Optional.of(DEFAULT_SPLUNK_PORT);

        splunkAuthMETRICToken = Optional.ofNullable(System.getenv(SECRET_SPLUNK_AUTHMETRIC_TOKEN)).filter(o -> !o.isEmpty());
        if (!splunkAuthMETRICToken.isPresent()) throw new NeoLoadException("The Splunk HEC Metric Token is required");

        splunkAuthEVENTToken = Optional.ofNullable(System.getenv(SECRET_SPLUNK_AUTHEVENT_TOKEN)).filter(o -> !o.isEmpty());
        if (!splunkAuthEVENTToken.isPresent()) throw new NeoLoadException("The Splunk HEC Event Token is required ");


    }
}
