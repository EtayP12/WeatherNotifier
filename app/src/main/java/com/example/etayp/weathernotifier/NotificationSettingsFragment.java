package com.example.etayp.weathernotifier;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link NotificationSettingsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link NotificationSettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NotificationSettingsFragment extends Fragment {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor sharedPreferencesEditor;
    private View rootView;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public NotificationSettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NotificationSettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NotificationSettingsFragment newInstance(String param1, String param2) {
        NotificationSettingsFragment fragment = new NotificationSettingsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        sharedPreferences = getActivity().getSharedPreferences(MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
        sharedPreferencesEditor = sharedPreferences.edit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_notification_settings, container, false);
        ((CheckBox) rootView.findViewById(R.id.temperature_option))
                .setChecked(sharedPreferences.getBoolean("templature_option", true));
        ((CheckBox) rootView.findViewById(R.id.uv_option))
                .setChecked(sharedPreferences.getBoolean("uv_option", true));
        ((CheckBox) rootView.findViewById(R.id.rain_option))
                .setChecked(sharedPreferences.getBoolean("rain_option", true));
        ((CheckBox) rootView.findViewById(R.id.pollution_option))
                .setChecked(sharedPreferences.getBoolean("pollution_option", true));
        return rootView;
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
        sharedPreferencesEditor.putBoolean("temperature_option",
                ((CheckBox) rootView.findViewById(R.id.temperature_option)).isChecked());
        sharedPreferencesEditor.putBoolean("uv_option",
                ((CheckBox) rootView.findViewById(R.id.uv_option)).isChecked());
        sharedPreferencesEditor.putBoolean("rain_option",
                ((CheckBox) rootView.findViewById(R.id.rain_option)).isChecked());
        sharedPreferencesEditor.putBoolean("pollution_option",
                ((CheckBox) rootView.findViewById(R.id.pollution_option)).isChecked());
        sharedPreferencesEditor.commit();
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
