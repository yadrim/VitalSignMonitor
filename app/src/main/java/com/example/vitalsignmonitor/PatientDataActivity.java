package com.example.vitalsignmonitor;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.vitalsignmonitor.Adapters.PatientAdapter;
import com.example.vitalsignmonitor.Model.Patient;
import com.example.vitalsignmonitor.Model.PatientData;
import com.example.vitalsignmonitor.Repositories.PatientDataRepository;
import com.example.vitalsignmonitor.Repositories.PatientRepository;
import com.example.vitalsignmonitor.Repositories.SensorTypeRepository;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class PatientDataActivity extends AppCompatActivity {

    private Patient patient;
    private final Calendar calendar = Calendar.getInstance();

    private TextInputEditText txtDataDate;
    private TextInputEditText txtDataTime;
    private TextInputEditText txtTemperature;
    private TextInputEditText txtPressure;
    private TextInputEditText txtBPM;
    private TextInputEditText txtSPO;
    private Button cmdSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_data);

        Bundle extras = getIntent().getExtras();
        long patientId = (long) extras.get("patientId");

        patient = PatientRepository.getPatient(patientId);
        if(patient == null) {
            finish();
            return;
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setTitle("Paciente " + patient.number);

        txtDataDate = findViewById(R.id.txtPatientDataDate);
        txtDataTime = findViewById(R.id.txtPatientDataTime);

        txtTemperature = findViewById(R.id.txtTemperature);
        txtPressure = findViewById(R.id.txtPressure);
        txtBPM = findViewById(R.id.txtBPM);
        txtSPO = findViewById(R.id.txtSPO);
        cmdSave= findViewById(R.id.cmdSavePatientData);

        setupTextHandler();
        setupDateTime();
        setupCommands();
        updateDateTime();

        validateCanSave();
    }

    private void validateCanSave() {
        boolean canSave = false;
        boolean hasTemp, hasPressure, hasBPM, hasSPO;

        try {

            hasTemp = Float.parseFloat(txtTemperature.getText().toString()) > 0;
            hasPressure = Float.parseFloat(txtPressure.getText().toString()) > 0;
            hasBPM = Float.parseFloat(txtBPM.getText().toString()) > 0;
            hasSPO = Float.parseFloat(txtSPO.getText().toString()) > 0;

            canSave = hasTemp || hasPressure || hasBPM || hasSPO;
        }catch (Exception e){
        }

        cmdSave.setEnabled(canSave);
    }

    private void updateDateTime() {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yy", Locale.US);
        SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm aa", Locale.US);

        txtDataDate.setText(dateFormatter.format(calendar.getTime()));
        txtDataTime.setText(timeFormatter.format(calendar.getTime()));
    }

    private void setupTextHandler(){
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                validateCanSave();
            }
        };

        txtTemperature.addTextChangedListener(watcher);
        txtTemperature.setText("0");

        txtPressure.addTextChangedListener(watcher);
        txtPressure.setText("0");

        txtBPM.addTextChangedListener(watcher);
        txtBPM.setText("0");

        txtSPO.addTextChangedListener(watcher);
        txtSPO.setText("0");
    }

    private void setupDateTime() {
        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                updateDateTime();
                validateCanSave();
            }
        };

        TimePickerDialog.OnTimeSetListener time = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);

                updateDateTime();
                validateCanSave();
            }
        };

        txtDataDate.setOnClickListener((View view) ->{
            DatePickerDialog datePicker = new DatePickerDialog(this,
                    AlertDialog.THEME_HOLO_LIGHT,
                    date,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));

            datePicker.show();
        });

        txtDataTime.setOnClickListener((View view) ->{
            TimePickerDialog timePicker = new TimePickerDialog(this,
                    time,
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false);

            timePicker.show();
        });
    }

    private void setupCommands() {
        cmdSave.setOnClickListener((View view) -> {
            String groupId;
            Float tempValue, pressureValue, bpmValue, spoValue;

            try{
                groupId = java.util.UUID.randomUUID().toString();

                tempValue = Float.parseFloat(txtTemperature.getText().toString());
                pressureValue = Float.parseFloat(txtPressure.getText().toString());
                bpmValue = Float.parseFloat(txtBPM.getText().toString());
                spoValue = Float.parseFloat(txtSPO.getText().toString());

                if(tempValue >0)
                    savePatientData(groupId, "temp", tempValue);

                if(pressureValue >0)
                    savePatientData(groupId, "pressure", pressureValue);

                if(bpmValue >0)
                    savePatientData(groupId, "bpm", bpmValue);

                if(spoValue >0)
                    savePatientData(groupId, "spo", spoValue);

                Toast.makeText(getApplicationContext(), "Datos registrados exitosamente", Toast.LENGTH_LONG).show();
                finish();
            }catch (Exception e){
                Toast.makeText(getApplicationContext(), "Problema al registrar datos del paciente. Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void savePatientData(String groupId, String sensorType, Float value) {
        PatientData patientData;

        // Save temperature
        patientData = new PatientData();
        patientData.patient = this.patient;
        patientData.date = calendar;
        patientData.groupId = groupId;
        patientData.sensorType = SensorTypeRepository.getSensorType(sensorType);
        patientData.dato1 = value;
        PatientDataRepository.savePatientData(patientData);
    }

}
