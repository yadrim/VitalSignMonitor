package com.example.vitalsignmonitor.Repositories;

import com.example.vitalsignmonitor.Model.Patient;

import java.util.ArrayList;
import java.util.List;

public class PatientRepository {

    public static void checkInitialData() {
        long totalPatients;
        List<Patient> patients = new ArrayList<Patient>();

        try{
            totalPatients = Patient.count(Patient.class, null, null);
            if(totalPatients == 0){
                patients.add(new Patient(1, "Paciente","1"));
                patients.add(new Patient(2, "Paciente","2"));
                patients.add(new Patient(3, "Paciente","3"));
                patients.add(new Patient(4, "Paciente","4"));
                patients.add(new Patient(5, "Paciente","5"));

                Patient.saveInTx(patients);
            }
        }catch(Exception ex){
            ex.toString();
        }
    }

    public static List<Patient> getPatients() {
        return Patient.listAll(Patient.class);
    }

    public static Patient getPatient(long patientId) {
        Patient result;

        result = Patient.findById(Patient.class, patientId);

        return result;
    }

    public static Patient savePatient(Patient patient) {
        patient.save();
        return  patient;
    }

}
