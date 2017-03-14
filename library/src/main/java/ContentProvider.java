package com.tacchistudios.androiduserstorage;

import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.tacchistudios.androiduserstorage.User;

import static com.tacchistudios.androiduserstorage.User.Storage.EMAIL;
import static com.tacchistudios.androiduserstorage.User.Storage.PASSWORD;
import static com.tacchistudios.androiduserstorage.User.Storage.TOKEN;

public class ContentProvider extends android.content.ContentProvider {
    private static final String TAG = ContentProvider.class.getSimpleName();

    private static final String AUTHORITY = ".androiduserstorage";
    private static final String PATH_TOKEN = "token";
    private static final int CODE_TOKEN = 1;

    public static Uri tokenURIForAuthority(String authority) {
        return Uri.parse("content://" +  authority + "/" + PATH_TOKEN);
    }

    @Override
    public boolean onCreate() {
        return false;
    }

    void setupUserStorage() {
        Log.e(TAG, "This must be overridden in a subclass that you define.");

    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(getContext().getPackageName() + AUTHORITY, PATH_TOKEN, CODE_TOKEN);

        Log.d(TAG, "Got query for: " + uri.toString());

        switch (uriMatcher.match(uri)) {
            case CODE_TOKEN:
                Log.d(TAG, "Handling query for: " + uri.toString());

                if (User.getInstance().getStorage() == null) {
                    setupUserStorage(); // We need to do this here, as the Application doesn't get setup before this.
                }

                if (!User.getInstance().isLoggedIn()) {
                    Log.d(TAG, "Not logged in. Returning null.");
                    return null;
                }

                Log.d(TAG, "User logged in, so sharing with other app: " + User.getInstance().oAuthToken());

                MatrixCursor cursor = new MatrixCursor(new String[]{"package", TOKEN, EMAIL, PASSWORD});

                // Get details from prefs
                User.Storage storage = User.getInstance().getStorage();
                cursor.addRow(new String[]{getContext().getPackageName(), storage.getToken(), storage.getEmail(), storage.getPassword()});

                return cursor;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}