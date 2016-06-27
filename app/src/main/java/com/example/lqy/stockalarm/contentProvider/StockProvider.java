package com.example.lqy.stockalarm.contentProvider;

import android.app.SearchManager;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import com.example.lqy.stockalarm.dataHelper.DatabaseHelper;

/**
 * Created by lqy on 16-6-15.
 */
public class StockProvider extends ContentProvider {
    public  static String AUTHORITY = "com.example.lqy.stockalarm.contentProvider.StockProvider";
    public static  final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/stock");
    public static final String STOCKS_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.example.lay.stockalarm.contentProvider";
    public static final String STOCK_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.example.lay.stockalarm.contentProvider";
    private SQLiteDatabase db;
    private DatabaseHelper databaseHelper;

    private static final int SEARCH_STOCKS = 0;
    private static final int GET_STOCK = 1;
    private static final int SEARCH_SUGGEST = 2;
//    private static final int REFRESH_SHORTCUT = 3;
    private static final UriMatcher sURIMatcher = buildUriMatcher();

    private static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(AUTHORITY, "stock", SEARCH_STOCKS);
        matcher.addURI(AUTHORITY, "stock/#", GET_STOCK);
        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST);
        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH_SUGGEST);

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
            case SEARCH_SUGGEST:
                if (selectionArgs == null) {
                    throw new IllegalArgumentException("selectionArgs must be provided for the Uri: " + uri);
                }
                return getSuggestions(selectionArgs[0]);
            case GET_STOCK:
                return getStock(uri);
            case SEARCH_STOCKS:
                if (selectionArgs == null) {
                    throw new IllegalArgumentException("selectionArgs must be provided for the Uri: " + uri);
                }
                return searchStocks(selectionArgs[0]);
        }
        return null;
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
            case SEARCH_STOCKS:
                _id = db.insert("stock", "gid", values);
                return ContentUris.withAppendedId(uri, _id);
            case GET_STOCK:
                _id = db.insert("stock", "gid", values);
                String path = uri.toString();
                return Uri.parse(path.substring(0, path.lastIndexOf("/")) + "/" + _id);
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        switch (sURIMatcher.match(uri)) {
            case SEARCH_STOCKS:
                db.rawQuery("UPDATE sqlite_sequence   SET seq   = 0   WHERE name   = 'stock'", null);
                return db.delete("stock", null, null);
        }
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    private Cursor getSuggestions(String query) {
        query = query.toLowerCase();
        String[] colums = new String[] {
                "_id",
                "name as " + SearchManager.SUGGEST_COLUMN_TEXT_1,
                "code as "+ SearchManager.SUGGEST_COLUMN_TEXT_2,
                "_id as " + SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID
        };
        return getStockNameMatches(query, colums);
    }

    private Cursor searchStocks(String query) {
        query = query.toLowerCase();
        String[] colums = new String[]{
                "_id",
                "name",
                "code",
                "gid"
        };
        return getStockNameMatches(query, colums);
    }

    private Cursor getStock(Uri uri) {
        String _id = uri.getLastPathSegment();
        String[] columns = new String[]{
                "name",
                "code",
                "gid"
        };
        String selection = "_id = ?";
        String selectionArgs[] = new String[] {
                _id
        };

        return db.query("stock", columns, selection, selectionArgs, null, null, "_id");
    }

    public Cursor getStockNameMatches(String query, String[] columns) {
        if (!query.isEmpty()){
            String selection = "code" + " LIKE ?";
            String[] selectionArgs = new String[]{"%" + query + "%"};

            return db.query("stock", columns, selection, selectionArgs, null, null, "_id");
        }
        else

            return null;
    }
}
