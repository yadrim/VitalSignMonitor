package com.example.vitalsignmonitor.Fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.vitalsignmonitor.Model.Patient;
import com.example.vitalsignmonitor.R;
import com.example.vitalsignmonitor.Repositories.PatientRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class PatientGeneralFragment extends Fragment
        implements IPatientHandler {

    private OnFragmentInteractionListener mListener;

    private Patient patient;
    private Spinner patientGenders;
    private TextInputEditText txtPatientDob;
    private TextInputEditText txtPatientName;
    private TextInputEditText txtPatientLastName;

    Calendar myCalendar = Calendar.getInstance();
    IPatientListener mPatientListener;

    public PatientGeneralFragment() {
        // Required empty public constructor
    }

    public void SetPatientListener(IPatientListener listener){
        mPatientListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_patient_general, container, false);

        txtPatientName = view.findViewById(R.id.txtPatientName);
        txtPatientLastName = view.findViewById(R.id.txtPatientLastName);
        txtPatientDob = view.findViewById(R.id.txtPatientDob);
        patientGenders = view.findViewById(R.id.cbxPatientGender);

        setupGenders();
        setupDateOfBirth();
        setPatientInformation();

        return view;
    }

    private void setupGenders() {
        List<String> genders = new ArrayList<>();
        genders.add("Hombre");
        genders.add("Mujer");

        ArrayAdapter<String> genderAdapter =  new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.simple_spinner_item,
                genders
        );

        patientGenders.setAdapter(genderAdapter);
    }

    private void setupDateOfBirth() {
        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updatePatientDob();
            }

        };

        txtPatientDob.setOnClickListener((View v) ->{
            DatePickerDialog datePicker = new DatePickerDialog(getContext(),
                    AlertDialog.THEME_HOLO_LIGHT,
                    date,
                    myCalendar.get(Calendar.YEAR),
                    myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH));

            datePicker.show();
        });
    }

    private void updatePatientDob() {
        String myFormat = "MM/dd/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        txtPatientDob.setText(sdf.format(myCalendar.getTime()));
    }

    public void setPatient(Patient patient){
        this.patient = patient;
    }

    public void refresh() {}

    private void setPatientInformation() {
        if(patient == null)
            return;

        txtPatientName.setText(patient.firstName);
        txtPatientLastName.setText(patient.lastName);

        if(!patient.dateOfBith.equals(Calendar.getInstance())){
            myCalendar = patient.dateOfBith;
            updatePatientDob();
        }

        patientGenders.setSelection(patient.gender);
    }

    private void getPatientInformation() {
        if(patient == null)
            return;

        patient.firstName = txtPatientName.getText().toString();
        patient.lastName= txtPatientLastName.getText().toString();
        patient.dateOfBith = myCalendar;
        patient.gender = patientGenders.getSelectedItemPosition();
    }

    private void savePatient() {
        getPatientInformation();
        PatientRepository.savePatient(patient);

        if(mPatientListener != null)
            mPatientListener.onPatientChanged(patient);

        Toast toast = Toast.makeText(getContext(), "Paciente guardado!", Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_patient_general, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_save:
                savePatient();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}