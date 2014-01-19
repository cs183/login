package com.example.login;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: cs
 * Date: 18.01.14
 * Time: 18:23
 * To change this template use File | Settings | File Templates.
 */
public class PrefParameterSettings extends PreferenceActivity {
    public void onBuildHeaders(List<PreferenceActivity.Header> target) {
        SharedPreferences settings = getSharedPreferences("Sensor", MODE_PRIVATE);
        for(int i=0; i<8; i++) {
            Header h = new Header();
            h.title = "Параметр " + (i+1);
            h.summary = settings.getString("Parameter_" + i + "_Name", "Неустановлено");
            h.fragment = "com.example.login.PrefParameterFragment";
            h.fragmentArguments = new Bundle();
            h.fragmentArguments.putInt("ParameterNo", i);
            target.add(h);
        }
    }
}
