package com.example.vitalsignmonitor.Repositories;

import android.hardware.Sensor;

import com.example.vitalsignmonitor.Model.*;

import java.util.ArrayList;
import java.util.List;

public class SensorTypeRepository {

    public static void checkInitialData() {
        long totalSensorType;
        List<SensorType> sensorTypes = new ArrayList<SensorType>();

        try{
            totalSensorType = SensorType.count(SensorType.class, null, null);
            if(totalSensorType == 0){
                sensorTypes.add(new SensorType("temp", "Temperature",true));
                sensorTypes.add(new SensorType("pressure", "Presion",true));
                sensorTypes.add(new SensorType("bpm", "Ritmo Cardiaco",true));
                sensorTypes.add(new SensorType("spo2", "Saturacion",true));

                SensorType.saveInTx(sensorTypes);
            }
        }catch(Exception ex){
            ex.toString();
        }
    }

    public static List<SensorType> getSensorTypes() {
        return SensorType.listAll(SensorType.class);
    }

    public static  SensorType getSensorType(String name){
        SensorType result = null ;
        List<SensorType> query;

        query = SensorType.find(SensorType.class, "name = ?", name);
        if(query != null && !query.isEmpty()) {
            result = query.get(0);
        }

        return result;
    }

}
