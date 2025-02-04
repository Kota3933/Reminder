package com.websarva.wings.android.reminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DataProcess {

    public static int bs_count = 1;
    private static DatabaseHelper helper;
    private static MainActivity _mainActivity;
    private static Context maContext;

    //バブルソート関連：バブルソート実行
    public static List<Map<String,Object>> bs_Execute (List<Map<String,Object>> taskList){

        int i,j,min1,min2;
        int size = taskList.size();

        //現在時刻の取得
        final Calendar c = Calendar.getInstance();
        int curHour = c.get(Calendar.HOUR_OF_DAY);
        int curMin = c.get(Calendar.MINUTE);

        //minFromNowの値設定（全てのMapオブジェクトの対して更新）
        int tmpHour, tmpMin, tmpMinFromNow;
        for(i=0 ; i<size ; i++){
            Map<String, Object> map = taskList.get(i);
            tmpHour = (int)map.get("hour");
            tmpMin = (int)map.get("min");
            tmpMinFromNow = (tmpHour*60 + tmpMin) - (curHour*60 + curMin);
            if(tmpMinFromNow < 0){
                tmpMinFromNow += 24*60;
            }
            map.replace("minFromNow", tmpMinFromNow);
        }

        //minFromNowの値をもとに並べ替えを実行
        for(i=0 ; i<size ; i++){
            for(j=0 ; j<size-i-1 ; j++){
                min1 = (int)taskList.get(j).get("minFromNow");
                min2 = (int)taskList.get(j+1).get("minFromNow");
                //どちらの時刻が早いか比較、左が大きければ並べ替え
                if(min1 > min2){
                    taskList = bs_MapSwap(taskList, j);
                }
            }
            //bs_ListPrint(taskList, i, bs_count);
        }
        bs_count++;
        return taskList;
    }

    //バブルソート関連：並べ替え経過表示
    public static void bs_ListPrint (List<Map<String, Object>> list, int time, int count){
        int i;
        int size = list.size();
        Log.i("BubbleSort", count + "度目のBS、" +  time + "回目の並べ替え↓" );
        for(i=0 ; i<size ; i++){
            Log.i("BubbleSort", i + ":" + (list.get(i).get("name") + ", minFN:" + list.get(i).get("minFromNow")));
        }
    }

    //バブルソート関連：Mapオブジェクト入れ替え
    public static List<Map<String,Object>>  bs_MapSwap (List<Map<String,Object>> taskList, int i){
        Map<String, Object> tmp = new HashMap<>();

        //tmp = map1の処理
        tmp.put("name", taskList.get(i).get("name"));
        tmp.put("time", taskList.get(i).get("time"));
        tmp.put("hour", taskList.get(i).get("hour"));
        tmp.put("min", taskList.get(i).get("min"));
        tmp.put("minFromNow", taskList.get(i).get("minFromNow"));

        //map1 = map2の処理
        taskList.get(i).replace("name", taskList.get(i+1).get("name"));
        taskList.get(i).replace("time", taskList.get(i+1).get("time"));
        taskList.get(i).replace("hour", taskList.get(i+1).get("hour"));
        taskList.get(i).replace("min", taskList.get(i+1).get("min"));
        taskList.get(i).replace("minFromNow", taskList.get(i+1).get("minFromNow"));

        //map2 = tempの処理
        taskList.get(i+1).replace("name", tmp.get("name"));
        taskList.get(i+1).replace("time", tmp.get("time"));
        taskList.get(i+1).replace("hour", tmp.get("hour"));
        taskList.get(i+1).replace("min", tmp.get("min"));
        taskList.get(i+1).replace("minFromNow", tmp.get("minFromNow"));

        return taskList;
    }


    //TaskData関連：タスク削除
    public static void TaskDelete(String taskName, Context context){
        SQLiteOpenHelper _helper = new DatabaseHelper(context);

        //DBからタスクを削除
        //まず削除するタスクのIdを取得する
        int deleteId = -1;
        if(_helper == null){
            Log.e("TaskDelete", "ヘルパーが取得できませんでした");
        }
        SQLiteDatabase db = _helper.getWritableDatabase();
        String sqlSELECT = "SELECT _id FROM taskdata WHERE name = '" + taskName + "'";
        Log.i("taskDelete", "完成したSQL文：" + sqlSELECT);
        String[] bindName = {taskName};
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
        Log.i("TaskDelete", "DBからタスク「" + taskName + "」の削除完了");

        //DBの内容をALと同期
        //今はやらない

        //完了のトースト表示
        String msg = "「" + taskName + "」を削除しました";
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }


}
