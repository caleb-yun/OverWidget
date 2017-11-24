package com.cogentworks.overwidget;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.cogentworks.overwidget.R;

/**
 * Created by cyun on 11/23/17.
 */

public class WidgetPrefFragment extends PreferenceFragment {
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        // Defining PreferenceChangeListener
        Preference.OnPreferenceChangeListener onPreferenceChangeListener = new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Preference.OnPreferenceChangeListener listener = (Preference.OnPreferenceChangeListener) getActivity();
                listener.onPreferenceChange(preference, newValue);
                return true;
            }
        };

        // Getting the ListPreference from the Preference Resource
        EditTextPreference p = (EditTextPreference) getPreferenceManager().findPreference("username");
        // Setting Preference change listener for the ListPreference
        p.setOnPreferenceChangeListener(onPreferenceChangeListener);
    }
}
