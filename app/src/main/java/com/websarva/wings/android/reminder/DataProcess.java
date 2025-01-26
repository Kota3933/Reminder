package com.websarva.wings.android.reminder;

import android.util.Log;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataProcess {

    public static int bs_count = 1;
    //バブルソート関連関数：バブルソート実行
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

    //バブルソート関連関数：並べ替え経過表示
    public static void bs_ListPrint (List<Map<String, Object>> list, int time, int count){
        int i;
        int size = list.size();
        Log.i("BubbleSort", count + "度目のBS、" +  time + "回目の並べ替え↓" );
        for(i=0 ; i<size ; i++){
            Log.i("BubbleSort", i + ":" + (list.get(i).get("name") + ", minFN:" + list.get(i).get("minFromNow")));
        }
    }

    //バブルソート関連関数：Mapオブジェクト入れ替え
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
}
