package com.websarva.wings.android.reminder;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Map;

public class NotificationService extends Service {

    private final String CHANNEL_ID = "notificationservice_notification_channel";

    private static NotificationService instance;
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate(){

        //通知チャンネルの作成
        String name = getString(R.string.notification_channel_name);
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
        Log.i("ServiceGenerated", "サービスのインスタンス作られた");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        return START_NOT_STICKY;
    }

    public static void TestNotification(String name, int hour, int min){
    }


}