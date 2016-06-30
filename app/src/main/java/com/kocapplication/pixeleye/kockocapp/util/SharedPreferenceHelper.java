package com.kocapplication.pixeleye.kockocapp.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Han_ on 2016-06-30.
 */
public class SharedPreferenceHelper {
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    public SharedPreferenceHelper(Context context) {
        preferences = context.getSharedPreferences("PREFERENCE", Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    public boolean put(String key, String value) {
        editor.putString(key, value);
        return editor.commit();
    }

    public boolean put(String key, int value) {
        editor.putInt(key, value);
        return editor.commit();
    }

    public boolean put(String key, boolean value) {
        editor.putBoolean(key, value);
        return editor.commit();
    }

    public String get(String key, String defaultValue) {
        return preferences.getString(key, defaultValue);
    }

    public int get(String key, int defaultValue) {
        return preferences.getInt(key, defaultValue);
    }

    public boolean get(String key, boolean defaultValue) {
        return preferences.getBoolean(key, defaultValue);
    }
}
