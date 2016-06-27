package com.example.lqy.stockalarm.app;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.lqy.stockalarm.R;
import com.example.lqy.stockalarm.contentProvider.UserShareProvider;
import com.example.lqy.stockalarm.data.Stock;
import com.example.lqy.stockalarm.data.StockAPI;
import com.github.mikephil.charting.charts.CandleStickChart;

import java.util.Timer;
import java.util.TimerTask;

public class StockActivity extends AppCompatActivity implements View.OnTouchListener{
    private TextView tvNowPri;
    private TextView tvIncrease;
    private TextView tvIncrePer;
    private TextView tvHigh;
    private TextView tvLow;
    private TextView tvOpen;
    private TextView tvYes;
    private TextView tvNumber;
    private TextView tvAmount;
    private String gid;
    private boolean isUserShare;
    private static final String KEY_NOWPRI = "nowPri", KEY_INCREASE = "increase", KEY_INCREPER = "increPer"
            , KEY_HIGH = "todayMax", KEY_LOW = "todayMin", KEY_OPEN = "todayStartPri", KEY_YES = "yestodEndPri"
            , KEY_NUMBER = "traNumber", KEY_AMOUNT = "traAmount", KEY_NAME = "name", KEY_CODE = "code"
            , KEY_GID = "gid";
    private static final int MESSAGE_UPDATE_TV = 1;
    private Handler handler;
    private Thread thread;
    private LinearLayout linearLayoutWarn, linearLayoutUserShare, linearLayoutNews;
    private TextView tv_UserShare;
    private int TAG_ADD = 1;
    private int TAG_DELETE = 0;
    private Cursor cursor;
    private CandleStickChart chart;
    public String strData = "Date,Open,High,Low,Close,Volume,Adj Close\n" +
            "2016-06-24,15.70,15.79,15.08,15.28,30575500,15.28\n" +
            "2016-06-23,15.90,15.90,15.71,15.72,12882200,15.72\n" +
            "2016-06-22,16.1818,16.2727,16.1636,16.2636,15650200,14.31691\n" +
            "2016-06-21,17.88,17.95,17.75,17.78,15852800,15.6518\n" +
            "2016-06-20,17.93,17.93,17.75,17.85,9681400,15.71342\n" +
            "2016-06-17,17.75,17.99,17.74,17.77,17412200,15.643\n" +
            "2016-06-16,19.437,19.723,19.36,19.58,30871100,17.23635";
//    private WebView webView;
    Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock);

        this.init();
    }

    @Override
    protected void onStop() {
        super.onStop();
        timer.cancel();
        if (cursor != null) {
            cursor.close();
        }

    }

    public void init() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.stockToolbar);
        setSupportActionBar(toolbar);

        ContentResolver contentResolver = this.getContentResolver();
        Uri uri = getIntent().getData();
        cursor = contentResolver.query(uri, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle(cursor.getString(cursor.getColumnIndex(KEY_NAME)));
            toolbar.setSubtitle(cursor.getString(cursor.getColumnIndex(KEY_CODE)));
            gid = cursor.getString(cursor.getColumnIndex(KEY_GID));
            cursor.close();
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //提醒
        linearLayoutWarn = (LinearLayout) findViewById(R.id.ll_bottom_warn);
        linearLayoutWarn.setOnTouchListener(this);

        //自选股
        linearLayoutUserShare = (LinearLayout) findViewById(R.id.ll_bottom_userShare);
        linearLayoutUserShare.setOnTouchListener(this);

        //资讯
        linearLayoutNews = (LinearLayout) findViewById(R.id.ll_bottom_news);
        linearLayoutNews.setOnTouchListener(this);

//        webView = (WebView) findViewById(R.id.webView);
//        webView.getSettings().setJavaScriptEnabled(true);
//        webView.loadUrl("http://stockpage.10jqka.com.cn/HQ_v3.html#hs_601766");
//        webView.getSettings().setUseWideViewPort(true);
//        webView.getSettings().setLoadWithOverviewMode(true);

        tv_UserShare = (TextView) findViewById(R.id.tv_bottom_userShare);

        if(uri.getAuthority().equals(UserShareProvider.AUTHORITY)) {
            tv_UserShare.setText("从自选股删除");
            linearLayoutWarn.setVisibility(View.VISIBLE);

            linearLayoutUserShare.setTag(TAG_DELETE);
        }
        else {
            uri = Uri.parse("content://" + UserShareProvider.AUTHORITY + "/userShare/" + gid);
            cursor = contentResolver.query(uri, null, "gid = ?", new String[] {gid}, null);
            if (cursor.getCount() == 0)
                linearLayoutUserShare.setTag(TAG_ADD);
            else{
                linearLayoutUserShare.setTag((TAG_DELETE));
                tv_UserShare.setText("从自选股删除");
                linearLayoutWarn.setVisibility(View.VISIBLE);
            }
            cursor.close();
        }

        tvNowPri = (TextView) findViewById(R.id.tv_content_nowPri);
        tvIncrease = (TextView) findViewById(R.id.tv_content_increase);
        tvIncrePer = (TextView) findViewById(R.id.tv_content_increPer);
        tvHigh = (TextView) findViewById(R.id.tv_content_high);
        tvLow = (TextView) findViewById(R.id.tv_content_low);
        tvOpen = (TextView) findViewById(R.id.tv_content_open);
        tvYes = (TextView) findViewById(R.id.tv_content_yes);
        tvNumber = (TextView) findViewById(R.id.tv_content_number);
        tvAmount = (TextView) findViewById(R.id.tv_content_amount);

        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                {
                    if (msg.what == MESSAGE_UPDATE_TV){
                        Bundle data = msg.getData();
                        tvNowPri.setText(data.getString(KEY_NOWPRI));
                        tvIncrease.setText(data.getString(KEY_INCREASE));
                        tvIncrePer.setText(data.getString(KEY_INCREPER) + "%");
                        tvHigh.setText(data.getString(KEY_HIGH));
                        tvLow.setText(data.getString(KEY_LOW));
                        tvOpen.setText(data.getString(KEY_OPEN));
                        tvYes.setText(data.getString(KEY_YES));
                        tvNumber.setText(data.getString(KEY_NUMBER));
                        tvAmount.setText(data.getString(KEY_AMOUNT));

                        Log.i("handle", data.getString(KEY_NOWPRI));
                    }
                }

                return  true;
            }
        });

        startTimer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cursor != null) {
            cursor.close();
        }
        if (timer != null) {
            timer.cancel();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (timer != null) {
            timer.cancel();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (timer != null) {
            timer.cancel();
        }

        startTimer();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(v == linearLayoutWarn || v == linearLayoutUserShare) {
            if(event.getAction() == MotionEvent.ACTION_DOWN) {
                v.setBackgroundResource(R.color.primary_light);
            }
            if(event.getAction() == MotionEvent.ACTION_UP) {
                v.setBackgroundResource(R.color.icons);
                //点击自选股按钮
                if(v == linearLayoutUserShare) {
                    if (linearLayoutUserShare.getTag().equals(TAG_ADD)) {
                        insertUserShare();
                        linearLayoutUserShare.setTag((TAG_DELETE));
                        tv_UserShare.setText("从自选股删除");
                        linearLayoutWarn.setVisibility(View.VISIBLE);
                        Toast.makeText(StockActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        deleteUserShare(gid);
                        tv_UserShare.setText("加入自选股");
                        linearLayoutWarn.setVisibility(View.GONE);
                        Toast.makeText(StockActivity.this, "添加成功", Toast.LENGTH_SHORT).show();
                    }
                } else if (v == linearLayoutWarn) { //提醒按钮
                    Intent intent = new Intent(StockActivity.this, WarnSettingsActivity.class);
                    intent.putExtra(KEY_GID, gid);
                    intent.putExtra(KEY_NAME, StockActivity.this.getSupportActionBar().getTitle());
                    startActivity(intent);
                }
            }

        }
        return true;

    }

    public void updateTV (){
        Stock data;

        if ((data = StockAPI.getHSStock(gid)) != null) {
            try {
                Message message = Message.obtain();
                Bundle bundle = new Bundle();
                bundle.putString(KEY_NOWPRI, data.getNowPri());
                bundle.putString(KEY_INCREASE, data.getIncrease());
                bundle.putString(KEY_INCREPER, data.getIncrePer());
                bundle.putString(KEY_HIGH, data.getHigh());
                bundle.putString(KEY_LOW, data.getLow());
                bundle.putString(KEY_OPEN, data.getOpen());
                bundle.putString(KEY_YES, data.getYesterday());
                bundle.putString(KEY_NUMBER, data.getNumber());
                bundle.putString(KEY_AMOUNT, data.getAmount());

                message.setData(bundle);
                message.what = MESSAGE_UPDATE_TV;

                handler.sendMessage(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void startTimer() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateTV();
            }
        }, 0, 5000);
    }

    public void insertUserShare() {
        ContentResolver contentResolver = this.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(KEY_GID, gid);
        values.put(KEY_CODE, getSupportActionBar().getSubtitle().toString());
        values.put(KEY_NAME, getSupportActionBar().getTitle().toString());

        Uri uri = contentResolver.insert(UserShareProvider.CONTENT_URI, values);
        Log.i("insert", uri.toString());
    }

    public int deleteUserShare(String gid) {
        ContentResolver contentResolver = this.getContentResolver();
        Uri uri = Uri.parse("content://" + UserShareProvider.AUTHORITY + "/userShare/" + gid);
        return contentResolver.delete(uri, null, null);
    }

    public void chartInit() {
        chart = (CandleStickChart) findViewById(R.id.candler_chart);

//        chart.setStartAtZero(true);
//        chart.setDrawYValues(false); // disable the drawing of values into the chart
        chart.setDrawBorders(false);   //是否在折线图上添加边框
        chart.setDescription("");// 数据描述  
        chart.setNoDataTextDescription("You need to provide data for the chart.");  //如果没有数据的时候，会显示这个，类似listview的emtpyview  
//        chart.setDrawVerticalGrid(false); // enable / disable grid lines
//        chart.setDrawHorizontalGrid(false);
        chart.setDrawGridBackground(false); // 是否显示表格颜色  
        chart.setBackgroundColor(Color.DKGRAY);// 设置背景
        chart.setGridBackgroundColor(Color.DKGRAY);//设置表格背景色
//        chart.setGridColor(Color.WHITE & 0x70FFFFFF); // 表格线的颜色，在这里是是给颜色设置一个透明度
//        chart.setGridWidth(1.25f);// 表格线的线宽
        chart.setTouchEnabled(true); // enable touch gestures 
        chart.setDragEnabled(true);// 是否可以拖拽  
        chart.setScaleEnabled(true);// 是否可以缩放  
        chart.setPinchZoom(false);// if disabled, scaling can be done on x- and y-axis separately  
        chart.setScaleYEnabled(false);// if disabled, scaling can be done on x-axis
        chart.setScaleXEnabled(false);// if disabled, scaling can be done on  y-axis 
//        chart.setValueTypeface(mTf);// 设置字体

    }

//    public void getData() {
//        String[] dataArray = strData.split("\n");
//
//        ArrayList<String[]> dataList = new ArrayList<>();
//        for (int i = 1; i < dataArray.length; i++) {
//            String data = dataArray[i];
//            dataList.add(data.split(","));
//        }
//
//        for (int i = 0; i < dataList.size(); i++) {
//            String[] values = dataList.get(i);
//            CandleEntry entry = new CandleEntry(dataList.size() - 0, Float.parseFloat(values[2]), Float.parseFloat(values[3]), Float.parseFloat(values[1]), Float.parseFloat(values[4]));
//
//        }
//    }
}