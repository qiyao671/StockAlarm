package com.example.lqy.stockalarm.app;

import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import com.example.lqy.design.views.SyncHorizontalScrollView;
import com.example.lqy.stockalarm.R;
import com.example.lqy.stockalarm.contentProvider.StockProvider;
import com.example.lqy.stockalarm.contentProvider.UserShareProvider;
import com.example.lqy.stockalarm.data.StockAPI;
import com.example.lqy.stockalarm.tools.ConstantValues;
import com.example.lqy.stockalarm.tools.UtilTools;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;

public class UserShareListActivity extends AppCompatActivity {
    private Cursor cursor;
    private static final String KEY_NOWPRI = "nowPri", KEY_INCREASE = "increase", KEY_INCREPER = "increPer"
            , KEY_HIGH = "todayMax", KEY_LOW = "todayMin", KEY_OPEN = "todayStartPri", KEY_YES = "yestodEndPri"
            , KEY_NUMBER = "traNumber", KEY_AMOUNT = "traAmount", KEY_NAME = "name", KEY_CODE = "code"
            , KEY_GID = "gid";
    private SimpleCursorAdapter adapterStock;
    private SimpleAdapter adapterInfo;
    private ListView listViewStock;
    private ListView listViewStockInfo;
    private ArrayList<HashMap<String, Object>> stockInfoList;
    private Handler handler;
    private Timer timer;
    private static final String DATE_FILE = "date";
    private static final int MSG_WHAT_UPDATE_LV = 0, MSG_WHAT_NETERROR = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_share_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.userShareToolbar);
        setSupportActionBar(toolbar);

        //初始化listview
        listViewStock = (ListView) findViewById(R.id.usrShareListView);
        ContentResolver contentResolver = getContentResolver();
        cursor = contentResolver.query(UserShareProvider.CONTENT_URI, new String[]{"_id", KEY_NAME, KEY_CODE, KEY_GID}, null, null, null);
        adapterStock = new SimpleCursorAdapter(this, R.layout.list_item_usershare1, cursor, new String[]{KEY_NAME, KEY_CODE}, new int[]{R.id.tv_content_name, R.id.tv_content_code}, 0);
        listViewStock.setAdapter(adapterStock);

        listViewStockInfo = (ListView) findViewById(R.id.usrShareInfoListView);
        stockInfoList = new ArrayList<>();
        adapterInfo = new SimpleAdapter(this, stockInfoList, R.layout.list_item_usershare2, new String[]{KEY_NOWPRI, KEY_INCREPER, KEY_INCREASE, KEY_OPEN, KEY_YES, KEY_HIGH, KEY_LOW}, new int[]{R.id.tv_item_now, R.id.tv_item_increPer, R.id.tv_item_increase, R.id.tv_item_open, R.id.tv_item_yes, R.id.tv_item_high, R.id.tv_item_low});
        listViewStockInfo.setAdapter(adapterInfo);

        SyncHorizontalScrollView infoScrollView = (SyncHorizontalScrollView) findViewById(R.id.infoHoriScrollView);
        SyncHorizontalScrollView stockScrollView = (SyncHorizontalScrollView) findViewById(R.id.stockHoriScrollView);
        infoScrollView.setScrollView(stockScrollView);
        stockScrollView.setScrollView(infoScrollView);
        UtilTools.setListViewHeightBasedOnChildren(listViewStock);

        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_WHAT_UPDATE_LV:
                        adapterInfo.notifyDataSetChanged();
                        UtilTools.setListViewHeightBasedOnChildren(listViewStockInfo);
                        Log.i("handler test", "handleMessage:  info changed");
                        break;
                    case MSG_WHAT_NETERROR:
                        Toast.makeText(UserShareListActivity.this, "网络连接错误", Toast.LENGTH_LONG).show();
                }
                return true;
            }
        });

        listViewStockInfo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //跳转到 股票的具体信息页面
                Log.i("log", "onItemClick: " + position);
                cursor.moveToPosition(position);
                int _id = cursor.getInt(cursor.getColumnIndex("_id"));
                Uri uri = Uri.parse("content://" + UserShareProvider.AUTHORITY + "/userShare/" + _id);

                Intent intent = new Intent(UserShareListActivity.this, StockActivity.class);
                intent.setData(uri);
                startActivity(intent);
            }
        });

        updateDB();
    }

    public void startTimer() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                getStocksInfo();
            }
        }, 0, 5000);
        Log.i("resume_test", "onResume: timer is start");
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_share_list, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
        switch (id){
            case R.id.action_search:{

            }
        }

        return super.onOptionsItemSelected(item);
    }

    public void initStockTable() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                for (int page = 1; page <= StockAPI.getPageCount(StockAPI.URL_SHLIST); page ++) {
                    JSONArray ja = StockAPI.getShStockList(page);
                    for (int i = 0; i < ja.length(); i ++) {
                        JSONObject jb = null;
                        try {
                            jb = ja.getJSONObject(i);
                            ContentValues values = new ContentValues();
                            values.put(ConstantValues.KEY_GID, jb.getString("symbol"));
                            values.put(ConstantValues.KEY_NAME, jb.getString("name"));
                            values.put(ConstantValues.KEY_CODE, jb.getString("code"));

                            ContentResolver cr = getContentResolver();
                            cr.insert(StockProvider.CONTENT_URI, values);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        }).start();
    }

    public void getStocksInfo() {
        String gidList = new String();
        cursor.moveToFirst();
        for(int i = 0; i < cursor.getCount(); i++){
            gidList = gidList + cursor.getString(cursor.getColumnIndex("gid"));
            if (i != cursor.getCount() - 1) {
                gidList = gidList + ",";
                cursor.moveToNext();
            }
        }

        sinaResultToStocks(gidList);
    }

    public void sinaResultToStocks(String list) {
        String res = StockAPI.querySinaStocks(list);
        if (res != null) {
            res = res.replaceAll("\n", "");
            String[] stocks = res.split(";");

            stockInfoList.clear();
            for(String stock : stocks) {
                String right = StockAPI.sinaResultToStock(stock);
                String[] values = right.split(",");

                double yesterday = Double.parseDouble(values[2]);
                double nowPrice = Double.parseDouble(values[3]);
                double increase = nowPrice - yesterday;
                double increPer = increase / yesterday * 100;
                HashMap<String, Object> stockNow = new HashMap<>();

                stockNow.put(KEY_OPEN, values[1]);
                stockNow.put(KEY_YES, values[2]);
                stockNow.put(KEY_NOWPRI, values[3]);
                stockNow.put(KEY_HIGH, values[4]);
                stockNow.put(KEY_LOW, values[5]);
                stockNow.put(KEY_INCREASE, String.format("%.2f", increase));
                stockNow.put(KEY_INCREPER, String.format("%.2f", increPer) + "%");

                stockInfoList.add(stockNow);
            }
            Message msg = Message.obtain();
            msg.what = MSG_WHAT_UPDATE_LV;
            handler.sendMessage(msg);
        }
        else {
            Message msg = Message.obtain();
            msg.what = MSG_WHAT_NETERROR;
            handler.sendMessage(msg);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (timer != null) {
            timer.cancel();
            Log.i("pause_test", "onPause: timer is cancel");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cursor.close();
        if (timer != null) {
            timer.cancel();
            Log.i("destroy_test", "onDestroy: timer is cancel");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        ContentResolver contentResolver = getContentResolver();
        cursor = contentResolver.query(UserShareProvider.CONTENT_URI, new String[]{"_id", KEY_NAME, KEY_CODE, KEY_GID}, null, null, null);
        adapterStock.swapCursor(cursor);
        adapterStock.notifyDataSetChanged();
        UtilTools.setListViewHeightBasedOnChildren(listViewStock);
        if(timer!=null){
            timer.cancel();
        }
        if(cursor.getCount() > 0)
            startTimer();

//        getStocksInfo();
        adapterInfo.notifyDataSetChanged();
        UtilTools.setListViewHeightBasedOnChildren(listViewStockInfo);



    }
    public void writeDateToFile() {
        long time = getToday();

        Log.i("calendarTest", String.valueOf(time));
        //将时间写入文件中
        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = openFileOutput(DATE_FILE, Context.MODE_PRIVATE);
            fileOutputStream.write(UtilTools.longToBytes(time));
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public long getToday() {
        // 获取当前时间并清除时分秒毫秒数
        Calendar calendar = Calendar.getInstance();
        calendar.clear(Calendar.HOUR);
        calendar.clear(Calendar.MINUTE);
        calendar.clear(Calendar.SECOND);
        calendar.clear(Calendar.MILLISECOND);

        return calendar.getTimeInMillis();
    }

    public boolean isSameDay(){

        FileInputStream fileInputStream;
        File file = new File(this.getFilesDir(), DATE_FILE);
        if(!file.exists()) {
            writeDateToFile();
            return false;
        }

        else {
            try {
                fileInputStream = this.openFileInput(DATE_FILE);
                int length = fileInputStream.available();
                byte [] buffer = new byte[length];
                fileInputStream.read(buffer);
                long lastUpdateDay = UtilTools.bytesToLong(buffer);
                long today = getToday();
                fileInputStream.close();
                if (today > lastUpdateDay) {
                    writeDateToFile();
                    return false;
                }
                else {
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    public void updateDB() {
        if (!isSameDay()) {
            //清除表的数据
            ContentResolver contentResolver = getContentResolver();
            contentResolver.delete(StockProvider.CONTENT_URI, null, null);
            initStockTable();
        }
    }
}
