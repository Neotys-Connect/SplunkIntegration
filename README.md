# NeoLoad Splunk Synchroniser
<p align="center"><img src="/screenshots/splunk-logo.jpg" width="40%" alt="SuperMon Logo" /></p>

This project will stream out all data of a neoload web test to Splunk
This project has one container :
* `neoload_splunksync` : container that will listen for NeoLoad WEB webhook calls. Once received a notification ( "test started") the service will extract the NeoLoad data and feed the Splunk.

## WebHook Handler : neoload_splunksync

###Configuration

####Step 1 : Enable HTTP Event Collector ( [Read more](https://docs.splunk.com/Documentation/Splunk/latest/Data/UsetheHTTPEventCollector))
Before you can use Event Collector to receive events through HTTP, you must enable it. For Splunk Enterprise, enable HEC through the Global Settings dialog box.
* Click Settings > Data Inputs.
* Click HTTP Event Collector.
* Click Global Settings.
* In the All Tokens toggle button, select Enabled.
* (Optional) Choose a Default Source Type for all HEC tokens. You can also type in the name of the source type in the text field above the drop-down before choosing the source type.
* (Optional) Choose a Default Index for all HEC tokens.
* (Optional) Choose a Default Output Group for all HEC tokens.
* (Optional) To use a deployment server to handle configurations for HEC tokens, click the Use Deployment Server check box.
* (Optional) To have HEC listen and communicate over HTTPS rather than HTTP, click the Enable SSL checkbox.
* Click Save.

####Step 2 Create a metric index in Splunk ([Read More](https://docs.splunk.com/Documentation/Splunk/8.0.5/Indexer/Setupmultipleindexes))
* In Splunk Web, navigate to Settings > Indexes and click New.
* click Metrics.
* Click Save

####Step 3 : Create a HTTP Collector in Splunk to collect NeoLaod Events
In Splunk WEB
* Click Settings > Add Data.
* Click monitor.
* Click HTTP Event Collector.
* In the Name field, enter a name for the token.
* Select your metric index name for HEC events.
* Click Review.
* Confirm that all settings for the endpoint are what you want.
* If all settings are what you want, click Submit. Otherwise, click < to make changes.

Copy the token value that Splunk Web displays and paste it into another document to configure the docker compose.

####Step 4 Create a event index in Splunk ([Read More](https://docs.splunk.com/Documentation/Splunk/8.0.5/Indexer/Setupmultipleindexes))
* In Splunk Web, navigate to Settings > Indexes and click New.
* click Event.
* Click Save

####Step 5 : Create a HTTP Collector in Splunk to collect NeoLaod Events
In Splunk WEB
* Click Settings > Add Data.
* Click monitor.
* Click HTTP Event Collector.
* In the Name field, enter a name for the token.
* Select your event index name for HEC events.
* Click Review.
* Confirm that all settings for the endpoint are what you want.
* If all settings are what you want, click Submit. Otherwise, click < to make changes.

Copy the token value that Splunk Web displays and paste it into another document to configure the docker compose.


####Step 6: Deploy the webHookHandler

The webhook handler is a web service package in a container : `hrexed/neoload_splunksync`
The container will required different to define ther right environement variables

####  Docker environement variables 
To be able to import NeoLoad test results you will need to specify :
* `NL_WEB_HOST`: Hostname of the webui of NeoLoad WEB
* `NL_API_HOST` : Hostname of the rest-api of NeoLoad WEB
* `NL_API_TOKEN` : API token of NeoLoad WEB ( [how to generate an API token](https://www.neotys.com/documents/doc/nlweb/latest/en/html/#24270.htm))
* `PORT`  : Port that the service will listen to
* `logging-level` : Logging level of the service ( DEBUG, INFO, ERROR)
* `SPLUNK_HOST` : InfluxDB database that will receive all the NeoLoad measurements
* `SPLUNK_PORT` : INfluxdb user that the service will connnect. this user needs to have the write permission on the database
* `SPLUNK_HEC_METRIC_TOKEN` : Token related to your HEC that will receive NeoLoad Metrics
* `SPLUNK_HEC_EVENT_TOKEN` : Token  related to your HEC that will receive NeoLoad Events
* `SPLUNK_HEC_SSL` : true or false if your HEC requires SSL

#### Run the webhookHandler

Requirements : Server having :
* docker installed
* acessible from NeoLoad WEB ( Saas our your managemend instance of NeoLoad WEB)

The deployment will use  :
* `/deploy/docker-compose.yaml` to connect spin up the service, influxdb and grafana

Make sure to update the docker-compose file by specifying the Environment variables.

the deployment will be done by running the following command :
```bash
docker-compose -f <docker file> up -d
```
#### Step4 : Configure the WebHook in your NeoLoad Web Account to send a notification to your WebHook service

The webhookhandler service is listenning to 2 disctinct endpoints :
* `/Health` : Get request build to check if the webhookhandler is up
* `/webhook` : POST request to receive the webhook from NeoLoad WEB

The Webhookhandler is expecting the following Json Payload :
```json
{
	"testid" : "TESTID"
}
```

To configure the webhook in NeoLoad WEB you will need to :
1. Connect to NeoLoad WEB
2. Click on 
3. Click On the TAB named WebHook
4. Create a new Webhook ( [How to create a webhook](https://www.neotys.com/documents/doc/nlweb/latest/en/html/#27141.htm))
5. URL of the webhook : http://<IP of you WEBHOOKHANDLER>:8080/webhook
6. Events : Test started
7. Payload :
```json
{
            "testid": "$(test_result_id)"
}
```
<p align="center"><img src="/screenshots/webhook.png" alt="InfluxDB webhok" /></p>
