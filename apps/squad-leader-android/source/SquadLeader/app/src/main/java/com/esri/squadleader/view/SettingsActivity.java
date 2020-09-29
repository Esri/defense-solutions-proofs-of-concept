/*******************************************************************************
 * Copyright 2013-2017 Esri
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 ******************************************************************************/
package com.esri.squadleader.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.esri.core.geometry.AngularUnit;
import com.esri.squadleader.R;

/**
 * An Activity that lets the user modify application settings. This class works
 * with res/xml/preferences.xml.
 */
public class SettingsActivity extends AppCompatActivity implements OnSharedPreferenceChangeListener {

    private static final String TAG = SettingsActivity.class.getSimpleName();

    public static class SettingsPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            updateSummaries();
        }

        private void updateSummaries() {
            SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
            for (String key : sp.getAll().keySet()) {
                updateSummary(key, sp);
            }
        }

        private void updateSummary(String key) {
            SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
            updateSummary(key, sp);
        }

        private void updateSummary(String key, SharedPreferences sp) {
            Preference pref = findPreference(key);
            if (key.equals(getString(R.string.pref_angularUnits))) {
                ListPreference listPref = (ListPreference) pref;
                try {
                    AngularUnit unit = (AngularUnit) AngularUnit.create(Integer.parseInt(listPref.getValue()));
                    pref.setSummary(unit.getDisplayName());
                } catch (Throwable t) {
                    Log.i(TAG, "Couldn't get " + getString(R.string.pref_angularUnits) + " value", t);
                }
            } else if (key.equals(getString(R.string.pref_messagePort))
                    || key.equals(getString(R.string.pref_username))
                    || key.equals(getString(R.string.pref_vehicleType))
                    || key.equals(getString(R.string.pref_sic))
                    || key.equals(getString(R.string.pref_uniqueId))) {
                EditTextPreference editTextPref = (EditTextPreference) pref;
                pref.setSummary(editTextPref.getText());
            } else if (key.equals(getString(R.string.pref_positionReportPeriod))) {
                EditTextPreference editTextPref = (EditTextPreference) pref;
                pref.setSummary(editTextPref.getText() + getString(R.string.pref_positionReportPeriod_summary));
            } else if (key.equals(getString(R.string.pref_viewshedObserverHeight))) {
                EditTextPreference editTextPref = (EditTextPreference) pref;
                pref.setSummary(editTextPref.getText() + getString(R.string.pref_viewshedObserverHeight_summary));
            }
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            if (getString(R.string.pref_resetApp).equals(preference.getKey())) {
                new AlertDialog.Builder(getActivity())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(R.string.reset_map)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getActivity().getIntent().putExtra(getString(R.string.pref_resetApp), true);
                                getActivity().setResult(RESULT_OK, getActivity().getIntent());
                            }

                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
                return true;
            } else {
                return super.onPreferenceTreeClick(preferenceScreen, preference);
            }
        }
    }

    private SettingsPreferenceFragment fragment = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragment = new SettingsPreferenceFragment();
        getFragmentManager().beginTransaction().replace(android.R.id.content, fragment).commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fragment.getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        fragment.getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        fragment.updateSummary(key);
    }

}
