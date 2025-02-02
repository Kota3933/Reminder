package com.websarva.wings.android.reminder;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataProcess {

    public static int bs_count = 1;
    private static DatabaseHelper helper;
    private static MainActivity _mainActivity;

    //TaskData関連：MainActivityのインスタンスを取得、ヘルパーオブジェクトを生成
    public static void SetmainActivity(MainActivity activity){
        _mainActivity = activity;
        helper = new DatabaseHelper(_mainActivity);
    }

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
            bs_ListPrint(taskList, i, bs_count);
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

    //TaskData関連:アプリ立ち上げ時に起動し、DBのタスク内容を全てALに反映する
    public static void SQLInitial(List<Map<String, Object>> list, Context context){
        String sql = "SELECT * FROM taskdata";
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.rawQuery(sql, null);

        int idx;
        String taskName;
        int hour, min;
        while(cursor.moveToNext()){
            idx = cursor.getColumnIndex("name");
            taskName = cursor.getString(idx);
            idx = cursor.getColumnIndex("hour");
            hour = cursor.getInt(idx);
            idx = cursor.getColumnIndex("min");
            min = cursor.getInt(idx);
            list = InsertToAL(list, taskName, hour, min);
        }
        cursor.close();
    }

    //TaskData関連：新規のタスクをDBとALに追加する
    public static void TaskInsert(List<Map<String,Object>> list, String taskName, int hour, int min){
        //DBに追加
        InsertToDB(taskName, hour, min);
        //ALに追加
        _mainActivity.taskList = InsertToAL(list, taskName, hour, min);
    }

    //TaskData関連：DBに新規のタスクを追加する
    public static void InsertToDB(String taskName, int hour, int min){
        SQLiteDatabase db = helper.getReadableDatabase();

        //まずDBにいくつのデータが入っているか取得し、新規の_idを決める
        int elementNum=0;
        String sqlCount = "SELECT * FROM taskdata";
        Cursor cursor = db.rawQuery(sqlCount, null);
        while(cursor.moveToNext()){
            elementNum++;
        }

        //DBに追加
        String sqlInsert = "INSERT INTO taskdata (_id, name, hour, min) VALUES (?, ?, ?, ?)";
        SQLiteStatement stmt = db.compileStatement(sqlInsert);
        stmt.bindLong(1,elementNum+1);
        stmt.bindString(2,taskName);
        stmt.bindLong(3,hour);
        stmt.bindLong(4,min);
        stmt.execute();

        cursor.close();
    }

    //TaskData関連：ALに新規のタスクを追加する
    public static List<Map<String,Object>> InsertToAL(List<Map<String,Object>> list, String taskName, int hour, int min){

        //時・分を時刻表示に変換
        String time;
        if(min<10){
            time = hour + ":0" + min;
        }else{
            time = hour + ":" + min;
        }

        //現在時刻の取得
        final Calendar c = Calendar.getInstance();
        int curHour = c.get(Calendar.HOUR_OF_DAY);
        int curMin = c.get(Calendar.MINUTE);

        //翌日のタスクなら、時刻表示を調整する
        if((hour*60 + min) - (curHour*60 + curMin) < 0){
            time = "明日"+time;
        }

        //リストに追加
        Map<String, Object> task = new HashMap<>();
        task.put("name", taskName);
        task.put("time", time);
        task.put("hour", hour);
        task.put("min", min);
        task.put("minFromNow", -1); //並べ替え先で設定する
        list.add(task);
        //リストを時刻順に並べ替え
        list = bs_Execute(list);

        //リストUIの更新（MainActivityで行う）
        _mainActivity.ListUIUpdate(list);

        return list;
    }

    //TaskData関連:DatabaseHelperオブジェクトの解放
    public static void HelperRelease(){
        helper.close();
    }

    public static void testSQLInsert(){
        SQLiteDatabase db = helper.getWritableDatabase();
        String sql = "INSERT INTO taskdata (_id, name, hour, min) VALUES (?,?,?,?)";
        SQLiteStatement stmt = db.compileStatement(sql);
    }
}
