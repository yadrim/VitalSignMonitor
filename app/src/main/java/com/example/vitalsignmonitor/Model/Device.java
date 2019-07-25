package com.example.vitalsignmonitor.Model;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;

import java.util.Calendar;

public class Device extends SugarRecord<Device> {

    @Ignore
    public int idDevice;

    public String name;
    public String identifier;
    public Calendar createdDate;
    public Calendar lastConnection;
    public boolean active;
    public boolean current;

}
