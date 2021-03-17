package com.example.exploreclient;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import androidx.annotation.Nullable;

@SuppressWarnings("deprecation")
public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.app_settings);
    }
}
