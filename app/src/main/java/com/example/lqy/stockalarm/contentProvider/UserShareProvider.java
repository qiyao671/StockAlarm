package com.example.lqy.stockalarm.contentProvider;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import com.example.lqy.stockalarm.dataHelper.DatabaseHelper;

/**
 * Created by lqy on 16-6-18.
 */
public class UserShareProvider extends ContentProvider{
    public  static String AUTHORITY = "com.example.lqy.stockalarm.contentProvider.UserShareProvider";
    public static  final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/userShare");
    public static final String USERSHARES_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.example.lay.stockalarm.contentProvider";
    public static final String USERSHARE_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.example.lay.stockalarm.contentProvider";
    private DatabaseHelper databaseHelper;
    private SQLiteDatabase db;

    private static final int USERSHARES = 1;
    private static final int USERSHARE = 2;
    private static final int GET_USERSHARE = 3;

    private static final UriMatcher sURIMatcher = buildUriMatcher();

    private static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(AUTHORITY, "userShare", USERSHARES);
        matcher.addURI(AUTHORITY, "userShare/#", GET_USERSHARE);
        matcher.addURI(AUTHORITY, "userShare/*", USERSHARE);

        return matcher;
    }
    @Override
    public boolean onCreate() {
        databaseHelper = new DatabaseHelper(getContext());
        db = databaseHelper.getWritableDatabase();
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        switch (sURIMatcher.match(uri)) {
            case USERSHARES:
                return db.query("userShare", projection, selection, selectionArgs, null, null, null);
            case USERSHARE:
                return db.query("userShare", null, selection, selectionArgs, null, null, null);
            case GET_USERSHARE:
                return getUserShare(uri);
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long _id;
        switch (sURIMatcher.match(uri)) {
            case USERSHARE:
                _id = db.insert("userShare", "gid", values);
                return ContentUris.withAppendedId(uri, _id);
            case USERSHARES:
                _id = db.insert("userShare", "gid", values);
                String path = uri.toString();
                return Uri.parse(path.substring(0, path.lastIndexOf("/")) + "/" + _id);
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        return db.delete("userShare", "gid = ?", new String[]{uri.getLastPathSegment()});
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        return db.update("userShare",values, selection, selectionArgs);
    }

    public Cursor getUserShare(Uri uri) {
        String id = uri.getLastPathSegment();
        String[] columns = new String[] {
                "name",
                "code",
                "gid"
        };

        return db.query("userShare", columns, "_id = ?", new String[]{id}, null, null, null);
    }
}
