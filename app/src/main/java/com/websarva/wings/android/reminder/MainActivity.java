package com.websarva.wings.android.reminder;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity {

    List<Map<String,Object>> taskList = new ArrayList<>();
    String taskName = "";
    int taskTime_hour;
    int taskTime_min;
    int bsCount = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //リストビュー設定
        ListView lvTask = findViewById(R.id.lvTask);
        String[] from = {"name", "time"};
        int[] to = {android.R.id.text1, android.R.id.text2};
        SimpleAdapter adapter = new SimpleAdapter(MainActivity.this, taskList, android.R.layout.simple_list_item_2, from, to);
        lvTask.setAdapter(adapter);

        //FAB設定
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new FABListener());

        //フラグメントからタスクの名前・時刻を受け取る
        FragmentManager manager = getSupportFragmentManager();
        manager.setFragmentResultListener("taskNameRequest", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                taskName = result.getString("taskName");
            }
        });
        manager.setFragmentResultListener("taskTimeRequest", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                taskTime_hour = result.getInt("taskTime_hour");
                taskTime_min = result.getInt("taskTime_minute");
                String msg = "「" + taskName + "」を"+ taskTime_hour + "時" + taskTime_min + "分に設定しました";
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
                ListTaskAdd(taskName, taskTime_hour, taskTime_min);
            }
        });
    }

    private class FABListener implements View.OnClickListener{
        @Override
        public void onClick(View view){
            TaskNameDialogFragment dialogFragment = new TaskNameDialogFragment();
            dialogFragment.show(getSupportFragmentManager(), "TaskNameDialogFragment");
        }
    }

    //リストにサンプルタスクを追加する（UI確認）
    public void makeSampleTasks(){
        Map<String, Object> task = new HashMap<>();
        task.put("name", "夕食を買う");
        task.put("time", "19:00");
        taskList.add(task);
        task = new HashMap<>();
        task.put("name", "課題を提出する");
        task.put("time", "22:30");
        taskList.add(task);
        task = new HashMap<>();
        task.put("name", "朝ゴミを出す");
        task.put("time", "8:00");
        taskList.add(task);
    }

    //タスクをリストに追加する
    public void ListTaskAdd(String taskName, int hour, int min){
        //時・分を時刻表示に変換
        String time;
        if(min<10){
            time = hour + ":0" + min;
        }else{
            time = hour + ":" + min;
        }
        //リストに追加
        Map<String, Object> task = new HashMap<>();
        task.put("name", taskName);
        task.put("time", time);
        task.put("hour", hour);
        task.put("min", min);
        task.put("minConverted", (60*hour + min)); //分に換算すると何分か。並べ替えに使用
        taskList.add(task);
        //リストを時刻順に並べ替え
        bs_Sort();
        //リストUIを更新
        String[] from = {"name", "time"};
        int[] to = {android.R.id.text1, android.R.id.text2};
        SimpleAdapter adapter = new SimpleAdapter(MainActivity.this, taskList, android.R.layout.simple_list_item_2, from, to);
        ListView lvTask = findViewById(R.id.lvTask);
        lvTask.setAdapter(adapter);
    }

    //バブルソート関連関数：バブルソート実行
    public void bs_Sort(){
        int i,j,min1,min2;
        Map<String,Object> tmp = new HashMap<>();
        int size = taskList.size();
        for(i=0 ; i<size ; i++){
            for(j=0 ; j<size-i-1 ; j++){
                //隣り合うMapオブジェクトの時刻を分に換算したものを取得
                min1 = (int)taskList.get(j).get("minConverted");
                min2 = (int)taskList.get(j+1).get("minConverted");
                //どちらの時刻が早いか分で比較、左が大きければ並べ替え
                if(min1 > min2){
                    bs_MapSwap(j);
                }
            }
            bs_ListPrint(taskList, i, bsCount);
        }
        bsCount++;
    }
    //バブルソート関連関数：並べ替え状況表示
    public void bs_ListPrint(List<Map<String, Object>> list, int time, int count){
        int i;
        int size = list.size();
        Log.i("BubbleSort", count + "度目のBS、" +  time + "回目の並べ替え↓" );
        for(i=0 ; i<size ; i++){
            Log.i("BubbleSort", i + ":" + (list.get(i).get("name") + "," + list.get(i).get("minConverted")));
        }
    }

    //バブルソート関連関数：Mapオブジェクトの入れ替え
    public void bs_MapSwap(int n){
        Map<String, Object> tmp = new HashMap<>();

        //tmp = map1の処理
        tmp.put("name", taskList.get(n).get("name"));
        tmp.put("time", taskList.get(n).get("time"));
        tmp.put("hour", taskList.get(n).get("hour"));
        tmp.put("min", taskList.get(n).get("min"));
        tmp.put("minConverted", taskList.get(n).get("minConverted"));

        //map1 = map2の処理
        taskList.get(n).replace("name", taskList.get(n+1).get("name"));
        taskList.get(n).replace("time", taskList.get(n+1).get("time"));
        taskList.get(n).replace("hour", taskList.get(n+1).get("hour"));
        taskList.get(n).replace("min", taskList.get(n+1).get("min"));
        taskList.get(n).replace("minConverted", taskList.get(n+1).get("minConverted"));

        //map2 = tempの処理
        taskList.get(n+1).replace("name", tmp.get("name"));
        taskList.get(n+1).replace("time", tmp.get("time"));
        taskList.get(n+1).replace("hour", tmp.get("hour"));
        taskList.get(n+1).replace("min", tmp.get("min"));
        taskList.get(n+1).replace("minConverted", tmp.get("minConverted"));
    }
}