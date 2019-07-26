package com.example.vitalsignmonitor.Fragments;

import com.example.vitalsignmonitor.Model.Patient;

public interface IPatientHandler{
    void setPatient(Patient patient);
    void refresh();
}
