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
    private static final String PATH_CLEAR_TOKEN = "clear_token";

    private static final int CODE_TOKEN = 1;
    private static final int CODE_CLEAR_TOKEN = 2;

    public static Uri tokenURIForAuthority(String authority) {
        return Uri.parse("content://" +  authority + "/" + PATH_TOKEN);
    }

    public static Uri clearTokenURIForAuthority(String authority) {
        return Uri.parse("content://" +  authority + "/" + PATH_TOKEN);
    }

    @Override
    public boolean onCreate() {
        return false;
    }

    void setupUserStorage() {
        // By default we use content provider storage, but this can be overridden in a subclass if needed.
        ContentProviderPerAppStorage storage = new ContentProviderPerAppStorage(getContext());
        User.getInstance().setStorage(storage);
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

                MatrixCursor cursor = new MatrixCursor(new String[]{"package", TOKEN, EMAIL, PASSWORD}); // Is package needed?

                // Get details from prefs
                User.Storage storage = User.getInstance().getStorage();
                cursor.addRow(new String[]{getContext().getPackageName(), storage.getToken(), storage.getEmail(), storage.getPassword()});
                cursor.close();

                return cursor;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        if (uri.equals(clearTokenURIForAuthority(getContext().getPackageName() + AUTHORITY))) {
            if (User.getInstance().getStorage() == null) {
                setupUserStorage(); // We need to do this here, as the Application doesn't get setup before this.
            }

            if (User.getInstance().oAuthToken() != null) {
                Log.d(TAG, "Logging out through ContentProvider: " + getContext().getPackageName());
                User.getInstance().logout();
                // TODO: How do we do all the other bits? Like clearing products etc.

                return 1;
            }
        }

        return 0;
    }


    // These methods are unused as we don't support CUD (of CRUD)
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
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
