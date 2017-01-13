package io.github.froodyapp.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import io.github.froodyapp.R;
import io.github.froodyapp.api.invoker.ApiClient;
import io.github.froodyapp.api.invoker.Configuration;
import io.github.froodyapp.service.UserRegisterer;
import io.github.froodyapp.util.AppSettings;
import io.github.froodyapp.util.BlockCache;
import io.github.froodyapp.util.MyEntriesHelper;

/**
 * Activity for settings
 */
public class SettingActivity extends PreferenceActivity {
    //########################
    //## Members
    //########################
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }

    //########################
    //## Settings fragment
    //########################
    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        private SharedPreferences sharePrefs;
        private AppSettings appSettings;

        public void onCreate(Bundle savedInstances) {
            super.onCreate(savedInstances);
            getPreferenceManager().setSharedPreferencesName(AppSettings.SHARED_PREF_APP);
            addPreferencesFromResource(R.xml.preferences);
            sharePrefs = getPreferenceScreen().getSharedPreferences();
            sharePrefs.registerOnSharedPreferenceChangeListener(this);
            appSettings = new AppSettings(getActivity());
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            updatePreference(findPreference(key));

            Context context = getActivity() != null ? getActivity().getApplicationContext() : null;
            if (context == null) {
                return;
            }

            if (key.equals(context.getString(R.string.pref_key__froody_server))) {
                BlockCache.getInstance().clearCache(context);
                ApiClient apiClient = Configuration.getDefaultApiClient();
                apiClient.setBasePath(appSettings.getFroodyServer());
                appSettings.setFroodyUserId(-1);
                UserRegisterer.registerUserIfNotRegistered(context);
            }
        }

        // Set preference details that are not set by PreferenceFragment itself
        private void updatePreference(Preference pref) {
            if (pref == null) {
                return;
            }
            if (pref instanceof EditTextPreference) {
                EditTextPreference textPref = (EditTextPreference) pref;
                textPref.setSummary(textPref.getText());
            }
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen screen, Preference preference) {
            // Some preference was clicked
            Context context = getActivity().getApplicationContext();
            switch (preference.getTitleRes()) {
                case R.string.pref_title__clear_cache: {
                    BlockCache.getInstance().clearCache(context);
                    break;
                }

                case R.string.pref_title__reset_app: {
                    BlockCache.getInstance().clearCache(context);
                    new MyEntriesHelper(context).deleteMyEntries();
                    appSettings.resetSettings();
                    System.exit(0);
                    break;
                }
            }
            return super.onPreferenceTreeClick(screen, preference);
        }

        @Override
        public void onPause() {
            super.onPause();
        }
    }
}
