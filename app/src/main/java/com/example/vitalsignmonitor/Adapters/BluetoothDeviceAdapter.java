package com.example.vitalsignmonitor.Adapters;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.vitalsignmonitor.R;

import java.util.ArrayList;

public class BluetoothDeviceAdapter extends ArrayAdapter {

    public BluetoothDeviceAdapter(Context context, ArrayList<BluetoothDevice> devices){
        super(context, 0, devices);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    private View initView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView= LayoutInflater.from(getContext()).inflate(R.layout.device_spinner_layout, parent, false);
        }

        TextView txtName = convertView.findViewById(R.id.txtDeviceName);
        TextView txtAddress = convertView.findViewById(R.id.txtDeviceAddress);

        BluetoothDevice currentItem = (BluetoothDevice) getItem(position);
        if (currentItem != null){
            txtName.setText(currentItem.getName());
            txtAddress.setText(currentItem.getAddress());
        }

        return  convertView;
    }
}
