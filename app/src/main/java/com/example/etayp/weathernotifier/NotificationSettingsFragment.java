package com.example.etayp.weathernotifier;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;


public class NotificationSettingsFragment extends Fragment {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor sharedPreferencesEditor;
    private View rootView;

    private onUpdateTimeSelectedListener mListener;
    private Spinner updateTimeSpinner;

    public NotificationSettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getActivity().getSharedPreferences(MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
        sharedPreferencesEditor = sharedPreferences.edit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        ((CheckBox) rootView.findViewById(R.id.option_temperature))
                .setChecked(sharedPreferences.getBoolean(Constants.OPTION_TEMPRATURE, true));
        ((CheckBox) rootView.findViewById(R.id.option_temperature)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sharedPreferencesEditor.putBoolean(Constants.OPTION_TEMPRATURE, isChecked);
                sharedPreferencesEditor.apply();
            }
        });

        ((CheckBox) rootView.findViewById(R.id.option_wind))
                .setChecked(sharedPreferences.getBoolean(Constants.OPTION_WIND, true));
        ((CheckBox) rootView.findViewById(R.id.option_wind)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sharedPreferencesEditor.putBoolean(Constants.OPTION_WIND, isChecked);
                sharedPreferencesEditor.apply();
            }
        });

        ((CheckBox) rootView.findViewById(R.id.option_rain))
                .setChecked(sharedPreferences.getBoolean(Constants.OPTION_RAIN, true));
        ((CheckBox) rootView.findViewById(R.id.option_rain)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sharedPreferencesEditor.putBoolean(Constants.OPTION_RAIN, isChecked);
                sharedPreferencesEditor.apply();
            }
        });

        ((CheckBox) rootView.findViewById(R.id.option_humidity))
                .setChecked(sharedPreferences.getBoolean(Constants.OPTION_HUMIDITY, true));
        ((CheckBox) rootView.findViewById(R.id.option_humidity)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sharedPreferencesEditor.putBoolean(Constants.OPTION_HUMIDITY, isChecked);
            }
        });

        setupSpinner();
        return rootView;
    }

    private void setupSpinner() {
        updateTimeSpinner = ((Spinner) rootView.findViewById(R.id.spinner));
        ArrayAdapter adapter = ArrayAdapter.createFromResource(getContext(), R.array.update_times, R.layout.item_spinner);
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        updateTimeSpinner.setAdapter(adapter);
        updateTimeSpinner
                .setSelection(sharedPreferences.getInt(Constants.UPDATE_TIME_SELECTION, 0));
        updateTimeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (mListener!=null && position!=sharedPreferences.getInt(Constants.UPDATE_TIME_SELECTION,0)) mListener.onUpdateTimeSelected();
                sharedPreferencesEditor.putInt(Constants.UPDATE_TIME_SELECTION
                        , position);
                sharedPreferencesEditor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof onUpdateTimeSelectedListener) {
            mListener = (onUpdateTimeSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentTimeOutListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        sharedPreferencesEditor.putBoolean(Constants.OPTION_TEMPRATURE,
                ((CheckBox) rootView.findViewById(R.id.option_temperature)).isChecked());
        sharedPreferencesEditor.putBoolean(Constants.OPTION_WIND,
                ((CheckBox) rootView.findViewById(R.id.option_wind)).isChecked());
        sharedPreferencesEditor.putBoolean(Constants.OPTION_RAIN,
                ((CheckBox) rootView.findViewById(R.id.option_rain)).isChecked());
        sharedPreferencesEditor.putBoolean(Constants.OPTION_HUMIDITY,
                ((CheckBox) rootView.findViewById(R.id.option_humidity)).isChecked());
        sharedPreferencesEditor.putInt(Constants.UPDATE_TIME_SELECTION
                , updateTimeSpinner.getSelectedItemPosition());
        sharedPreferencesEditor.apply();
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
    interface onUpdateTimeSelectedListener {
        void onUpdateTimeSelected();
    }
}
