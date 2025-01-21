package com.websarva.wings.android.reminder;

import android.app.Activity;
import android.os.Bundle;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    List<Map<String,Object>> taskList = new ArrayList<>();
    String taskName = "";
    int taskTime_hour;
    int taskTime_min;


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
        //makeSampleTasks();
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
        String time = hour + ":" + min;
        //リストに追加
        Map<String, Object> task = new HashMap<>();
        task.put("name", taskName);
        task.put("time", time);
        task.put("hour", hour);
        task.put("min", min);
        task.put("minConverted", (60*hour + min)); //分に換算すると何分か。並べ替えに使用
        taskList.add(task);
        //リストを時刻順に並べ替え
        //難しいので後でやる
        //リストUIを更新
        String[] from = {"name", "time"};
        int[] to = {android.R.id.text1, android.R.id.text2};
        SimpleAdapter adapter = new SimpleAdapter(MainActivity.this, taskList, android.R.layout.simple_list_item_2, from, to);
        ListView lvTask = findViewById(R.id.lvTask);
        lvTask.setAdapter(adapter);
    }

    //「20:30」のような時刻を時間と分に変換し、Bundleに入れて返す関数。使わないかも
    public Bundle TimeToHourAndMin(String time){
        int hour, min;
        int colonAt = time.indexOf(":");
        Bundle ans = new Bundle();
        hour = Integer.parseInt(time.substring(0,colonAt-1));
        min = Integer.parseInt(time.substring(colonAt+1, time.length()-1));
        ans.putInt("hour", hour);
        ans.putInt("min", min);
        return ans;
    }

}