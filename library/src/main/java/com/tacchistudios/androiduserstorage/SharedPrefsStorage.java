package com.tacchistudios.androiduserstorage;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SharedPrefsStorage extends User.Storage {
    protected SharedPreferences getPrefs() {
        return PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    public SharedPrefsStorage(Context context) {
        this.context = context;
    }

    @Override
    public User.TokenDetails anyAvailableExchangeableTokenDetails() {
        return null;
    }

    @Override
    protected void storeTokenDetails(String token, String email, String password) {
        SharedPreferences.Editor prefs = getPrefs().edit();
        prefs.putString(TOKEN, token);
        prefs.putString(EMAIL, email);
        prefs.putString(PASSWORD, password);
        prefs.commit();
    }

    @Override
    public String getToken() {
        return getPrefs().getString(TOKEN, null);
    }

    @Override
    public String getEmail() {
        return getPrefs().getString(EMAIL, null);
    }

    @Override
    public String getPassword() {
        return getPrefs().getString(PASSWORD, null);
    }

    @Override
    public boolean areExchangableTokensAvailable() {
        return false;
    }

}
