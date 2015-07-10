package ru.rian.riamessenger.prefs;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import javax.inject.Inject;

import ru.rian.riamessenger.RiaApplication;


public class UserAppPreference {
    private static final String RIA_XMPP_SERVICE_FLAG = "RIA_XMPP_SERVICE_FLAG";
    private static final String PASS_STRING_KEY = "PASS_STRING_KEY";
    private static final String LOGIN_STRING_KEY = "LOGIN_STRING_KEY";
    private static final String TOKEN_STRING_KEY = "TOKEN_STRING_KEY";
    private static final String SAMPLE_LONG_KEY = "SAMPLE_LONG_KEY";
    private static final String SAMPLE_INT_KEY = "SAMPLE_INT_KEY";
    
    private static Editor mEditor;

    @Inject
    SharedPreferences sharedPreferences;
    
    @Inject
    public UserAppPreference() {
        RiaApplication.component().inject(this);
        mEditor = sharedPreferences.edit();

    }

    public boolean getRiaXmppServiceStartedFlag() {
        return sharedPreferences.getBoolean(RIA_XMPP_SERVICE_FLAG, false);
    }

    public void setRiaXmppServiceStartedFlag(boolean value) {
        mEditor.putBoolean(RIA_XMPP_SERVICE_FLAG, value).apply();
    }

    public String getTokenStringKey() {
        return sharedPreferences.getString(TOKEN_STRING_KEY, "");
    }

    public void setTokenStringKey(String value) {
        mEditor.putString(TOKEN_STRING_KEY, value).apply();
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

    public void setSampleLongKey(long value) {
        mEditor.putLong(SAMPLE_LONG_KEY, value).apply();
    }

    public long getSampleLongKey() {
        return sharedPreferences.getLong(SAMPLE_LONG_KEY, 0);
    }

    public void setSampleIntKey(Integer value) {
        mEditor.putInt(SAMPLE_INT_KEY, value).apply();
    }

    public Integer getSampleIntKey() {
        return sharedPreferences.getInt(SAMPLE_INT_KEY, 0);
    }
}