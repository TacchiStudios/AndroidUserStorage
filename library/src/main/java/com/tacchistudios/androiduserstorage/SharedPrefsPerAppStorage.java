package com.tacchistudios.androiduserstorage;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


public class SharedPrefsPerAppStorage extends User.Storage {
    private static final String TAG = SharedPrefsPerAppStorage.class.getSimpleName();

    private final String APP_IDS_PREFS_NAME = "com.tacchistudios.user.storage.app_ids"; // Both the name of the sharedprefs for the apps, and the key for the array of IDs that are logged in

    private Context context;

    //TODO: the context is a strong requirement, so should it just be in the constructor?
    public void setContext(Context _context) {
        context = _context;
    }
    public Context getContext() {
        return context;
    }

    @Override
    public void storeTokenDetails(String token, String email, String password) {
        Log.d(TAG, "setTokenDetails: " + token);

        if (token == null) {
            // We're logging out, and want to clear *all* oauth tokens and related info from the sharedprefs this app group uses.
            prefs().edit().clear().apply();
            return;
        }

        HashMap<String, String> details = new HashMap<>();
        details.put(TOKEN, token);
        details.put(EMAIL, email);
        details.put(PASSWORD, password);

        JSONObject jsonDetails = new JSONObject(details);
        String jsonString = jsonDetails.toString();

        SharedPreferences.Editor editor = prefs().edit();
        editor.putString(context.getPackageName(), jsonString); // Store the token, email, password in the package name of this app
        editor.commit();

        Log.d(TAG, "Put: " + jsonString);

        // Also log that we have a token so other apps can discover it.
        Set<String> appIds = appIDsForSeparatedAppsWithTokens();
        appIds.add(context.getPackageName());
        setAppIDsForSeparatedAppsWithTokens(appIds);
    }

    @Nullable
    private JSONObject tokenDetails() {
        return tokenDetailsForPackageName(context.getPackageName());
    }

    @Nullable
    private JSONObject tokenDetailsForPackageName(String packageName) {
        String jsonString = prefs().getString(packageName, null);
        if (jsonString == null) {
            return null;
        }
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            return jsonObject;
        } catch (JSONException e) {
            Log.e(TAG, e.getLocalizedMessage());
            return null;
        }
    }

    @Override
    @Nullable
    public String getToken() {
        try {
            return tokenDetails().getString(TOKEN);
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage());
            return null;
        }
    }

    @Override
    @Nullable
    public String getEmail() {
        try {
            return tokenDetails().getString(EMAIL);
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage());
            return null;
        }
    }

    @Override
    @Nullable
    public String getPassword() {
        try {
            return tokenDetails().getString(PASSWORD);
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage());
            return null;
        }
    }

    @Nullable
    public Set<User.TokenDetails> tokenDetailsForSeparatedAppsThatCanBeExchangedForTokenForCurrentApp() {
        Set<User.TokenDetails> tokenDetailsSet = new HashSet<>();

        Set<String> appIds = appIDsForSeparatedAppsWithTokens();
        appIds.remove(context.getPackageName()); // TODO: Check this actually works. Does it do on String value, or reference?

        for (String appId : appIds) {
            // Get the token etc for each app ID (if email exists)
            JSONObject tokenDetails = tokenDetailsForPackageName(appId);
            if (tokenDetails != null) {
                try {
                    String email = tokenDetails.getString(EMAIL);
                    if (email != null) { // Need to check email so that anonymous sessions aren't included, as they can't be exchanged.

                        tokenDetailsSet.add(new User.TokenDetails(appId, tokenDetails.getString(TOKEN), email, tokenDetails.getString(PASSWORD)));
                    }
                } catch (JSONException e) {
                    Log.e(TAG, e.getLocalizedMessage());
                    // Just continue and give another tokenDetails a chance
                }
            }
        }

        if (!tokenDetailsSet.isEmpty()) {
            return tokenDetailsSet;
        }

        return null;
    }

    @Override
    public boolean areExchangableTokensAvailable() {
        return anyAvailableExchangeableTokenDetails() != null;
    }
    //    public User.TokenDetails tokenDetailsThatCanBeExchangedForTokenForCurrentApp() {
    public User.TokenDetails anyAvailableExchangeableTokenDetails() {
        Set<User.TokenDetails> set = tokenDetailsForSeparatedAppsThatCanBeExchangedForTokenForCurrentApp();
        if (set != null && !set.isEmpty()) {
            return (User.TokenDetails) set.toArray()[0]; // Any object should be fine.
        }

        return null;
    }
    @NonNull
    private Set<String> appIDsForSeparatedAppsWithTokens() {
        Set<String> set = prefs().getStringSet(APP_IDS_PREFS_NAME, new HashSet<String>());
        Log.d(TAG, "appIDsForSeparatedAppsWithTokens: " + set.toString());
        return set;
    }

    private void setAppIDsForSeparatedAppsWithTokens(Set<String> appIds) {
        boolean success = prefs().edit().putStringSet(APP_IDS_PREFS_NAME, appIds).commit();

        if (!success) {
            Log.e(TAG, "setAppIDsForSeparatedAppsWithTokens failed for appIds:" + appIds.toString());
        }
    }

    public SharedPreferences prefs() {
        // PreferenceManager.getDefaultSharedPreferences(context) wouldn't work, because it uses the application's package, which the other apps can't know without some hardcoding.

        SharedPreferences prefs = context.getSharedPreferences(APP_IDS_PREFS_NAME, Context.MODE_PRIVATE); // Private because it can still be accessed from other apps with the same User ID
        return prefs;
    }
}
