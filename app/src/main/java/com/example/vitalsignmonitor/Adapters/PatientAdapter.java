package com.example.vitalsignmonitor.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.vitalsignmonitor.Model.Patient;
import com.example.vitalsignmonitor.R;

import java.util.List;

public class PatientAdapter extends ArrayAdapter<Patient> {

    public PatientAdapter(Context context, List<Patient> objects) {
        super(context, 0, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView= LayoutInflater.from(getContext()).inflate(R.layout.patient_row_layout, parent, false);
        }

        TextView name = convertView.findViewById(R.id.txtPatientTitle);
        TextView description1 =  convertView.findViewById(R.id.txtPatientDescription1);
        TextView description2 =  convertView.findViewById(R.id.txtPatientDescription2);

        // Lead actual.
        Patient patient = getItem(position);

        // Setup.
        name.setText(patient.getTitle());

        return convertView;
    }
}
