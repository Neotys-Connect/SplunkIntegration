package com.neotys.splunk.DataModel;


import com.neotys.ascode.swagger.client.model.ElementDefinition;
import com.neotys.ascode.swagger.client.model.ElementValues;
import com.neotys.ascode.swagger.client.model.TestDefinition;


import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class NeoLoadElementsValues extends  NeoLoadElementData {

    private Long count ;

    private Double elementPerSecond ;

    private Long minDuration ;

    private Long maxDuration ;

    private Long sumDuration ;

    private Double avgDuration ;

    private Long minTTFB ;

    private Long maxTTFB ;

    private Long sumTTFB ;

    private Double avgTTFB ;

    private Long sumDownloadedBytes ;

    private Double downloadedBytesPerSecond ;

    private Long successCount = null;

    private Double successPerSecond ;


    private Double successRate ;

    private Long failureCount ;

    private  Double failurePerSecond ;

    private Double failureRate ;

    private Double percentile50;
    private Double  percentile90;

    private Double percentile95;

    private Double percentile99;

    public NeoLoadElementsValues(TestDefinition testDefinition, ElementDefinition elementDefinition, ElementValues values)
    {
        this.initialize(testDefinition);
        this.initElement(elementDefinition);
        this.count=values.getCount();
        this.avgDuration= setValue(values.getAvgDuration());
        this.avgTTFB= setValue(values.getAvgTTFB());
        this.downloadedBytesPerSecond= setValue(values.getDownloadedBytesPerSecond());
        this.elementPerSecond=setValue(values.getElementPerSecond());
        this.failureCount=values.getFailureCount();
        this.failurePerSecond= setValue(values.getFailurePerSecond());
        this.failureRate= setValue(values.getFailureRate());
        this.maxDuration=values.getMaxDuration();
        this.maxTTFB=values.getMaxTTFB();
        this.minDuration=values.getMinDuration();
        this.minTTFB=values.getMinTTFB();
        if(elementDefinition.getType().equalsIgnoreCase("TRANSACTION")) {
            this.percentile50 = setValue(values.getPercentile50());
            this.percentile90 = setValue(values.getPercentile90());
            this.percentile95 = setValue(values.getPercentile95());
            this.percentile99 = setValue(values.getPercentile99());
        }
        else {
            this.percentile50 = setValue(Float.valueOf(0));
            this.percentile90 = setValue(Float.valueOf(0));
            this.percentile95 = setValue(Float.valueOf(0));
            this.percentile99 = setValue(Float.valueOf(0));
        }
        this.successCount=values.getSuccessCount();
        this.successPerSecond= setValue(values.getSuccessPerSecond());
        this.successRate= setValue(values.getSuccessRate());
        this.sumDownloadedBytes=values.getSumDownloadedBytes();
        this.sumDuration=values.getSumDuration();

        if(this.getSumTTFB()!=null)
            this.sumTTFB=this.getSumTTFB();
        else
            this.sumTTFB= Long.valueOf(0);
        this.time= Instant.ofEpochMilli(testDefinition.getEndDate());
    }

    public String toString()
    {
        StringBuilder result=new StringBuilder();
        result.append("Monitroing Points :");
        result.append("count:"+count);
        result.append(" avgDuration:"+avgDuration);
        result.append(" avgTTFB:"+avgTTFB);
        result.append(" downloadedBytesPerSecond:"+downloadedBytesPerSecond);
        result.append(" elementPerSecond:"+elementPerSecond);
        result.append(" failureCount:"+failureCount);
        if(getType().equalsIgnoreCase("TRANSACTION")) {
            result.append(" percentile50:"+percentile50);
            result.append(" percentile90:"+percentile90);
            result.append(" percentile95:"+percentile95);
            result.append(" percentile99:"+percentile99);
        }

        result.append(" failurePerSecond:"+failurePerSecond);
        result.append(" failureRate:"+failureRate);
        result.append(" maxDuration:"+maxDuration);
        result.append(" maxTTFB:"+maxTTFB);
        result.append(" minDuration:"+minDuration);
        result.append(" successRate:"+successRate);
        result.append(" successCount:"+successCount);
        result.append(" successPerSecond:"+successPerSecond);
        result.append(" sumDownloadedBytes:"+sumDownloadedBytes);
        result.append(" sumDuration:"+sumDuration);
        result.append(" sumTTFB:"+sumTTFB);
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
    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Double getElementPerSecond() {
        return elementPerSecond;
    }

    public void setElementPerSecond(Double elementPerSecond) {
        this.elementPerSecond = elementPerSecond;
    }

    public Long getMinDuration() {
        return minDuration;
    }

    public void setMinDuration(Long minDuration) {
        this.minDuration = minDuration;
    }

    public Long getMaxDuration() {
        return maxDuration;
    }

    public void setMaxDuration(Long maxDuration) {
        this.maxDuration = maxDuration;
    }

    public Long getSumDuration() {
        return sumDuration;
    }

    public void setSumDuration(Long sumDuration) {
        this.sumDuration = sumDuration;
    }

    public Double getAvgDuration() {
        return avgDuration;
    }

    public void setAvgDuration(Double avgDuration) {
        this.avgDuration = avgDuration;
    }

    public Long getMinTTFB() {
        return minTTFB;
    }

    public void setMinTTFB(Long minTTFB) {
        this.minTTFB = minTTFB;
    }

    public Long getMaxTTFB() {
        return maxTTFB;
    }

    public void setMaxTTFB(Long maxTTFB) {
        this.maxTTFB = maxTTFB;
    }

    public Long getSumTTFB() {
        return sumTTFB;
    }

    public void setSumTTFB(Long sumTTFB) {
        this.sumTTFB = sumTTFB;
    }

    public Double getAvgTTFB() {
        return avgTTFB;
    }

    public void setAvgTTFB(Double avgTTFB) {
        this.avgTTFB = avgTTFB;
    }

    public Long getSumDownloadedBytes() {
        return sumDownloadedBytes;
    }

    public void setSumDownloadedBytes(Long sumDownloadedBytes) {
        this.sumDownloadedBytes = sumDownloadedBytes;
    }

    public Double getDownloadedBytesPerSecond() {
        return downloadedBytesPerSecond;
    }

    public void setDownloadedBytesPerSecond(Double downloadedBytesPerSecond) {
        this.downloadedBytesPerSecond = downloadedBytesPerSecond;
    }

    public Long getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(Long successCount) {
        this.successCount = successCount;
    }

    public Double getSuccessPerSecond() {
        return successPerSecond;
    }

    public void setSuccessPerSecond(Double successPerSecond) {
        this.successPerSecond = successPerSecond;
    }

    public Double getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(Double successRate) {
        this.successRate = successRate;
    }

    public Long getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(Long failureCount) {
        this.failureCount = failureCount;
    }

    public Double getFailurePerSecond() {
        return failurePerSecond;
    }

    public void setFailurePerSecond(Double failurePerSecond) {
        this.failurePerSecond = failurePerSecond;
    }

    public Double getFailureRate() {
        return failureRate;
    }

    public void setFailureRate(Double failureRate) {
        this.failureRate = failureRate;
    }

    public Double getPercentile50() {
        return percentile50;
    }

    public void setPercentile50(Double percentile50) {
        this.percentile50 = percentile50;
    }

    public Double getPercentile90() {
        return percentile90;
    }

    public void setPercentile90(Double percentile90) {
        this.percentile90 = percentile90;
    }

    public Double getPercentile95() {
        return percentile95;
    }

    public void setPercentile95(Double percentile95) {
        this.percentile95 = percentile95;
    }

    public Double getPercentile99() {
        return percentile99;
    }

    public void setPercentile99(Double percentile99) {
        this.percentile99 = percentile99;
    }
}
