package com.example.login;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created with IntelliJ IDEA.
 * User: csulimin
 * Date: 23.12.13
 * Time: 16:05
 * To change this template use File | Settings | File Templates.
 */
public class PrefDeviceParameterSettings extends PreferenceFragment{
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_device_parameter_settings);
    }
}
