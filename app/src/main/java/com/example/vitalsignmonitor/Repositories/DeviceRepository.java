package com.example.vitalsignmonitor.Repositories;

import com.example.vitalsignmonitor.Model.Device;

import java.util.Calendar;
import java.util.List;

public class DeviceRepository {

    public static Device getCurrentDevice() {
        Device result = null ;
        List<Device> query;

        query = Device.find(Device.class, "current = ?", "1");
        if(query != null && !query.isEmpty()) {
            result = query.get(0);
        }

        return result;
    }

    public static Device setCurrentDevice(Device device) {
        Device result = null;
        List<Device> query;

        query = Device.find(Device.class, "identifier = ?", device.identifier);
        if(query != null && !query.isEmpty()) {
            result = query.get(0);
        }

        if(result == null){
            result = device;
            result.createdDate = Calendar.getInstance();
            result.lastConnection= Calendar.getInstance();
        }

        // set default values
        result.active = true;
        result.current = true;

        // set all device as current = false
        Device.executeQuery("UPDATE Device SET current = 0");

        // save current device
        result.save();

        return  result;
    }

}
