package com.example.vitalsignmonitor;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.example.vitalsignmonitor.Adapters.PatientAdapter;
import com.example.vitalsignmonitor.Model.Patient;
import com.example.vitalsignmonitor.Repositories.PatientRepository;

public class PatientListActivity extends AppCompatActivity {

    PatientAdapter patients;
    ListView patientList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_list);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setTitle("Pacientes");

        patientList = findViewById(R.id.patientList);
        patientList.setOnItemClickListener((adapterView, view, position, id) -> {
            Patient patient = patients.getItem(position);

            Intent listIntent = new Intent(this, PatientActitvity.class);
            listIntent.putExtra("patientId", patient.getId());
            startActivity(listIntent);
        });

        refreshPatients();
    }

    @Override
    protected void onResume() {
        super.onResume();

        refreshPatients();
    }

    private void refreshPatients() {
        patients = new PatientAdapter(this, PatientRepository.getPatients());
        patientList.setAdapter(patients);
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
}
