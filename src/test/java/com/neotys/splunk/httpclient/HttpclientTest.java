package com.neotys.splunk.httpclient;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.concurrent.CountDownLatch;

public class HttpclientTest {

    //@Test
    public void sendRequest()
    {
        // port 8088 path /services/collector token"}
        String host="ec2-52-208-247-116.eu-west-1.compute.amazonaws.com";
        String token="YOUTOKEN6";
        String path="/services/collector";
        String port ="8088";
        String body="{\"time\":1595492176709,\"source\":\"NEOLOADkonakart\",\"host\":\"konakart_load_webinar\",\"fields\":{\"_value\":0.0,\"metric_name\":\"Controller.UserLoad.Average\",\"path\":\"Controller.UserLoad\",\"scenario\":\"konakart_load_webinar\",\"author\":\"Jan-Henrik Rexed\",\"test_name\":\"10:16 - 23 Jul 2020\"}}";
        String jsonObject=new JsonObject(body).encodePrettily();
        System.out.println(jsonObject);
        String testid="ededed";

        Httpclient httpclient=new Httpclient(Vertx.vertx(),host,port,path,token,false,testid);
        CountDownLatch countDownLatch=new CountDownLatch(1);
        Future<JsonObject> jsonObjectFuture = Future.future();
        jsonObjectFuture.setHandler(jsonObjectAsyncResult -> {
            countDownLatch.countDown();
            if (jsonObjectAsyncResult.succeeded()) {

                System.out.println("Data Received : " + jsonObjectAsyncResult.result().toString());

            } else {
                System.out.println("Issue to receive response"+  jsonObjectAsyncResult.cause());
            }
        });
        httpclient.sendJsonObject(body, jsonObjectFuture);

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}