package com.websarva.wings.android.reminder;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.List;
import java.util.Map;

public class AlarmBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent == null){
            Log.e("notification", "引継ぎデータを取得できませんでした");
        }

        String taskName = intent.getStringExtra("name");
        String channelID = intent.getStringExtra("CHANNEL_ID");
        Log.i("Alarm",taskName + "の時刻になった！");
        DataProcess.TaskDelete(taskName, context);

        //NotificationServiceの開始
        Intent serviceIntent = new Intent(context, NotificationService.class);
        context.startService(serviceIntent);

        //通知する
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelID);
        if(builder.equals(null)){
            Log.e("notification", "builderを取得できませんでした");
        }
        builder.setSmallIcon(android.R.drawable.ic_dialog_info);
        builder.setContentTitle(taskName + "の時間です！");
        builder.setContentText(taskName + "の時間になりました");
        Notification notification = builder.build();
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        manager.notify(100, notification);

        context.stopService(serviceIntent);
    }

}