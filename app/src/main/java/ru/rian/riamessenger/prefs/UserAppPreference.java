package ru.rian.riamessenger.prefs;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;

import org.jxmpp.jid.EntityFullJid;

import javax.inject.Inject;

import ru.rian.riamessenger.RiaApplication;


public class UserAppPreference {
    static final String FIRST_SECOND_NAME = "FIRST_SECOND_NAME";
    static final String PASS_STRING_KEY = "PASS_STRING_KEY";
    static final String ROSTER_STRING_KEY = "ROSTER_STRING_KEY";
    static final String LOGIN_STRING_KEY = "LOGIN_STRING_KEY";
    static final String CURRENT_JID_STRING_KEY = "CURRENT_JID_STRING_KEY";
    static final String CONNECTING_STATE_BOOL_KEY = "CONNECTING_STATE_BOOL_KEY";
    static final String XMPP_RES_STR_KEY = "XMPP_RES_STR_KEY";

    static Editor mEditor;

    @Inject
    public
    SharedPreferences sharedPreferences;

    @Inject
    public UserAppPreference() {
        RiaApplication.component().inject(this);
        mEditor = sharedPreferences.edit();

    }

    public String getFirstSecondName() {
        String ret = sharedPreferences.getString(FIRST_SECOND_NAME, "");
        if (TextUtils.isEmpty(ret)) ret = getLoginStringKey();
        return ret;
    }

    public void setFirstSecondName(String value) {
        mEditor.putString(FIRST_SECOND_NAME, value).apply();
    }

    public String getUserStringKey() {
        return sharedPreferences.getString(CURRENT_JID_STRING_KEY, "");
    }

    public void setJidStringKey(EntityFullJid value) {
        mEditor.putString(CURRENT_JID_STRING_KEY, value.asEntityBareJidString()).apply();
    }

    public boolean getConnectingStateKey() {
        return sharedPreferences.getBoolean(CONNECTING_STATE_BOOL_KEY, false);
    }

    public void setConnectingStateKey(boolean value) {
        mEditor.putBoolean(CONNECTING_STATE_BOOL_KEY, value).apply();
    }

    public String getUniqueXmppRes() {
        String res = sharedPreferences.getString(XMPP_RES_STR_KEY, "");
        if (TextUtils.isEmpty(res)) {
            res = "ria_mobile_andr_" + Math.random();
            mEditor.putString(XMPP_RES_STR_KEY, res).apply();
        }
        return sharedPreferences.getString(XMPP_RES_STR_KEY, "");
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

    public String getRosterPathStringKey() {
        return sharedPreferences.getString(ROSTER_STRING_KEY, "");
    }

    public void setRosterPathStringKey(String value) {
        mEditor.putString(ROSTER_STRING_KEY, value).apply();
    }
}