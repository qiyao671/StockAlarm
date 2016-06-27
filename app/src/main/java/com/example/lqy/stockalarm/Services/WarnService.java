package com.example.lqy.stockalarm.Services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.example.lqy.stockalarm.R;
import com.example.lqy.stockalarm.data.StockAPI;
import com.example.lqy.stockalarm.tools.ConstantValues;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class WarnService extends Service {
    private NotificationManager notificationManager;
    private Timer timer;
    private ArrayList<Bundle> taskList;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle data = intent.getExtras();
        boolean[] flagArray = new boolean[]{false, false, false, false};
        data.putBooleanArray("flags", flagArray);
        taskList.add(data);
        if (timer != null) {
            timer.cancel();
        }
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
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        taskList = new ArrayList<>();
    }

    public void deleteFromQueue(String gid) {
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

                        if (flagArray[i] && dataArray[i] != 0.0) {
                            //通知
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(WarnService.this)
                                    .setSmallIcon(R.mipmap.ic_launcher)
                                    .setContentTitle("股票提醒")
                                    .setContentText("\"" + name + "\"" + notifyText + dataArray[i]);
                            notificationManager.notify(i, builder.build());
                        }

                    }
                }
            }

        }
    }
}
