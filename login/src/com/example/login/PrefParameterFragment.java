package com.example.login;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.*;
import android.text.*;

import android.os.Bundle;

/**
 * Created with IntelliJ IDEA.
 * User: cs
 * Date: 18.01.14
 * Time: 10:32
 * To change this template use File | Settings | File Templates.
 */
public class PrefParameterFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private String _paramPrefix;
    private int _paramNo = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _paramNo = getArguments().getInt("ParameterNo", -1);
        if (_paramNo < 0 ) {
            throw new IllegalArgumentException("Wrong parameter number");
        }
        _paramPrefix = "Parameter_" + _paramNo + "_";
        getPreferenceManager().setSharedPreferencesName("Sensor");
        getPreferenceManager().setSharedPreferencesMode(Context.MODE_PRIVATE);
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        initScreen();
    }

    @Override
    public void onDestroy() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    private void initScreen() {
        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(this.getActivity());
        setPreferenceScreen(screen);
        SharedPreferences settings = getPreferenceManager().getSharedPreferences();

        Preference paramName = getPreferenceManager().findPreference(_paramPrefix + "Name");
        if (paramName == null) {
            paramName = new EditTextPreference(this.getActivity());
            paramName.setKey(_paramPrefix + "Name");
            paramName.setTitle("Наименование параметра");
            paramName.setSummary(settings.getString(_paramPrefix + "Name", "Не установлено"));
            ((EditTextPreference)paramName).getEditText().setSingleLine();
            paramName.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    return !(newValue == null || newValue.equals(""));
                }
            });
        }
        screen.addPreference(paramName);

        Preference paramMin = getPreferenceManager().findPreference(_paramPrefix + "Min");
        if (paramMin == null) {
            paramMin =  new EditTextPreference(this.getActivity());
            paramMin.setKey(_paramPrefix + "Min");
            paramMin.setTitle("Минимальное значение параметра");
            paramMin.setSummary(settings.getString(_paramPrefix + "Min", "Не установлено"));
            ((EditTextPreference)paramMin).getEditText().setRawInputType(InputType.TYPE_CLASS_NUMBER);
            ((EditTextPreference)paramMin).getEditText().setSingleLine();
            paramMin.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        double value = Double.valueOf((String)newValue);
                    } catch (Exception e) {
                        return false;
                    }
                    return true;
                }
            });
        }
        screen.addPreference(paramMin);

        Preference paramMax = getPreferenceManager().findPreference(_paramPrefix + "Max");
        if (paramMax == null) {
            paramMax = new EditTextPreference(this.getActivity());
            paramMax.setKey(_paramPrefix + "Max");
            paramMax.setTitle("Максимальное значение параметра");
            paramMax.setSummary(settings.getString(_paramPrefix + "Max", "Не установлено"));
            ((EditTextPreference)paramMax).getEditText().setRawInputType(InputType.TYPE_CLASS_NUMBER);
            ((EditTextPreference)paramMax).getEditText().setSingleLine();
        }
        screen.addPreference(paramMax);

        Preference paramDefault = getPreferenceManager().findPreference(_paramPrefix + "Default");
        if (paramDefault == null) {
            paramDefault = new EditTextPreference(this.getActivity());
            paramDefault.setKey(_paramPrefix + "Default");
            paramDefault.setTitle("Значение параметра по умолчанию");
            paramDefault.setSummary(settings.getString(_paramPrefix + "Default", "Не установлено"));
            ((EditTextPreference)paramDefault).getEditText()
                    .setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            ((EditTextPreference)paramDefault).getEditText().setSingleLine();
        }
        screen.addPreference(paramDefault);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference pref = getPreferenceScreen().findPreference(key);
        if (pref != null) {
            pref.setSummary(sharedPreferences.getString(key, "Не установлено"));
        }
    }
}
