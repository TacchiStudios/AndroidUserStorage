package com.tacchistudios.androiduserstorage;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.tacchistudios.androiduserstorage.ContentProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class ContentProviderPerAppStorage implements User.Storage {
    private static final String TAG = ContentProviderPerAppStorage.class.getSimpleName();

    private Context context;

    public ContentProviderPerAppStorage(Context _context) {
        context = _context;
    }

    @Override
    public void setTokenDetails(String token, String email, String password) {
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

    @Override
    @Nullable
    public Set<User.TokenDetails> tokenDetailsForSeparatedAppsThatCanBeExchangedForTokenForCurrentApp() {
        Set<User.TokenDetails> tokenDetailsSet = new HashSet<>();

        Set<String> appIds = appIDsForSeparatedAppsWithContentProviders();

        for (String appId : appIds) {
            // Get the token etc for each app ID (if email exists)

            // Use the AppID in the query...
            Cursor cursor = context.getContentResolver().query(ContentProvider.tokenURIForPackageName(appId), null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                String email = cursor.getString(cursor.getColumnIndex(EMAIL));
                if (email != null) { // Need to check email so that anonymous sessions aren't included, as they can't be exchanged.
                    tokenDetailsSet.add(new User.TokenDetails(cursor.getString(cursor.getColumnIndex("package")), cursor.getString(cursor.getColumnIndex(TOKEN)), email, cursor.getString(cursor.getColumnIndex(PASSWORD))));
                }
            }
        }

        if (!tokenDetailsSet.isEmpty()) {
            return tokenDetailsSet;
        }

        return null;
    }

    private Set<String> appIDsForSeparatedAppsWithContentProviders() {
        List<ProviderInfo> providers = context.getPackageManager()
                .queryContentProviders(null, 0, 0);

        Set<String> appIDs = new HashSet<>();

        for (ProviderInfo provider : providers) {

//            if (provider.authority.contains("com.espritline.androiduserstorage")) { // This package needs to be an option for this class
            if (provider.name.contains("com.tacchistudios.androiduserstorage.ContentProvider") && !provider.authority.contains(context.getPackageName())) {
                Log.d(TAG, "Provider: " + provider.toString() + ", authority: " + provider.authority);

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
