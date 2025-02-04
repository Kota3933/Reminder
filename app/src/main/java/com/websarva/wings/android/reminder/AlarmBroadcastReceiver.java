package com.websarva.wings.android.reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import android.widget.Toast;

import java.util.List;
import java.util.Map;

public class AlarmBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //Toast.makeText(_mainActivity, "時間になりました！ ", Toast.LENGTH_LONG).show();
        String taskName = intent.getStringExtra("name");
        Log.i("Alarm",taskName + "の時刻になった！");
        DataProcess.TaskDelete(taskName, context);
    }

}