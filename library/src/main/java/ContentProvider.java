package com.tacchistudios.androiduserstorage;

import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.tacchistudios.androiduserstorage.User;

import static com.tacchistudios.androiduserstorage.User.Storage.EMAIL;
import static com.tacchistudios.androiduserstorage.User.Storage.PASSWORD;
import static com.tacchistudios.androiduserstorage.User.Storage.TOKEN;

public class ContentProvider extends android.content.ContentProvider {
    private static final String AUTHORITY = ".androiduserstorage";
    private static final String PATH_TOKEN = "token";
    private static final int CODE_TOKEN = 1;

//    public static final Uri CONTENT_URI_TOKEN = Uri.parse("content://" + AUTHORITY + "/" + PATH_TOKEN);

//    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
//    static {
//        uriMatcher.addURI(AUTHORITY, PATH_TOKEN, CODE_TOKEN);
//    }

    public static Uri tokenURIForPackageName(String packageName) {
        return Uri.parse("content://" + packageName + AUTHORITY + "/" + PATH_TOKEN);
    }

    @Override
    public boolean onCreate() {
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(getContext().getPackageName() + AUTHORITY, PATH_TOKEN, CODE_TOKEN);

        switch (uriMatcher.match(uri)) {
            case CODE_TOKEN:
                MatrixCursor cursor = new MatrixCursor(new String[]{
                        "package", TOKEN, EMAIL, PASSWORD
                });

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
