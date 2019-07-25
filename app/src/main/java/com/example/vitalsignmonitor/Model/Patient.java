package com.example.vitalsignmonitor.Model;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Patient extends SugarRecord<Patient> {

    public final static int MALE = 0;
    public final static int FEMALE = 1;

    @Ignore
    public int idPatient;
    public int number;
    public String firstName;
    public String lastName;
    public Calendar dateOfBith;
    public String bloodType;
    public double weight;
    public int gender;

    public Calendar lastSync;
    public boolean needSync;

    public Patient() {}

    public Patient(int pNumber, String pFirstName, String pLastName) {
        number= pNumber;
        firstName = pFirstName;
        lastName = pLastName;
        needSync = false;
        dateOfBith = Calendar.getInstance();
        lastSync = Calendar.getInstance();
        gender = 0;
        bloodType = null;
    }

    public String getTitle() {
        return String.format("#%d - %s %s", number, firstName, lastName);
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getDescription() {
        return "Descripcion del paciente";
    }

}
