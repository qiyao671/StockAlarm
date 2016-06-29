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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import com.example.lqy.stockalarm.R;
import com.example.lqy.stockalarm.Services.WarnService;
import com.example.lqy.stockalarm.contentProvider.UserShareProvider;
import com.example.lqy.stockalarm.data.StockAPI;
import com.example.lqy.stockalarm.tools.ConstantValues;

import java.util.Timer;
import java.util.TimerTask;

public class WarnSettingsActivity extends AppCompatActivity implements View.OnFocusChangeListener {
    private static final String KEY_GID = "gid", KEY_NAME = "name", KEY_PRICE = "price", KEY_INCREASE = "increase", KEY_PERCENT = "percent";
    private TextView tvName, tvPrice, tvIncrease, tvPercent;
    private String gid;
    private Handler handler;
    private Timer timer;
    private Button btnComplete;
    private Switch switchPriceRise, switchPriceFall, switchPercentRise, switchPercentFall;
    private EditText etPriceRise, etPriceFall, etPercentRise, etPercentFall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warn_settings);

        Intent intent = getIntent();
        gid = intent.getStringExtra(KEY_GID);

        //初始化控件
        tvName = (TextView) findViewById(R.id.tv_name);
        tvPercent = (TextView) findViewById(R.id.tv_percent);
        tvIncrease = (TextView) findViewById(R.id.tv_increse);
        tvPrice = (TextView) findViewById(R.id.tv_price);
        btnComplete = (Button) findViewById(R.id.btn_tb_complete);
        switchPercentFall = (Switch) findViewById(R.id.switch4);
        switchPriceFall = (Switch) findViewById(R.id.switch2);
        switchPercentRise = (Switch) findViewById(R.id.switch3);
        switchPriceRise = (Switch) findViewById(R.id.switch1);
        etPriceRise = (EditText) findViewById(R.id.et_price_riseTo);
        etPriceFall = (EditText) findViewById(R.id.et_price_fallTo);
        etPercentFall = (EditText) findViewById(R.id.et_fall_per);
        etPercentRise = (EditText) findViewById(R.id.et_rise_per);

        tvName.setText(intent.getStringExtra(KEY_NAME));

        initEditText();

        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                Bundle data = msg.getData();
                tvPrice.setText(data.getString((KEY_PRICE)));
                tvIncrease.setText(data.getString(KEY_INCREASE));
                tvPercent.setText(data.getString(KEY_PERCENT) + "%");
                if (data.getBoolean("isRise")) {
                    tvPercent.setTextColor(Color.RED);
                    tvPrice.setTextColor(Color.RED);
                    tvIncrease.setTextColor(Color.RED);
                }
                else {
                    tvPercent.setTextColor(getResources().getColor(R.color.green));
                    tvPrice.setTextColor(getResources().getColor(R.color.green));
                    tvIncrease.setTextColor(getResources().getColor(R.color.green));
                }
                return true;
            }
        });

        btnComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues values = new ContentValues();
                if(switchPriceRise.isChecked()) {
                    values.put(ConstantValues.KEY_MAX_PRICE, Double.parseDouble(etPriceRise.getText().toString()));
                }
                if(switchPriceFall.isChecked()) {
                    values.put(ConstantValues.KEY_MIN_PRICE, Double.parseDouble(etPriceFall.getText().toString()));
                }
                if(switchPercentRise.isChecked()) {
                    values.put(ConstantValues.KEY_MAX_PERCENT, Double.parseDouble(etPercentRise.getText().toString()));
                }
                if(switchPercentFall.isChecked()) {
                    values.put(ConstantValues.KEY_MIN_PERCENT, Double.parseDouble(etPercentRise.getText().toString()));
                }

                Uri uri = Uri.parse("content://" + UserShareProvider.AUTHORITY + "/userShare/" + gid);
                String selection = "gid = ?";
                String[] selectionArgs = new String[] {
                        gid
                };

                //开启服务
                Intent intent = new Intent(WarnSettingsActivity.this, WarnService.class);
                Bundle data = new Bundle();
                data.putString(KEY_GID, gid);
                data.putDouble(ConstantValues.KEY_MAX_PRICE, Double.parseDouble(etPriceRise.getText().toString()));
                data.putDouble(ConstantValues.KEY_MIN_PRICE, Double.parseDouble(etPriceFall.getText().toString()));
                data.putDouble(ConstantValues.KEY_MAX_PERCENT, Double.parseDouble(etPercentRise.getText().toString()));
                data.putDouble(ConstantValues.KEY_MIN_PERCENT, Double.parseDouble(etPercentFall.getText().toString()));
                intent.putExtras(data);
                startService(intent);

                //设置数据库内的数据
                ContentResolver contentResolver = getContentResolver();
                int rowNum = contentResolver.update(uri, values, selection, selectionArgs);

                //关闭页面
                finish();
            }

        });
    }

    public void initEditText() {
        Cursor cursor;

        ContentResolver contentResolver = getContentResolver();
        cursor = contentResolver.query(UserShareProvider.CONTENT_URI, null, "gid = ?", new String[]{gid}, null);
        cursor.moveToFirst();
        String str;
        etPriceRise.setTag(0);
        etPriceFall.setTag(1);
        etPercentRise.setTag(2);
        etPercentFall.setTag(3);
        etPriceRise.setOnFocusChangeListener(this);
        etPriceFall.setOnFocusChangeListener(this);
        etPercentRise.setOnFocusChangeListener(this);
        etPercentFall.setOnFocusChangeListener(this);
        if ((str = cursor.getString(cursor.getColumnIndex(ConstantValues.KEY_MAX_PRICE))) != null) {
            etPriceRise.setText(str);
            switchPriceRise.setChecked(true);
        }

        if ((str = cursor.getString(cursor.getColumnIndex(ConstantValues.KEY_MIN_PRICE))) != null) {
            etPriceFall.setText(str);
            switchPriceFall.setChecked(true);
        }

        if ((str = cursor.getString(cursor.getColumnIndex(ConstantValues.KEY_MAX_PERCENT))) != null) {
            etPercentRise.setText(str);
            switchPercentRise.setChecked(true);
        }

        if ((str = cursor.getString(cursor.getColumnIndex(ConstantValues.KEY_MIN_PERCENT))) != null) {
            etPercentFall.setText(str);
            switchPercentFall.setChecked(true);
        }

        cursor.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateData();
            }
        }, 0, 5000);
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (timer != null) {
            timer.cancel();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }
    public void updateData() {
        String result = StockAPI.querySinaStocks(gid);
        String stockInfo = StockAPI.sinaResultToStock(result);
        String[] values = stockInfo.split(",");
        if(values.length > 1) {
            double yesterday = Double.parseDouble(values[2]);
            double nowPrice = Double.parseDouble(values[3]);
            double increase = nowPrice - yesterday;
            double increPer = increase / yesterday * 100;
            Message msg = Message.obtain();
            Bundle data = new Bundle();
            data.putString(KEY_PRICE, values[1]);
            data.putString(KEY_PERCENT, String.format("%.2f",increPer));
            data.putString(KEY_INCREASE, String.format("%.2f",increase));
            data.putBoolean("isRise", increase >= 0);
            msg.setData(data);
            handler.sendMessage(msg);
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        switch ((int)v.getTag()) {
            case 0:
                if (hasFocus) {
                    switchPriceRise.setChecked(true);
                }
                else {
                    if (((EditText)v).getText().toString().equals(""))
                        switchPriceRise.setChecked(false);
                }
                break;
            case 1:
                if (hasFocus) {
                    switchPriceFall.setChecked(true);
                }
                else {
                    if (((EditText)v).getText().toString().equals(""))
                        switchPriceFall.setChecked(false);
                }
                break;
            case 2:
                if (hasFocus) {
                    switchPercentRise.setChecked(true);
                }
                else {
                    if (((EditText)v).getText().toString().equals(""))
                        switchPercentRise.setChecked(false);
                }
                break;
            case 3:
                if (hasFocus) {
                    switchPercentFall.setChecked(true);
                }
                else {
                    if (((EditText)v).getText().toString().equals(""))
                        switchPercentFall.setChecked(false);
                }
                break;
        }
    }
}
