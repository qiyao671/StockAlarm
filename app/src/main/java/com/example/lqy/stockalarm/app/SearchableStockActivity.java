package com.example.lqy.stockalarm.app;

import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.example.lqy.stockalarm.R;
import com.example.lqy.stockalarm.contentProvider.StockProvider;
import com.example.lqy.stockalarm.contentProvider.UserShareProvider;

public class SearchableStockActivity extends AppCompatActivity {
    private ListView listView;
    private SimpleCursorAdapter cursorAdapter;
    private static final String KEY_NAME = "name", KEY_CODE = "code", KEY_GID = "gid";
    private static final int TAG_LISTVIEW = 0;
    private Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchable_stock);

        listView = (ListView) findViewById(R.id.searchableStockListView);

        handleIntent(getIntent());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(cursor != null)
            cursor.close();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(cursor != null)
            cursor.close();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    public void handleIntent(Intent intent) {
        if(intent.getAction().equals(Intent.ACTION_VIEW)) {
            Intent stockIntent = new Intent(this, StockActivity.class);
            stockIntent.setData(intent.getData());
            startActivity(stockIntent);
            this.finish();
        } else if (intent.getAction().equals(Intent.ACTION_SEARCH)) {
            ContentResolver contentResolver = getContentResolver();
            Uri uri = StockProvider.CONTENT_URI;
            cursor = contentResolver.query(uri, null, null, new String[]{intent.getStringExtra(SearchManager.QUERY)}, null);
            if(cursor.getCount() > 0) {
                cursorAdapter = new searchResultAdapter(SearchableStockActivity.this, R.layout.list_item_searchresult, cursor, new String[]{KEY_CODE, KEY_NAME}, new int[]{R.id.tv_search_code, R.id.tv_search_name}, 0);
                listView.setAdapter(cursorAdapter);
//            cursor.close();
            }
            else {
                FrameLayout frameLayout = (FrameLayout) findViewById(R.id.not_searched);
                frameLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    class searchResultAdapter extends SimpleCursorAdapter {
        Context context;
        String name, code, gid;
        public searchResultAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
            this.context = context;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if(convertView==null){
                LayoutInflater layoutInflater=LayoutInflater.from(context);
                convertView=layoutInflater.inflate(R.layout.list_item_searchresult, null, false);
            }

            ImageButton btn = (ImageButton) convertView.findViewById(R.id.imageButton);
            btn.setTag(position);
            convertView.setTag(position);

            cursor = cursorAdapter.getCursor();
            cursor.moveToPosition(position);
            name = cursor.getString(cursor.getColumnIndex(KEY_NAME));
            code = cursor.getString(cursor.getColumnIndex(KEY_CODE));
            TextView tvName = (TextView) convertView.findViewById(R.id.tv_search_name);
            TextView tvCode = (TextView) convertView.findViewById(R.id.tv_search_code);
            tvName.setText(name);
            tvCode.setText(code);

            ContentResolver contentResolver = getContentResolver();
            Uri uri = Uri.parse("content://" + UserShareProvider.AUTHORITY + "/userShare/" + gid);
            cursor = contentResolver.query(uri, null, "gid = ?", new String[] {cursor.getString(cursor.getColumnIndex(KEY_GID))}, null);

            if (cursor.getCount() > 0) {
                btn.setImageResource(R.mipmap.icon_added);
                btn.setClickable(false);
                btn.setEnabled(false);
            }
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cursor = cursorAdapter.getCursor();
                    cursor.moveToPosition((Integer) v.getTag());

                    ContentValues contentValues = new ContentValues();
                    contentValues.put(KEY_NAME, cursor.getString(cursor.getColumnIndex(KEY_NAME)));
                    contentValues.put(KEY_CODE, cursor.getString(cursor.getColumnIndex(KEY_CODE)));
                    contentValues.put(KEY_GID, cursor.getString(cursor.getColumnIndex(KEY_GID)));
                    insertStock(contentValues);
                    ((ImageButton)v).setImageResource(R.mipmap.icon_added);
                    v.setClickable(false);
                    v.setEnabled(false);
                    Toast.makeText(SearchableStockActivity.this, "添加成功", Toast.LENGTH_LONG).show();
                    Log.i("oo", "onClick: ");
//
//                    cursor.close();
                }
            });

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cursor = cursorAdapter.getCursor();
                    cursor.moveToPosition(position);
                    int _id = cursor.getInt(cursor.getColumnIndex("_id"));
                    Uri uri = Uri.parse("content://" + StockProvider.AUTHORITY + "/stock/" + _id);
                    Intent stockIntent = new Intent(SearchableStockActivity.this, StockActivity.class);
                    stockIntent.setData(uri);
                    startActivity(stockIntent);
                }
            });
//
//            cursor.close();

            return convertView;
        }

        public void insertStock(ContentValues values) {
            ContentResolver contentResolver = getContentResolver();
            Uri uri = UserShareProvider.CONTENT_URI;

            contentResolver.insert(uri, values);
        }
    }
}
