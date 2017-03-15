package com.tacchistudios.androiduserstorage;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class ContentProviderPerAppStorage implements User.Storage {
    private static final String TAG = ContentProviderPerAppStorage.class.getSimpleName();

    private Context context;
    public Context getContext() {
        return context;
    }

    public ContentProviderPerAppStorage(Context _context) {
        context = _context;
    }

    @Override
    public void setTokenDetails(String token, String email, String password) {
        Log.d(TAG, "setTokenDetails: " + token);

        if (token == null) {
            clearAllTokens();
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
    }

    private void clearAllTokens() {
        // We're logging out, and want to clear *all* oauth tokens and related info from the ContentProviders this app group uses.

        // So we start with the local...
        SharedPreferences.Editor editor = prefs().edit();
        editor.remove(context.getPackageName()); // Store the token, email, password in the package name of this app
        editor.commit();

        // Then clear all for ContentProviders
        Set<String> appIds = appIDsForSeparatedAppsWithContentProviders();
        for (String appId : appIds) {
            Uri uri = ContentProvider.clearTokenURIForAuthority(appId);
            int returnValue = context.getContentResolver().delete(uri, null, null);
            // TBH this is probably all we need to do?
        }
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
//            Log.e(TAG, e.getLocalizedMessage());
            return null;
        }
    }

    @Override
    @Nullable
    public String getEmail() {
        try {
            return tokenDetails().getString(EMAIL);
        } catch (Exception e) {
//            Log.e(TAG, e.getLocalizedMessage());
            return null;
        }
    }

    @Override
    @Nullable
    public String getPassword() {
        try {
            return tokenDetails().getString(PASSWORD);
        } catch (Exception e) {
//            Log.e(TAG, e.getLocalizedMessage());
            return null;
        }
    }

    @Override
    @Nullable
    public Set<User.TokenDetails> tokenDetailsForSeparatedAppsThatCanBeExchangedForTokenForCurrentApp() {
        Set<User.TokenDetails> tokenDetailsSet = new HashSet<>();

        Set<String> appIds = appIDsForSeparatedAppsWithContentProviders();

        for (String appId : appIds) {
            // Get the token etc for each app ID (if email exists)

            Uri uri = ContentProvider.tokenURIForAuthority(appId);

            Log.d(TAG, "Getting token cursor for Uri: " + uri.toString());

            // Use the AppID in the query...
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                Log.d(TAG, "User logged in. Got cursor for: " + uri.toString());

                String email = cursor.getString(cursor.getColumnIndex(EMAIL));
                if (email != null) { // Need to check email so that anonymous sessions aren't included, as they can't be exchanged.
                    tokenDetailsSet.add(new User.TokenDetails(cursor.getString(cursor.getColumnIndex("package")), cursor.getString(cursor.getColumnIndex(TOKEN)), email, cursor.getString(cursor.getColumnIndex(PASSWORD))));
                }
            } else {
                Log.d(TAG, "User not logged in for: " + uri.toString());
            }
        }

        if (!tokenDetailsSet.isEmpty()) {
            return tokenDetailsSet;
        }

        return null;
    }

    // Returns any exchangeable token details
    public User.TokenDetails tokenDetailsThatCanBeExchangedForTokenForCurrentApp() {
        Set<User.TokenDetails> set = tokenDetailsForSeparatedAppsThatCanBeExchangedForTokenForCurrentApp();
        if (set != null && !set.isEmpty()) {
            return (User.TokenDetails) set.toArray()[0]; // Any object should be fine.
        }

        return null;
    }

    private Set<String> appIDsForSeparatedAppsWithContentProviders() {
        List<ProviderInfo> providers = context.getPackageManager().queryContentProviders(null, 0, 0);

        Set<String> appIDs = new HashSet<>();

        for (ProviderInfo provider : providers) {
            if (provider.name.contains("com.tacchistudios.androiduserstorage.ContentProvider") && !provider.authority.contains(context.getPackageName())) {
//                Log.d(TAG, "Provider: " + provider.toString());

                appIDs.add(provider.authority);
            }
        }

        Log.d(TAG, "AppIDs: " + appIDs.toString());

        return appIDs;
    }

    public SharedPreferences prefs() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs;
    }
}
