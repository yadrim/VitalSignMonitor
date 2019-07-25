package com.example.vitalsignmonitor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import com.example.vitalsignmonitor.Adapters.BluetoothDeviceAdapter;
import com.example.vitalsignmonitor.Model.*;
import com.example.vitalsignmonitor.Repositories.DeviceRepository;

import java.util.ArrayList;

public class ConnectionActivity extends AppCompatActivity {

    private Spinner deviceSelector;
    private Switch bluetoothSwitcher;
    private BluetoothAdapter bluetoothManager;
    private BroadcastReceiver bluetoothReceiver;
    private FloatingActionButton cmdAddDevice;

    private BluetoothDeviceAdapter bluetoothDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setTitle("Configuracion");

        bluetoothManager= BluetoothAdapter.getDefaultAdapter();
        bluetoothSwitcher = findViewById(R.id.cmdBluetooth);
        deviceSelector = findViewById(R.id.cmdSelectDevice);
        cmdAddDevice = findViewById(R.id.cmdAddDevice);

        setupBluetoothReceiver();
        setupCommands();
        setupDeviceSelector();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    private void setupBluetoothReceiver() {
        IntentFilter receiveFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);

        bluetoothReceiver= new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                int bluetoothState;

                if (action == BluetoothAdapter.ACTION_STATE_CHANGED){
                    bluetoothState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                    switch (bluetoothState) {
                        case BluetoothAdapter.STATE_ON:
                            bluetoothSwitcher.setEnabled(true);
                            bluetoothSwitcher.setChecked(true);
                            break;

                        case BluetoothAdapter.STATE_OFF:
                            bluetoothSwitcher.setEnabled(true);
                            bluetoothSwitcher.setChecked(false);
                            break;

                        case BluetoothAdapter.STATE_TURNING_OFF:
                        case BluetoothAdapter.STATE_TURNING_ON:
                            bluetoothSwitcher.setEnabled(false);
                            break;
                    }
                }

                if (action == BluetoothDevice.EXTRA_DEVICE){
                    bluetoothState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);

                    switch (bluetoothState) {
                        case BluetoothDevice.BOND_BONDED:
                            break;
                    }
                }

            }
        };

        registerReceiver(bluetoothReceiver, receiveFilter);
    }

    private  void setupCommands() {
        bluetoothSwitcher.setChecked(bluetoothManager.isEnabled());

        bluetoothSwitcher.setOnCheckedChangeListener((buttonView, isCheked) -> {
            if(isCheked){
                if(!bluetoothManager.isEnabled()){
                    Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBluetoothIntent, 1);
                }
            } else {
                bluetoothManager.disable();
            }
        });

        // Floating Button open bluetooth settings to pair bluetooth devices
        cmdAddDevice.setOnClickListener(view -> {
            Intent settings_intent = new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
            startActivityForResult(settings_intent, 0);
        });
    }

    private void setupDeviceSelector() {
        ArrayList<BluetoothDevice> devices;
        Device currentDevice;
        BluetoothDevice currentBoundedDevice;
        int defaultPosition;

        try{
            // get bounded bluetooth devices and create adapter
            devices= new ArrayList<BluetoothDevice>();
            devices.addAll(bluetoothManager.getBondedDevices());
            bluetoothDevices = new BluetoothDeviceAdapter(this, devices);

            // configure spinner
            deviceSelector.setAdapter(bluetoothDevices);

            // get current bluetooth device in data base
            currentDevice = DeviceRepository.getCurrentDevice();
            if(currentDevice != null){
                currentBoundedDevice = devices.stream()
                        .filter(d -> d.getAddress().equals(currentDevice.identifier))
                        .findFirst()
                        .orElse(null);

                if (currentBoundedDevice != null){
                    defaultPosition = devices.indexOf(currentBoundedDevice);
                    deviceSelector.setSelection(defaultPosition);
                }
            }
        }catch (Exception ex){
            ex.toString();
        }

        deviceSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDevice currentItem = (BluetoothDevice) parent.getItemAtPosition(position);
                Device device = new Device();

                device.name = currentItem.getName();
                device.identifier = currentItem.getAddress();

                DeviceRepository.setCurrentDevice(device);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(bluetoothReceiver);
    }
}
