package com.websarva.wings.android.reminder;

import android.app.Activity;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
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
    private final String CHANNEL_ID = "notificationservice_notification_channel";
    private MainActivity mainActivity;
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
        lvTask.setOnItemClickListener(new TaskListListener());

        //FAB設定
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new FABListener());

        //自身のアクティビティのインスタンスを保存、送信
        mainActivity = this;
        DataProcess.SetmainActivity(mainActivity);

        //データベース初期設定を実行
        DataProcess.SQLInitial(taskList, MainActivity.this);

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
                //タスクをDB・ALへ追加
                DataProcess.TaskInsert(taskList, taskName, taskTime_hour, taskTime_min);
            }
        });
    }

    @Override
    protected void onDestroy(){
        DataProcess.HelperRelease();
        super.onDestroy();
    }

    private class FABListener implements View.OnClickListener{
        @Override
        public void onClick(View view){
            TaskNameDialogFragment dialogFragment = new TaskNameDialogFragment();
            dialogFragment.show(getSupportFragmentManager(), "TaskNameDialogFragment");
        }
    }

    private class TaskListListener implements AdapterView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id){
            //タスク情報取得
            Map<String,Object> map = (Map<String,Object>)parent.getItemAtPosition(position);
            String taskName = (String)map.get("name");
            int taskHour = (int)map.get("hour");
            int taskMin = (int)map.get("min");

            //テスト通知送信
            /*
            Intent intent = new Intent(MainActivity.this, NotificationService.class);
            startService(intent);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(NotificationService.getService(), CHANNEL_ID);
            builder.setSmallIcon(android.R.drawable.ic_dialog_info);
            builder.setContentTitle(taskName + getString(R.string.notification_title));
            builder.setContentText(taskName + getString(R.string.notification_text));
            Notification notification = builder.build();
            NotificationManagerCompat manager = NotificationManagerCompat.from(NotificationService.getService());
            manager.notify(100, notification);
             */
        }
    }

    //（使わない）タスクをリストに追加する
    public void ListTaskAdd(String taskName, int hour, int min){
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
        taskList.add(task);
        //リストを時刻順に並べ替え
        taskList = DataProcess.bs_Execute(taskList);
        //リストUIを更新
        String[] from = {"name", "time"};
        int[] to = {android.R.id.text1, android.R.id.text2};
        SimpleAdapter adapter = new SimpleAdapter(MainActivity.this, taskList, android.R.layout.simple_list_item_2, from, to);
        ListView lvTask = findViewById(R.id.lvTask);
        lvTask.setAdapter(adapter);
    }

    public void ListUIUpdate(List<Map<String,Object>> list){
        //AdapterでリストUIを更新する
        String[] from = {"name", "time"};
        int[] to = {android.R.id.text1, android.R.id.text2};
        SimpleAdapter adapter = new SimpleAdapter(MainActivity.this, list, android.R.layout.simple_list_item_2, from, to);
        ListView lvTask = findViewById(R.id.lvTask);
        lvTask.setAdapter(adapter);
    }

}