package com.websarva.wings.android.reminder;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

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
        instance = this;

        //通知チャンネルの作成
        String name = getString(R.string.notification_channel_name);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
        Log.i("ServiceGenerated", "サービスのインスタンス作られた");

        //リストビューをリスナーに設定
        LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.activity_main, null);
        ListView lvTask = layout.findViewById(R.id.lvTask);
        //lvTask.setOnItemClickListener(new TaskListListener());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        return START_NOT_STICKY;
    }

    public static void TestNotification(String name, int hour, int min){
    }

    public static NotificationService getService(){
        return instance;
    }
    private class TaskListListener implements AdapterView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id){
            //タスク情報取得
            Map<String,Object> map = (Map<String,Object>)parent.getItemAtPosition(position);
            String taskName = (String)map.get("name");
            int taskHour = (int)map.get("hour");
            int taskMin = (int)map.get("min");


            //通知する
            NotificationCompat.Builder builder = new NotificationCompat.Builder(NotificationService.getService(), CHANNEL_ID);
            builder.setSmallIcon(android.R.drawable.ic_dialog_info);
            builder.setContentTitle(taskName + getString(R.string.notification_title));
            builder.setContentText(taskName + getString(R.string.notification_text));
            Notification notification = builder.build();
            NotificationManagerCompat manager = NotificationManagerCompat.from(NotificationService.getService());
            manager.notify(100, notification);
        }
    }

}