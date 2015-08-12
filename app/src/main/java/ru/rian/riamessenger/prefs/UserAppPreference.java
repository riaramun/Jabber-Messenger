package ru.rian.riamessenger.prefs;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import javax.inject.Inject;

import ru.rian.riamessenger.RiaApplication;


public class UserAppPreference {
    private static final String FIRST_SECOND_NAME = "FIRST_SECOND_NAME";
    private static final String PASS_STRING_KEY = "PASS_STRING_KEY";
    private static final String LOGIN_STRING_KEY = "LOGIN_STRING_KEY";
    private static final String CURRENT_JID_STRING_KEY = "CURRENT_JID_STRING_KEY";
    private static final String CONNECTING_STATE_BOOL_KEY = "CONNECTING_STATE_BOOL_KEY";

    private static Editor mEditor;

    @Inject
    SharedPreferences sharedPreferences;
    
    @Inject
    public UserAppPreference() {
        RiaApplication.component().inject(this);
        mEditor = sharedPreferences.edit();

    }

    public String getFirstSecondName() {
        return sharedPreferences.getString(FIRST_SECOND_NAME, "");
    }

    public void setFirstSecondName(String value) {
        mEditor.putString(FIRST_SECOND_NAME, value).apply();
    }

    public String getJidStringKey() {
        return sharedPreferences.getString(CURRENT_JID_STRING_KEY, "");
    }

    public void setJidStringKey(String value) {
        mEditor.putString(CURRENT_JID_STRING_KEY, value).apply();
    }

    public boolean getConnectingStateKey() {
        return sharedPreferences.getBoolean(CONNECTING_STATE_BOOL_KEY, false);
    }

    public void setConnectingStateKey(boolean value) {
        mEditor.putBoolean(CONNECTING_STATE_BOOL_KEY, value).apply();
    }

    public String getPassStringKey() {
        return sharedPreferences.getString(PASS_STRING_KEY, "");
    }

    public void setPassStringKey(String value) {
        mEditor.putString(PASS_STRING_KEY, value).apply();
    }

    public String getLoginStringKey() {
        return sharedPreferences.getString(LOGIN_STRING_KEY, "");
    }

    public void setLoginStringKey(String value) {
        mEditor.putString(LOGIN_STRING_KEY, value).apply();
    }
}