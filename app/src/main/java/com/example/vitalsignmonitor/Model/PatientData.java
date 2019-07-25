package com.example.vitalsignmonitor.Model;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;

import java.util.Calendar;

public class PatientData extends SugarRecord<PatientData> {

    @Ignore
    public int idPatientData;

    public float dato1;
    public float dato2;
    public Calendar date;
    public String groupId;

    //Relaciones
    public SensorType sensorType;
    public Patient patient;

}
