package com.example.vitalsignmonitor.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.vitalsignmonitor.Model.Patient;
import com.example.vitalsignmonitor.Model.PatientDataGroup;
import com.example.vitalsignmonitor.R;

import java.text.SimpleDateFormat;
import java.util.List;

public class PatientDataAdapter extends ArrayAdapter<PatientDataGroup> {

    public PatientDataAdapter(Context context, List<PatientDataGroup> objects) {
        super(context, 0, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView= LayoutInflater.from(getContext()).inflate(R.layout.patient_data_row_layout, parent, false);
        }

        SimpleDateFormat formatter = new SimpleDateFormat("EEEEE dd-mm-yyyy");
        SimpleDateFormat timeFormatter = new SimpleDateFormat("hh:mm a");

        TextView txtDate = convertView.findViewById(R.id.txtPatientTitle);
        TextView txtTime = convertView.findViewById(R.id.txtPatientTitle);
        TextView txtPressure = convertView.findViewById(R.id.txtPatientTitle);
        TextView txtTemp = convertView.findViewById(R.id.txtPatientTitle);
        TextView txtHeartRate = convertView.findViewById(R.id.txtPatientTitle);

        // Lead actual.
        PatientDataGroup data = getItem(position);

        // Setup.
        txtDate.setText(formatter.format(data.date));
        txtDate.setText(timeFormatter.format(data.date));
        txtTemp.setText(String.format("Temperatura: %.2f C", data.temperature));
        txtPressure.setText(String.format("Presion: %.2f", data.pressure));
        txtHeartRate.setText(String.format("Bpm: %.2f / SPO: %.2f", data.bpm, data.spo2));

        return convertView;
    }

}
