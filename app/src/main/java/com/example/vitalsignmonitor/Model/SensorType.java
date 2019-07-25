package com.example.vitalsignmonitor.Model;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;

import java.util.Calendar;

public class SensorType extends SugarRecord<SensorType> {

    public final static int TEMPERATURE = 1;
    public final static int PRESSURE = 2;
    public final static int BPM = 3;
    public final static int SPO = 4;

    public String name;
    public String description;
    public boolean active;

    public SensorType () {}

    public SensorType(String pName, String pDescription, Boolean pActive){
        name = pName;
        description = pDescription;
        active= pActive;
    }

}
