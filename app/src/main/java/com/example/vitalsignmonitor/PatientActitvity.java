package com.example.vitalsignmonitor;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.icu.text.RelativeDateTimeFormatter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ExpandableListAdapter;
import android.widget.Toast;

import com.example.vitalsignmonitor.Fragments.*;
import com.example.vitalsignmonitor.Model.Device;
import com.example.vitalsignmonitor.Model.Patient;
import com.example.vitalsignmonitor.Model.PatientData;
import com.example.vitalsignmonitor.Model.SensorType;
import com.example.vitalsignmonitor.Repositories.DeviceRepository;
import com.example.vitalsignmonitor.Repositories.PatientDataRepository;
import com.example.vitalsignmonitor.Repositories.PatientRepository;
import com.example.vitalsignmonitor.Repositories.SensorTypeRepository;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;

public class PatientActitvity extends AppCompatActivity
        implements PatientGeneralFragment.OnFragmentInteractionListener,
                    PatientReminderFragment.OnFragmentInteractionListener,
                    PatientHistoryFragment.OnFragmentInteractionListener
{

    public final static int SYNC_PATIENT = 0;
    public final static int GET_PATIENT_DATA = 1;

    final PatientGeneralFragment generalFragment = new PatientGeneralFragment();
    final PatientReminderFragment reminderFragment = new PatientReminderFragment();
    final PatientHistoryFragment historyFragment = new PatientHistoryFragment();
    final FragmentManager fragmentManager = getSupportFragmentManager();

    Fragment currentContent;
    Patient patient;
    BluetoothSPP bluetooth;

    private ProgressDialog progressDialog;

    private StringBuilder dataRaw = new StringBuilder();
    private int currentOperation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        setContentView(R.layout.patient_actitvity);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        generalFragment.setPatient(patient);
        historyFragment.setPatient(patient);

        currentContent = generalFragment;

        fragmentManager.beginTransaction().add(R.id.main_container, historyFragment, "3").hide(historyFragment).commit();
        fragmentManager.beginTransaction().add(R.id.main_container, reminderFragment, "2").hide(reminderFragment).commit();
        fragmentManager.beginTransaction().add(R.id.main_container, generalFragment, "1").commit();

        setupBluetoothHandler();
    }

    public void onDestroy() {
        super.onDestroy();
        bluetooth.stopService();
    }

    private void setupBluetoothHandler(){
        bluetooth = new BluetoothSPP(this);

        bluetooth.setOnDataReceivedListener((data, message) -> {
            if(dataRaw == null)
                dataRaw = new StringBuilder();

            dataRaw.append(message);

            if(dataRaw.length() == 0)
                return;

            if(dataRaw.charAt(dataRaw.length() -1) == '|' || dataRaw.charAt(dataRaw.length() -1) == '\0' || dataRaw.charAt(dataRaw.length() -1) == '\n')
                ProcessMessage();
        });

        bluetooth.setBluetoothStateListener(state -> {
            switch(state){
                case BluetoothState.STATE_CONNECTED:
                    Toast.makeText(this, "Connected with " + bluetooth.getConnectedDeviceName(), Toast.LENGTH_LONG);
                    break;

                case BluetoothState.STATE_NONE:
                    Toast.makeText(this, "Connection Failed", Toast.LENGTH_LONG);
                    break;
            }
        });

        configureBluetoothConnection();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;

            case R.id.menu_sync_patient:
                syncPatient();
                return true;

            case R.id.menu_getdata_patient:
                getPatientData();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_patient, menu);
        return true;
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.patient_nav_general:
                    fragmentManager.beginTransaction().hide(currentContent).show(generalFragment).commit();
                    currentContent = generalFragment;
                    return true;

                case R.id.patient_nav_reminders:
                    fragmentManager.beginTransaction().hide(currentContent).show(reminderFragment).commit();
                    currentContent = reminderFragment;
                    return true;

                case R.id.patient_nav_history:
                    fragmentManager.beginTransaction().hide(currentContent).show(historyFragment).commit();
                    currentContent= historyFragment;
                    return true;
            }
            return false;
        }
    };

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    private void ProcessMessage(){
        this.progressDialog.dismiss();

        try {
            switch(currentOperation){
                case SYNC_PATIENT:
                    ProcessSyncPatientResponse();
                    break;

                case GET_PATIENT_DATA:
                    ProcessGetPatientDataResponse();
                    break;
            }
        }catch (Exception e){
        }

        dataRaw = null;
    }

    private void ProcessSyncPatientResponse(){
        JSONObject response = null;
        boolean success = false;
        String message;

        try {
             response = new JSONObject(this.dataRaw.toString());
             success = response.getBoolean("success");
        } catch(Exception e) {
        }

        if(success)
            message = "Paciente sincronizado hacia dispositivo!";
        else
            message = "Error al sincronizar paciente";

        Toast.makeText(this, message, Toast.LENGTH_LONG);
    }

    private void ProcessGetPatientDataResponse(){
        JSONObject response = null;
        JSONArray data;
        JSONObject current;
        Calendar currentCalendar;
        Date currentTime;
        String groupId;

        boolean success = false;
        String message;

        try {
            response = new JSONObject(this.dataRaw.toString());
            data = response.getJSONArray("data");
            groupId = java.util.UUID.randomUUID().toString();

            for(int i = 0; i<data.length(); i++){
                current = data.getJSONObject(i);
                currentCalendar = convertToCalendar(current.getString("date"), "dd/mm/yyyy");
                currentTime = convertToTime(current.getString("time"), "hh:mm:ss");

                currentCalendar.set(Calendar.HOUR, currentTime.getHours());
                currentCalendar.set(Calendar.MINUTE, currentTime.getMinutes());
                currentCalendar.set(Calendar.SECOND, currentTime.getSeconds());

                PatientData patientData;

                // Save temperature
                patientData = new PatientData();
                patientData.patient = this.patient;
                patientData.date = currentCalendar;
                patientData.groupId = groupId;
                patientData.sensorType = SensorTypeRepository.getSensorType("temp");
                patientData.dato1 = Float.parseFloat(current.getString("temp"));
                PatientDataRepository.savePatientData(patientData);

                // Save pressure
                patientData = new PatientData();
                patientData.patient = this.patient;
                patientData.date = currentCalendar;
                patientData.groupId = groupId;
                patientData.sensorType = SensorTypeRepository.getSensorType("pressure");
                patientData.dato1 = Float.parseFloat(current.getString("pressure"));
                PatientDataRepository.savePatientData(patientData);

                //Save bpm
                patientData = new PatientData();
                patientData.patient = this.patient;
                patientData.date = currentCalendar;
                patientData.groupId = groupId;
                patientData.sensorType = SensorTypeRepository.getSensorType("bpm");
                patientData.dato1 = Float.parseFloat(current.getString("bpm"));
                PatientDataRepository.savePatientData(patientData);

                //Save
                patientData = new PatientData();
                patientData.patient = this.patient;
                patientData.date = currentCalendar;
                patientData.groupId = groupId;
                patientData.sensorType = SensorTypeRepository.getSensorType("spo2");
                patientData.dato1 = Float.parseFloat(current.getString("spo2"));
                PatientDataRepository.savePatientData(patientData);
            }

            success = true;
        } catch(Exception e) {
        }

        if(success)
            message = "Se obtuvieron datos de paciente desde dispositivo!";
        else
            message = "Error al obtener datos de paciente desde dispositivo";

        Toast.makeText(this, message, Toast.LENGTH_LONG);
    }

    private void syncPatient() {
        JSONObject request = new JSONObject();
        JSONObject data = new JSONObject();

        try{
            configureBluetoothConnection();

            request.put("operation","savePatient");

            data.put("number" , patient.number);
            data.put("name", patient.firstName + " " + patient.lastName);
            data.put("dobDay", patient.dateOfBith.get(Calendar.DAY_OF_MONTH));
            data.put("dobMonth", patient.dateOfBith.get(Calendar.MONTH));
            data.put("dobMonth", patient.dateOfBith.get(Calendar.YEAR));

            request.put("payload", data);

            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Sincronizando paciente a dispositivo");
            progressDialog.setCancelable(false);
            progressDialog.show();

            currentOperation = SYNC_PATIENT;
            //bluetoothServer.write(request.toString() + "|");

        }catch(Exception e){
            Toast.makeText(this, "Problem to sync patient. Error: " + e.getMessage(), Toast.LENGTH_LONG);
        }
    }

    private void getPatientData() {
        JSONObject request = new JSONObject();
        JSONObject data = new JSONObject();

        try{
            request.put("operation","getPatientData");

            data.put("patient" , patient.number);

            request.put("payload", data);

            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Obteniendo datos de paciente desde dispositivo");
            progressDialog.setCancelable(false);
            progressDialog.show();

            currentOperation= GET_PATIENT_DATA;
            bluetooth.send(request.toString(), true);

        }catch(Exception e){
            Toast.makeText(this, "Problem to get patient data. Error: " + e.getMessage(), Toast.LENGTH_LONG);
        }
    }

    private void configureBluetoothConnection() {
        Device device;

        try{
            device = DeviceRepository.getCurrentDevice();
            if(device == null){
                Toast.makeText(this, "No device configured", Toast.LENGTH_LONG);
                return;
            }

            bluetooth.startService(BluetoothState.DEVICE_OTHER);
            bluetooth.connect(device.identifier);
        }catch(Exception e){
            Toast.makeText(this, "Problem to connect with device", Toast.LENGTH_LONG);
        }
    }

    private Calendar convertToCalendar(String date, String format){
        Calendar result;
        Date dateResult = null;

        try{
            SimpleDateFormat formatter = new SimpleDateFormat(format);
            dateResult = formatter.parse(date);
        }catch (Exception e) {
        }

        result = Calendar.getInstance();
        if(dateResult != null)
            result.setTime(dateResult);

        return result;
    }

    private Date convertToTime(String time, String format) {
        Date result = null;

        try {
            SimpleDateFormat formatter = new SimpleDateFormat(format);
            result = formatter.parse(time);
        } catch (Exception e) {
        }

        return result;
    }
}
