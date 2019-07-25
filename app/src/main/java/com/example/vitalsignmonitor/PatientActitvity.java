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

    static Handler bluetoothIn;
    final int handlerState = 0;
    private ConnectedThread bluetoothServer;
    private ProgressDialog progressDialog;

    private BluetoothDevice bluetoothDevice = null;
    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothSocket bluetoothSocket = null;
    private StringBuilder dataRaw = new StringBuilder();
    private int currentOperation;

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

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

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        setupHandlerData();
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

    @SuppressLint("HandlerLeak")
    private void setupHandlerData() {
        bluetoothIn = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == handlerState){
                    try{
                        String readMessage = (String) msg.obj;
                        dataRaw.append(readMessage);
                        int endOfLineIndex = dataRaw.indexOf("|");
                        if (endOfLineIndex > 0) {
                            dataRaw.deleteCharAt(dataRaw.length() - 1);
                            ProcessMessage();
                        }
                    }catch(Exception e){
                        dataRaw = new StringBuilder();
                        Toast.makeText(getApplicationContext(), "Problem to process incoming message. Error: " + e.getMessage(), Toast.LENGTH_LONG);
                    }
                }
            }
        };
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

        dataRaw = new StringBuilder();
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

        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
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

        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
    }

    private void syncPatient() {
        JSONObject request = new JSONObject();
        JSONObject data = new JSONObject();

        try{
            configureBluetoothConnection();

            if(bluetoothServer == null){
                return;
            }

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
            bluetoothServer.write(request.toString() + "|");

        }catch(Exception e){
            Toast.makeText(getApplicationContext(), "Problem to sync patient. Error: " + e.getMessage(), Toast.LENGTH_LONG);
        }
    }

    private void getPatientData() {
        JSONObject request = new JSONObject();
        JSONObject data = new JSONObject();

        try{
            configureBluetoothConnection();

            if(bluetoothServer == null)
                return;

            request.put("operation","getPatientData");

            data.put("patient" , patient.number);

            request.put("payload", data);

            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Obteniendo datos de paciente desde dispositivo");
            progressDialog.setCancelable(false);
            progressDialog.show();

            currentOperation= GET_PATIENT_DATA;
            bluetoothServer.write(request.toString() + "|");

        }catch(Exception e){
            Toast.makeText(getApplicationContext(), "Problem to get patient data. Error: " + e.getMessage(), Toast.LENGTH_LONG);
        }
    }

    private void configureBluetoothConnection() {
        if(bluetoothSocket != null)
            return;

        Device device;

        try{
            device = DeviceRepository.getCurrentDevice();
            if(device == null){
                Toast.makeText(getApplicationContext(), "No device configured", Toast.LENGTH_LONG);
                return;
            }

            bluetoothDevice = bluetoothAdapter.getRemoteDevice(device.identifier);
            if(device == null){
                Toast.makeText(getApplicationContext(), "Device has no longer paired", Toast.LENGTH_LONG);
                return;
            }

            bluetoothSocket = createBluetoothSocket(bluetoothDevice);
            bluetoothServer = new ConnectedThread(bluetoothSocket);
        }catch(Exception e){
            Toast.makeText(getApplicationContext(), "Problem to connect with device", Toast.LENGTH_LONG);
        }
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connecetion with BT device using UUID
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

    //create new class for connect thread
    public class ConnectedThread extends Thread {

        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            // Keep looping to listen for received messages
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);            //read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    // Send the obtained bytes to the UI Activity via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    progressDialog.dismiss();
                    break;
                }
            }
        }
        //write method
        public void write(String input) {
            //converts entered String into bytes
            byte[] msgBuffer = input.getBytes();

            try {
                //write bytes over BT connection via outstream
                mmOutStream.write(msgBuffer);
            } catch (IOException e) {
                progressDialog.dismiss();
                Toast.makeText(getBaseContext(), "Connection Failure", Toast.LENGTH_LONG).show();
            }
        }
    }

}
