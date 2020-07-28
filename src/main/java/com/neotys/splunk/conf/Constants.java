package com.neotys.splunk.conf;

import java.util.Arrays;
import java.util.List;

public class Constants {

    public static final String HEALTH_PATH="/health";
    public static final String WEBHOOKPATH="/webhook";
    public static final String DEFAULT_NL_SAAS_API_URL="";
    public static final String DEFAULT_NL_WEB_API_URL="";
    public static final String API_URL_VERSION="/v1";
    public static final String TESTID_KEY="testid";

    public static final String SECRET_API_TOKEN="NL_API_TOKEN";
    public static final String SECRET_NL_WEB_HOST="NL_WEB_HOST";
    public static final String SECRET_SSL="SPLUNK_HEC_SSL";
    public static final String SECRET_NL_API_HOST="NL_API_HOST";
    public static final String SECRET_PORT="PORT";
    public static final String VALUES = "Values.";
    public static String LOGING_LEVEL_KEY="logging-level";
    public static int HTTP_PORT=8080;
    public static final String SECRET_SPLUNK_HOST="SPLUNK_HOST";

    public static final String SECRET_SPLUNK_PORT="SPLUNK_PORT";
    public static final String SECRET_SPLUNK_AUTHMETRIC_TOKEN="SPLUNK_HEC_METRIC_TOKEN";
    public static final String SECRET_SPLUNK_AUTHEVENT_TOKEN="SPLUNK_HEC_EVENT_TOKEN";
    public static final String DEFAULT_SPLUNK_PORT="8888";

    public static long MIN_WAIT_DURATION=2000;
    public static final String SOURCE_NEOLOAD="NEOLOAD";

    public static final String PATH="path";

    public static final String TYPE="type";
    public static final String TEST_NAME="test_name";
    public static final String SCENARIO="scenario";
    public static final String AUTHOR="author";
    //----name of the metrics
    public static final String AVG="Average";
    public static final String AVG_TTFB="AverageTTFB";
    public static final String COUNT="Count";
    public static final String ELEMENTS_PER_SECOND="elementPerSecond";
    public static final String ERROR_RATE="ErrorRate";
    public static final String ERRORS_PER_SECOND="ErrorsPerSecond";
    public static final String MAX_DURATION="MaxDuration";
    public static final String MIN_DURATION="MinDuration";
    public static final String MAX_TTFB="MaxTTFB";
    public static final String THROUGHPUT="Throughput";

    public static final String DOWNLOADEDBYTESPERSECOND="downloadedBytesPerSecond";
    public static final String ELEMENTPERSECOND="elementPerSecond";
    public static final String FAILURECOUNT="failureCount";
    public static final String PERCENTILE50="percentile50";
    public static final String PERCENTILE90="percentile90";
    public static final String PERCENTILE95="percentile95";
    public static final String PERCENTILE99="percentile99";
    public static final String FAILUREPERSECOND="failurePerSecond";
    public static final String FAILURERATE="failureRate";
    public static final String MAXDURATION="maxDuration";
    public static final String MAXTTFB="maxTTFB";
    public static final String MINDURATION="minDuration";
    public static final String SUCCESSRATE="successRate";
    public static final String SUCCESSCOUNT="successCount";
    public static final String SUCCESSPERSECOND="successPerSecond";
    public static final String SUMDOWNLOADEDBYTES="sumDownloadedBytes";
    public static final String SUMDURATION="sumDuration";
    public static final String SUMTTFB="sumTTFB";


    public static final String MAX="max";
    public static final String MIN="min";
    public static final String SUM="sum";


    //---
    public static final String SLA_TYPE_PERINTERVAL="PerTimeInterval";
    public static final String SLA_TYPE_PERTEST="PerRun";

    public static final String SPLUNK_HTTP_COLLECTOR_METRIC_PATH="/services/collector";
    public static final String SPLUNK_HTTP_COLLECTOR_EVENT_PATH="/services/collector/raw";
    public static final String SPLUNK_AUTH_HEADER="Authorization";
    public static final String SPLUNK_TOKEN_PREFIX="Splunk";

    public static final String NEOLOAD="neoload";


    public static final String HTTPS="https://";
    public static final String NEOLAOD_WEB_URL="/#!result/";
    public static final String NEOLAOD_WEB_LASTPART_URL="/overview";

    public static final String NEOLOAD_ENDSTATUS="TERMINATED";

    //-----SLA Status

    public static final List<String> ELEMENT_LIST_CATEGORY = Arrays.asList("TRANSACTION");

   // public static final List<String> ELEMENT_LIST_CATEGORY = new ArrayList<>(  Arrays.asList("TRANSACTION", "PAGE","REQUEST"));
    public static final String ELEMENT_STATISTICS="AVG_DURATION,MIN_DURATION,MAX_DURATION,COUNT,THROUGHPUT,ELEMENTS_PER_SECOND,ERRORS,ERRORS_PER_SECOND,ERROR_RATE,AVG_TTFB,MIN_TTFB,MAX_TTFB" ;

    public final static int NL_API_LIMITE_CODE=429;
    public final static String RETRY_AFTER="Retry-After";

    public final static String ALL_REQUEST="all-requests";
}
