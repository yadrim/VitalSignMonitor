package com.example.vitalsignmonitor.Fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.example.vitalsignmonitor.Adapters.PatientAdapter;
import com.example.vitalsignmonitor.Adapters.PatientDataAdapter;
import com.example.vitalsignmonitor.Model.Patient;
import com.example.vitalsignmonitor.PatientActitvity;
import com.example.vitalsignmonitor.R;
import com.example.vitalsignmonitor.Repositories.PatientDataRepository;
import com.example.vitalsignmonitor.Repositories.PatientRepository;

public class PatientHistoryFragment extends Fragment
    implements IPatientHandler {

    private OnFragmentInteractionListener mListener;

    private Patient patient;
    PatientDataAdapter patients;
    ListView patientList;

    public PatientHistoryFragment() {
        // Required empty public constructor
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
        View view = inflater.inflate(R.layout.fragment_patient_history, container, false);

        patientList = view.findViewById(R.id.patientList);

        refreshPatients();

        return view;
    }

    private void refreshPatients() {
        try{
            patients = new PatientDataAdapter(this.getContext(), PatientDataRepository.getPatientDataGroup(this.patient));
            patientList.setAdapter(patients);
        }catch (Exception e){
            Toast toast = Toast.makeText(getContext(), "Problema al mostrar datos de paciente. Error: "+ e.getMessage(), Toast.LENGTH_SHORT);
            toast.show();
        }
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

    public void setPatient(Patient patient){
        this.patient = patient;
    }

    public void refresh() {
        refreshPatients();
    }
}
