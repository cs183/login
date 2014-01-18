package com.example.login;

import android.preference.PreferenceActivity;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: csulimin
 * Date: 23.12.13
 * Time: 13:48
 * To change this template use File | Settings | File Templates.
 */
public class Preferences extends PreferenceActivity {
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers,  target);
    }
}
