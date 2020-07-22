package com.neotys.splunk.DataModel.splunk;

import java.util.HashMap;

public class Fields {

        //				"region":"us-west-1",
        //				"datacenter":"us-west-1a",
        //				"rack":"63",
        //				"os":"Ubuntu16.10",
        //				"arch":"x64",
        //				"team":"LON",
        //				"service":"6",
        //				"service_version":"0",
        //				"service_environment":"test",
        //				"path":"/dev/sda1",
        //				"fstype":"ext3",
        //				"_value":1099511627776,
        //				"metric_name": "test1.test2.ded.total"
        //
        //		}

        String metric_name;
        Double _value;
        HashMap<String,String> fields;

        public Fields(String metric_name, Double _value) {
                this.metric_name = metric_name;
                this._value = _value;
        }

        public String getMetric_name() {
                return metric_name;
        }

        public void setMetric_name(String metric_name) {
                this.metric_name = metric_name;
        }

        public Double get_value() {
                return _value;
        }

        public void set_value(Double _value) {
                this._value = _value;
        }

        public HashMap<String, String> getFields() {
                return fields;
        }

        public void setFields(HashMap<String, String> fields) {
                this.fields = fields;
        }
}
