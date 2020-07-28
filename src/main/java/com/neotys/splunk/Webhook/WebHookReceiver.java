package com.neotys.splunk.Webhook;


import com.neotys.splunk.Logger.NeoLoadLogger;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.ArrayList;
import java.util.List;

import static com.neotys.splunk.conf.Constants.*;

public class WebHookReceiver  extends AbstractVerticle {
    private HttpServer server;
    private NeoLoadLogger loadLogger;
    int httpPort;
    private Vertx rxvertx;
    List<String> testidlist=new ArrayList<>();
    public void start() {

        loadLogger=new NeoLoadLogger(this.getClass().getName());
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.post(WEBHOOKPATH).handler(this::postWebHook);
        router.get(HEALTH_PATH).handler(this::getHealth);
        String port=System.getenv(SECRET_PORT);
        if(port==null)
        {
            httpPort=HTTP_PORT;
        }
        else
        {
            httpPort=Integer.parseInt(port);
        }
        server = vertx.createHttpServer();

        server.requestHandler(router::accept);
        server.listen(httpPort);
    }

    private void getHealth(RoutingContext routingContext) {
        routingContext.response().setStatusCode(200).end("Health Check status OK");
    }

    private void postWebHook(RoutingContext routingContext) {
        if(routingContext.getBody()==null) {
            loadLogger.error("Technical error - there is no payload" );
            routingContext.response().setStatusCode(500).end("Technical error - there is no payload");
            return;
        }
        JsonObject body=routingContext.getBodyAsJson();
        if(body.containsKey(TESTID_KEY))
        {
            String testid=body.getString(TESTID_KEY);
            if(!testidlist.contains(testid))
            {
                testidlist.add(testid);
                loadLogger.setTestid(testid);
                loadLogger.debug("Received Webhook with testid  " + testid);
                try {
                    NeoLoadHttpHandler httpHandler = new NeoLoadHttpHandler(testid);
                    Future<Boolean> booleanFuture = httpHandler.sync(vertx);
                    booleanFuture.setHandler(booleanAsyncResult -> {
                        if (booleanAsyncResult.succeeded()) {
                            testidlist.remove(testid);
                            routingContext.response().setStatusCode(200)
                                    .end("Results sent to Splunk");
                        } else {
                            routingContext.response().setStatusCode(500)
                                    .end("Issue to sent to Splunk");
                        }
                    });

                } catch (Exception e) {
                    loadLogger.error("Technical error " + e.getMessage());
                    e.printStackTrace();
                    routingContext.response().setStatusCode(500).end(e.getMessage());
                }
            }
            else
            {
                routingContext.response().setStatusCode(302)
                        .end("Results is already synchronised with Splunk");
            }

        }
        else
        {
            routingContext.response()
                    .setStatusCode(420)
                    .end("Response time does not have any TestID");
        }
    }
}
