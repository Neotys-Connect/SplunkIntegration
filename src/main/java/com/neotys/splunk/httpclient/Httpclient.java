package com.neotys.splunk.httpclient;


import com.neotys.splunk.Logger.NeoLoadLogger;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.client.predicate.ResponsePredicate;

import java.util.HashMap;

import static com.neotys.splunk.conf.Constants.SPLUNK_AUTH_HEADER;
import static com.neotys.splunk.conf.Constants.SPLUNK_TOKEN_PREFIX;


public class Httpclient {

    private WebClient client;
    private Vertx vertx;
    private NeoLoadLogger logger;
    private String serverport;
    private String serverhost;
    private String token;
    private boolean ssl;
    private String apiPath;
    private HashMap<String,String> headers;

    public Httpclient(Vertx vertx, String serverhost, String serverport, String cloudapipath, String token, boolean ssl) {
        this.vertx=vertx;
        this.ssl=ssl;
        this.serverhost=serverhost;
        this.serverport=serverport;
        this.apiPath =cloudapipath;
        this.token=token;
        client= WebClient.create(vertx,new WebClientOptions().setSsl(ssl).setLogActivity(true));
        logger=new NeoLoadLogger(this.getClass().getName());

        headers=new HashMap<>();
        headers.put(SPLUNK_AUTH_HEADER,SPLUNK_TOKEN_PREFIX+" "+ token);

    }

    public void setSsl(boolean ssl)
    {
        this.ssl=ssl;
    }

    public String getServerport() {
        return serverport;
    }

    public void setServerport(String serverport) {
        this.serverport = serverport;
    }

    public String getServerhost() {
        return serverhost;
    }

    public void setServerhost(String serverhost) {
        this.serverhost = serverhost;
    }




    public Future<String> sendJsonObjectWithURLParams( HashMap<String,String> urlParameters,  JsonObject object)
    {

        Future<String> future= Future.future();
        HttpRequest<Buffer> request = client.post(Integer.parseInt(serverport),serverhost,apiPath);

        MultiMap header=((HttpRequest) request).headers();
        header.addAll(headers);
        request.putHeaders(header)
                .expect(ResponsePredicate.JSON)
                .expect(ResponsePredicate.status(200,300));

        urlParameters.forEach((s, s2) -> {
            request.addQueryParam(s,s2);
        });

        request.sendJson(object,handler->{
                    if(handler.succeeded())
                    {
                        logger.debug("Request sent successfuly - uri :"+apiPath+" payload :"+object.toString());
                        logger.debug("Received the following response :"+ handler.result().bodyAsString());
                        future.complete(handler.result().bodyAsString());

                    }
                    else
                    {
                        logger.error("Issue to receive response ");
                        if(handler.result()!=null) {
                            logger.error("Response code :" + handler.result().statusCode() + " and response  " + handler.result().bodyAsString());
                            future.fail("Response code :" + handler.result().statusCode() + " and response  " + handler.result().bodyAsString());
                        }
                        else {
                            logger.error("no Response ", handler.cause());
                            future.fail("no Response " + handler.cause().getMessage());

                        }

                    }

                });

        return future;
    }



    public void sendGetRequest(Future<JsonObject> future, String uri, HashMap<String,String> queryParams)
    {

        logger.debug("Sending get request to "+ uri +" servhost "+serverhost +" port "+ serverport  );
        HttpRequest<Buffer> request = client.get(Integer.parseInt(serverport),serverhost,uri);

        MultiMap header=((HttpRequest) request).headers();
        headers.forEach((s, s2) -> {
            logger.debug("adding header "+s+" value "+s2);
            header.add(s,s2);
        });


        if(queryParams!=null)
        {
            logger.debug("adding parameters ");
            queryParams.forEach((s, s2) -> {
                request.addQueryParam(s,s2);
                logger.debug(" parametere "+s+" value "+s2);
            });
        }

        header.forEach(stringStringEntry -> {

            logger.debug("Hader "+ stringStringEntry.getKey()+" value "+ stringStringEntry.getValue());
        });

        request.putHeaders(header)
                .send(handler->{
                    if(handler.succeeded())
                    {
                        if(handler.result().statusCode()>=200 && handler.result().statusCode()<400) {
                            logger.debug("Request sent successfuly - uri :" + uri);
                            logger.debug("Received the following response :" + handler.result().toString());
                            future.complete(handler.result().bodyAsJsonObject());

                        }
                        else
                        {
                            logger.error("Response code :" + handler.result().statusCode() + " and response  " + handler.result().bodyAsString());
                            future.fail("Response code :" + handler.result().statusCode() + " and response  " + handler.result().bodyAsString());

                        }
                    }
                    else
                    {
                        logger.error("Issue to get response ");
                        if(handler.result()!=null) {
                            logger.error("Response code :" + handler.result().statusCode() + " and response  " + handler.result().bodyAsString());
                            future.fail("Response code :" + handler.result().statusCode() + " and response  " + handler.result().bodyAsString());

                        }
                        else {
                            logger.error("no Response ", handler.cause());
                            future.fail("no Response " + handler.cause().getMessage());

                        }

                    }

                });


    }

    public Future<JsonObject> sendJsonObject(String object)
    {

        Future<JsonObject> future= Future.future();
        HttpRequest<Buffer> request = client.post(Integer.parseInt(serverport),serverhost,apiPath);

        MultiMap header=((HttpRequest) request).headers();
        header.addAll(headers);
        request.putHeaders(header)
                .expect(ResponsePredicate.JSON)
                .expect(ResponsePredicate.status(200,300))
                .sendJson(object,handler->{
                    if(handler.succeeded())
                    {
                        logger.debug("Request sent successfuly - uri :"+apiPath+" payload :"+object.toString());
                        future.complete(handler.result().bodyAsJsonObject());
                        logger.debug("Received the following response :"+ handler.result().toString());
                    }
                    else
                    {
                        logger.error("Issue to get response ");
                        if(handler.result()!=null) {
                            future.fail("Response code :" + handler.result().statusCode() + " and response  " + handler.result().bodyAsString());
                            logger.error("Response code :" + handler.result().statusCode() + " and response  " + handler.result().bodyAsString());
                        }
                        else {
                            future.fail("no Response " + handler.cause().getMessage());
                            logger.error("no Response ", handler.cause());
                        }

                    }

                });

        return future;
    }
}
