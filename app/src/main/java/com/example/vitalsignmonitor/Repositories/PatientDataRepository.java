package com.example.vitalsignmonitor.Repositories;

import com.example.vitalsignmonitor.Model.*;
import com.orm.query.Condition;
import com.orm.query.Select;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PatientDataRepository {

    public static List<PatientData> getPatientData(Patient patient) {
        List<PatientData> result;

        result = PatientData.find(PatientData.class, "patient = ?", patient.getId().toString());

        return result;
    }

    public static PatientData savePatientData(PatientData data) {
        data.save();
        return data;
    }

    public static List<PatientDataGroup> getPatientDataGroup(Patient patient) {
        List<PatientDataGroup> result = new ArrayList<PatientDataGroup>();
        List<PatientData> data;

        //data = PatientData.find(PatientData.class, "patient = ?", patient.getId().toString());
        data = Select.from(PatientData.class)
                .where(Condition.prop("patient").eq(patient.getId()))
                .orderBy("date desc")
                .list();

        for(PatientData current : data){
            PatientDataGroup group = null;

            for(PatientDataGroup currentGroup : result)
                if(currentGroup.groupId.equals(current.groupId)){
                    group = currentGroup;
                    break;
                }

            if(group == null){
                group = new PatientDataGroup();
                group.groupId = current.groupId;
                group.date = current.date;

                result.add(group);
            }

            switch (current.sensorType.getId().intValue()){
                case SensorType.TEMPERATURE:
                    group.temperature = current.dato1;
                    break;

                case SensorType.PRESSURE:
                    group.pressure = current.dato1;
                    break;

                case SensorType.BPM:
                    group.bpm = current.dato1;
                    break;

                case SensorType.SPO:
                    group.spo2 = current.dato1;
                    break;
            }
        }

        return result;
    }

}
