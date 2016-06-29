package com.example.lqy.stockalarm.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.example.lqy.stockalarm.R;
import com.example.lqy.stockalarm.contentProvider.UserShareProvider;
import com.example.lqy.stockalarm.data.StockAPI;
import com.example.lqy.stockalarm.tools.ConstantValues;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class WarnService extends Service {
    private NotificationManager notificationManager;
    private Timer timer;
    private static ArrayList<Bundle> taskList;
    private static boolean isRunning = false;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Bundle data = intent.getExtras();
        if (data != null) {
            deleteFromQueue(data.getString(ConstantValues.KEY_GID));
            boolean[] flagArray = new boolean[]{false, false, false, false};
            data.putBooleanArray("flags", flagArray);
            taskList.add(data);

            if (timer == null) {
                startTimer();
            }
        }

        return START_REDELIVER_INTENT;
    }

    public void startTimer() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (taskList.size() <= 0) {
                    stopSelf();
                }
                for (int i = 0; i < taskList.size(); i ++) {
                    new GetDataThread(taskList.get(i)).start();
                }
            }
        }, 0, 10000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
        isRunning = false;
    }

    public void initTastList() {
        taskList = new ArrayList<>();
        String[] projection = new String[] {
                ConstantValues.KEY_GID,
                ConstantValues.KEY_MAX_PERCENT,
                ConstantValues.KEY_MAX_PRICE,
                ConstantValues.KEY_MIN_PERCENT,
                ConstantValues.KEY_MIN_PRICE
        };

        String selection = ConstantValues.KEY_MAX_PERCENT + " is not null or " + ConstantValues.KEY_MIN_PRICE + " is not null or " + ConstantValues.KEY_MAX_PRICE + " is not null or " + ConstantValues.KEY_MIN_PRICE + " is not null";

        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(UserShareProvider.CONTENT_URI, projection, selection, null, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                Bundle data = new Bundle();
                data.putString(ConstantValues.KEY_GID, cursor.getString(cursor.getColumnIndex(ConstantValues.KEY_GID)));
                data.putString(ConstantValues.KEY_MAX_PRICE, cursor.getString(cursor.getColumnIndex(ConstantValues.KEY_MAX_PRICE)));
                data.putString(ConstantValues.KEY_MIN_PRICE, cursor.getString(cursor.getColumnIndex(ConstantValues.KEY_MIN_PRICE)));
                data.putString(ConstantValues.KEY_MAX_PERCENT, cursor.getString(cursor.getColumnIndex(ConstantValues.KEY_MAX_PERCENT)));
                data.putString(ConstantValues.KEY_MIN_PERCENT, cursor.getString(cursor.getColumnIndex(ConstantValues.KEY_MIN_PERCENT)));
                boolean[] flagArray = new boolean[]{false, false, false, false};
                data.putBooleanArray("flags", flagArray);

                taskList.add(data);
                cursor.moveToNext();
            }
        }

    }

    public static boolean isRunning() {
        return isRunning;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        isRunning = true;

        initTastList();

        if (taskList.size() > 0)
            startTimer();
    }

    public static void deleteFromQueue(String gid) {
        for (Bundle task : taskList) {
            if (task.get(ConstantValues.KEY_GID).equals(gid)) {
                taskList.remove(task);
            }
        }
    }

//    public void

    class GetDataThread extends Thread {
        Bundle data;
        public GetDataThread(Bundle data) {
            this.data = data;
        }

        @Override
        public void run() {
            String gid = data.getString(ConstantValues.KEY_GID);
            Log.i("service test", "run: " + gid);
            double maxPrice = data.getDouble(ConstantValues.KEY_MAX_PRICE);
            double minPrice = data.getDouble(ConstantValues.KEY_MIN_PRICE);
            double maxPercent = data.getDouble(ConstantValues.KEY_MAX_PERCENT);
            double minPercent = data.getDouble(ConstantValues.KEY_MIN_PERCENT);

            double[] dataArray = new double[]{maxPrice, minPrice, maxPercent, minPercent};
            boolean[] flagArray = data.getBooleanArray("flags");

            String result = StockAPI.querySinaStocks(gid);
            String stockInfo = StockAPI.sinaResultToStock(result);
            String[] values = stockInfo.split(",");
            if(values.length > 1) {
                double yesterday = Double.parseDouble(values[2]);
                double nowPrice = Double.parseDouble(values[3]);
                double increase = nowPrice - yesterday;
                double increPer = increase / yesterday * 100;
                String name = values[0];
                String[] notifyText = new String[]{"当前的股价已涨到", "当前的股价已跌至", "当前的涨幅已达到", "当前的跌幅已达到"};


                for (int i = 0; i < flagArray.length; i++) {
                    boolean flag = flagArray[i];

                    //之前未达到目标
                    if (!flag) {
                        //更改flag
                        switch (i) {
                            case 0:
                                flagArray[i] = (nowPrice >= dataArray[i]);
                                break;
                            case 1:
                                flagArray[i] = (nowPrice <= dataArray[i]);
                                break;
                            case 2:
                                flagArray[i] = (increPer >= dataArray[i]);
                                break;
                            case 3:
                                flagArray[i] = (-increPer >= dataArray[i]);
                                break;
                        }
                        Log.i("", "run: " + name);

                        if (flagArray[i] && dataArray[i] != 0.0) {
                            //通知
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(WarnService.this)
                                    .setSmallIcon(R.mipmap.ic_launcher)
                                    .setDefaults(Notification.DEFAULT_SOUND)
                                    .setDefaults(Notification.DEFAULT_VIBRATE)
                                    .setContentTitle("StockAlarm")
                                    .setContentText((name + notifyText[i] + dataArray[i]));

                            notificationManager.notify(i, builder.build());
                        }

                    }
                }
            }

        }
    }
}
