package com.websarva.wings.android.reminder;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class BackGroundDBService extends Service {

    private static DatabaseHelper helper;


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Log.i("BackGroundDB", "バックグラウンドでサービスを実行");
        helper = new DatabaseHelper(BackGroundDBService.this);
        Bundle extras = intent.getExtras();
        String operation = extras.getString("operation");
        String name = extras.getString("name");

        //DBからタスクを削除
        if(operation == "delete"){
            //まず削除するタスクのIdを取得する
            int deleteId = -1;
            if(helper == null){
                Log.e("TaskDelete", "ヘルパーが設定されてない");
            }
            SQLiteDatabase db = helper.getWritableDatabase();
            String sqlSELECT = "SELECT _id FROM taskdatabase WHERE name = '" + name + "'";
            Cursor cursor = db.rawQuery(sqlSELECT, null);
            if(cursor.moveToNext()){
                int idx = cursor.getColumnIndex("_id");
                deleteId = cursor.getInt(idx);
            }else{
                Log.e("TaskDelete", "削除するタスクが見つかりません");
            }
            cursor.close();
            //タスクを削除
            String sqlDelete = "DELETE FROM taskdata WHERE _id = ?";
            SQLiteStatement stmt = db.compileStatement(sqlDelete);
            stmt.bindLong(1, deleteId);
            stmt.executeUpdateDelete();
        }
        return START_NOT_STICKY;
    }
}