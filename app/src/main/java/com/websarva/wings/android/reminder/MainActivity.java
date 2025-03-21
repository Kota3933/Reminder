package com.websarva.wings.android.reminder;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

    private List<Map<String,Object>> taskList = new ArrayList<>();
    String taskName = "";
    int taskTime_hour;
    int taskTime_min;
    private static DatabaseHelper _helper;
    private final String CHANNEL_ID = "notificationservice_notification_channel";
    private static int count = 0;

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

        //コンテキストメニュー設定
        registerForContextMenu(lvTask);

        //データベース初期設定を実行
        _helper = new DatabaseHelper(MainActivity.this);
        DBtoALSync(true);

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
                String preTaskName;
                taskTime_hour = result.getInt("taskTime_hour");
                taskTime_min = result.getInt("taskTime_minute");
                //タスク編集モードかどうか確認
                if(result.getString("preTaskName") == null){
                    //タスク新規追加モード
                    TaskInsert(taskName, taskTime_hour, taskTime_min);
                }else{
                    //タスク編集モード
                    preTaskName = result.getString("preTaskName");
                    TaskEdit(preTaskName, taskName, taskTime_hour, taskTime_min);
                }
                //トーストメッセージ作成
                final Calendar c = Calendar.getInstance();
                int curHour = c.get(Calendar.HOUR_OF_DAY);
                int curMin = c.get(Calendar.MINUTE);
                int minConverted = taskTime_hour*60 + taskTime_min - (curHour*60 + curMin);
                if(minConverted <= 0){
                    minConverted += 24*60; //負の時は明日のタスクなので、時間を調整する
                }
                int hourFromNow = minConverted/60;
                int minFromNow = minConverted%60;
                StringBuilder sb = new StringBuilder();
                sb.append("「" + taskName + "」を");
                if(hourFromNow != 0){
                    sb.append(hourFromNow + "時間");
                }
                if(minFromNow != 0){
                    sb.append(minFromNow + "分");
                }
                sb.append("後に通知します");
                Toast.makeText(MainActivity.this, sb.toString(), Toast.LENGTH_LONG).show();
            }
        });
        //フラグメントから削除するタスクの名前を受け取る
        manager.setFragmentResultListener("taskDeleteRequest", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                String deleteTaskName = result.getString("taskName");
                if(deleteTaskName == null){
                    Log.e("TaskDelete", "削除するタスクの名前を受け取れませんでした");
                }else{
                    ALTaskDelete(deleteTaskName);
                }
            }
        });
    }

    @Override
    protected void onDestroy(){
        _helper.close();
        super.onDestroy();
    }

    private class FABListener implements View.OnClickListener{
        @Override
        public void onClick(View view){
            TaskNameDialogFragment dialogFragment = new TaskNameDialogFragment();
            dialogFragment.show(getSupportFragmentManager(), "TaskNameDialogFragment");
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu, view, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_context_menu_list, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item){
        count++;
        Log.i("contextMenu", count + "回目の呼び出し");

        boolean returnVal = true;
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int listPosition = info.position;
        Map<String, Object> map = taskList.get(listPosition);
        String taskName = (String)map.get("name");
        Log.i("contextMenu", "編集・削除するタスクの名前：" + taskName);

        int itemId = item.getItemId();
        if(itemId == R.id.menuListContextEdit){
            //編集前のタスク名・通知時刻を送信
            TaskNameDialogFragment fragment = new TaskNameDialogFragment();
            Bundle extras = new Bundle();
            String preTaskName = (String) taskList.get(listPosition).get("name");
            int preHour = (int) taskList.get(listPosition).get("hour");
            int preMin = (int) taskList.get(listPosition).get("min");
            extras.putString("preTaskName", preTaskName);
            extras.putInt("preHour", preHour);
            extras.putInt("preMin", preMin);
            Log.i("TaskEdit", "編集前のタスク名：" + preTaskName);
            Log.i("TaskEdit", "編集前のタスク時間：" + preHour + ":" + preMin);
            fragment.setArguments(extras);
            //タスク名入力ダイアログを表示
            fragment.show(getSupportFragmentManager(), "TaskEditFragment");
        }else if(itemId == R.id.menuListContextDelete){
            //削除するタスク名を送信
            TaskDeleteDialogFragment fragment = new TaskDeleteDialogFragment();
            Bundle extras = new Bundle();
            extras.putString("name", taskName);
            fragment.setArguments(extras);
            //タスク削除ダイアログの表示
            fragment.show(getSupportFragmentManager(), "TaskDeleteFragment");
        }
        return returnVal;
    }

    public void ListUIUpdate(List<Map<String,Object>> list){
        //AdapterでリストUIを更新する
        String[] from = {"name", "time"};
        int[] to = {android.R.id.text1, android.R.id.text2};
        SimpleAdapter adapter = new SimpleAdapter(MainActivity.this, list, android.R.layout.simple_list_item_2, from, to);
        ListView lvTask = findViewById(R.id.lvTask);
        lvTask.setAdapter(adapter);
    }

    //TaskData関連：ALに新規のタスクを追加する
    public void InsertToAL(String taskName, int hour, int min){

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
        //Log.i("taskDB", "(InsertToAL)タスク「" + taskName  + "」をALに追加");
        //Log.i("taskDB", "(InsertToAL)現時点の要素数：" + list.size());
        //リストを時刻順に並べ替え
        taskList = DataProcess.bs_Execute(taskList);
    }

    //TaskData関連：新規のタスクをDBとALに追加する
    public void TaskInsert(String taskName, int hour, int min){
        //DBに追加
        InsertToDB(taskName, hour, min);
        //DBの内容をALに同期
        DBtoALSync(true);
        //通知アラーム設定
        AlarmSet(taskName, hour, min);
    }

    //TaskData関連：DBに新規のタスクを追加する
    public void InsertToDB(String taskName, int hour, int min){
        SQLiteDatabase db = _helper.getReadableDatabase();

        //まずDBにいくつのデータが入っているか取得し、新規の_idを決める
        String sqlCount = "SELECT * FROM taskdata";
        Cursor cursor = db.rawQuery(sqlCount, null);
        int newId = -1;
        while(cursor.moveToNext()){
            int idx = cursor.getColumnIndex("_id");
            newId = cursor.getInt(idx) + 1;
        }

        Log.i("TaskDelete", "新idとして" + newId + "を使用");

        //DBに追加
        String sqlInsert = "INSERT INTO taskdata (_id, name, hour, min) VALUES (?, ?, ?, ?)";
        SQLiteStatement stmt = db.compileStatement(sqlInsert);
        stmt.bindLong(1,newId);
        stmt.bindString(2,taskName);
        stmt.bindLong(3,hour);
        stmt.bindLong(4,min);
        stmt.execute();

        cursor.close();
    }

    //Notification関連：Alarm設定
    public void AlarmSet(String taskName, int hour, int min){
        //通知時刻をミリ秒に変換する
        //まず現在時刻と通知時刻の差分を計算
        int curTime_inSec, notifyTime_inSec, difference;
        Calendar c = Calendar.getInstance();
        curTime_inSec = c.get(Calendar.HOUR_OF_DAY) *3600 + c.get(Calendar.MINUTE)*60 + c.get(Calendar.SECOND);
        notifyTime_inSec = hour*3600 + min*60;
        difference = notifyTime_inSec - curTime_inSec;
        if(difference < 0){
            difference+=24*3600; //マイナスの場合、翌日のタスクなので値を調整する
        }
        Log.i("Alarm", "計算した差分：" + difference + "秒");
        //現在時刻のミリ秒を取得し、それに差分を足す
        c.setTimeInMillis(System.currentTimeMillis());
        c.add(Calendar.SECOND, difference);
        //ミリ秒に変換
        long notifyTime_inMilSec = c.getTimeInMillis();

        //broadcastを設定
        Intent intent = new Intent(MainActivity.this, AlarmBroadcastReceiver.class);
        intent.putExtra("name", taskName);
        intent.putExtra("CHANNEL_ID", CHANNEL_ID);
        PendingIntent pending = PendingIntent.getBroadcast(MainActivity.this, 0, intent,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE );

        //アラームをセットする
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if(manager != null){
            manager.setExact(AlarmManager.RTC_WAKEUP, notifyTime_inMilSec, pending);
        }

    }

    public void DBtoALSync(boolean UiUpdate){
        Log.i("Sync", "DBとALの同期開始");
        //ALのタスク全削除
        taskList.clear();

        //DBからタスクを読み取り全て追加
        String sql = "SELECT * FROM taskdata";
        SQLiteDatabase db = _helper.getWritableDatabase();
        Cursor cursor = db.rawQuery(sql, null);

        int idx;
        String taskName;
        int hour, min;
        int count=1;
        while(cursor.moveToNext()){
            idx = cursor.getColumnIndex("name");
            taskName = cursor.getString(idx);
            idx = cursor.getColumnIndex("hour");
            hour = cursor.getInt(idx);
            idx = cursor.getColumnIndex("min");
            min = cursor.getInt(idx);
            InsertToAL(taskName, hour, min);
            Log.i("Sync", taskName + "をALに追加。size = " + taskList.size());
            count++;
        }
        cursor.close();

        //UIの更新
        if(UiUpdate){
            ListUIUpdate(taskList);
        }
    }

    public void ALTaskDelete(String taskName){
        int i, size;
        int idx = -1;
        Map<String, Object> map = new HashMap<>();
        size = taskList.size();
        for(i=0 ; i<size ; i++){
            map = taskList.get(i);
            if(map.get("name") == taskName){
                idx = i;
            }
        }
        if(idx == -1){
            Log.e("TaskDelete", "削除するタスクが見つかりませんでした");
        }
        map = taskList.get(idx);
        taskList.remove(map);
        //リストUIを更新
        ListUIUpdate(taskList);
        //完了のトースト表示
        String msg = "「" + taskName + "」を削除しました";
        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

    public void TaskEdit(String preTaskName, String newTaskName, int newHour, int newMin){
        //DBからタスクを削除
        DataProcess.DBTaskDelete(preTaskName, MainActivity.this);
        //新しくタスクを追加
        TaskInsert(newTaskName, newHour, newMin);
    }
}