package com.neotys.splunk.Webhook;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public class SyncResultTimer {

    AtomicReference<Integer> offset_events;
    HashMap<String,AtomicReference<Long>> offset_elements;
    HashMap<String,AtomicReference<Long>> offset_monitor;
    String test_status;
    HashMap<String,Boolean> syncElementValues ;
    HashMap<String,Boolean> syncMonitoringValues ;

    long wait;


    public SyncResultTimer(long wait) {
        this.offset_events =new AtomicReference<>(0);
        this.offset_elements = new HashMap<>();
        this.offset_monitor = new HashMap<>();
        this.wait = wait;
        this.test_status="RUNNING";
        this.syncElementValues = new HashMap<>() ;
        this.syncMonitoringValues =new HashMap<>();
    }

    public AtomicReference<Long> getOffsetFromElementid(String elementid)
    {
        if(offset_elements.containsKey(elementid))
        {
            return offset_elements.get(elementid);
        }
        else
        {
            offset_elements.put(elementid,new AtomicReference<Long>((long) 0));
            return offset_elements.get(elementid);
        }
    }

    public void setOffsetFromElementid(String elementid,Long atomicReference)
    {
          offset_elements.put(elementid,new AtomicReference<Long>(atomicReference));

    }

    public void setOffsetFromMonitoringid(String monitoring,Long atomicReference)
    {
        offset_monitor.put(monitoring,new AtomicReference<Long>(atomicReference));

    }

    public AtomicReference<Long> getOffsetFromMonitoringid(String monitoringid)
    {
        if(offset_monitor.containsKey(monitoringid))
        {
            return offset_monitor.get(monitoringid);
        }
        else
        {
            offset_monitor.put(monitoringid,new AtomicReference<Long>((long) 0));
            return offset_monitor.get(monitoringid);
        }
    }

    public Boolean isALLElementSynchronized()
    {

        if(syncElementValues.containsValue(false))
            return false;
        else
            return true;
    }

    public Boolean isALLMonitoringSynchronized()
    {

        if(syncMonitoringValues.containsValue(false))
            return false;
        else
            return true;
    }

    public Boolean isElementValuesSynchronized(String elementID) {
        if(syncElementValues.containsKey(elementID))
        {
            return syncElementValues.get(elementID);
        }
        else
        {
            syncElementValues.put(elementID,false);
            return false;
        }

    }

    public void elementValuesSynchronized(String elmentid) {
        this.syncElementValues.put(elmentid,true);
    }

    public Boolean isMonitoringValuesSynchronized(String montoringid) {
        if(syncMonitoringValues.containsKey(montoringid))
        {
            return syncMonitoringValues.get(montoringid);
        }
        else
        {
            syncMonitoringValues.put(montoringid,false);
            return false;
        }
    }

    public void monitoringValuesSynchronized(String monitoringid) {
            syncMonitoringValues.put(monitoringid,true);

    }

    public String getTest_status() {
        return test_status;
    }

    public void setTest_status(String test_status) {
        this.test_status = test_status;
    }

    public AtomicReference<Integer> getOffset_events() {
        return offset_events;
    }

    public void setOffset_events(AtomicReference<Integer> offset_events) {
        this.offset_events = offset_events;
    }

    public long getWait() {
        return wait;
    }

    public void setWait(long wait) {
        this.wait = wait;
    }
}
