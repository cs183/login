package com.example.login;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created with IntelliJ IDEA.
 * User: cs
 * Date: 18.01.14
 * Time: 10:32
 * To change this template use File | Settings | File Templates.
 */
public class PrefParameterSetting extends PreferenceFragment {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_parameter_setting);
    }
}
