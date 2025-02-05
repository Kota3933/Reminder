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
    private static MainActivity instance;
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

        //FAB設定
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new FABListener());

        //コンテキストメニュー設定
        registerForContextMenu(lvTask);

        //データベース初期設定を実行
        _helper = new DatabaseHelper(MainActivity.this);
        DBtoALSync(true);

        //アクティビティのインスタンス送信
        DataProcess.SetMainActivity(this);
        instance = this;

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
                TaskInsert(taskName, taskTime_hour, taskTime_min);
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
            //タスク編集ダイアログの表示
        }else if(itemId == R.id.menuListContextDelete){
            //タスク削除ダイアログの表示
            TaskDeleteDialogFragment fragment = new TaskDeleteDialogFragment();
            Bundle extras = new Bundle();
            extras.putString("name", taskName);
            fragment.setArguments(extras);
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

    public List<Map<String,Object>> getTaskList(){
        return taskList;
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
        int i;
        int size = taskList.size();
        Log.i("Sync", "DBtoALSyncのAL全削除実行前の要素数：" + taskList.size());
        /*
        for(i=0 ; i<size ; i++){
            Map<String, Object> map = taskList.get(i);
            Log.i("Sync", map.get("name") + "を削除");
            map.clear();
            Log.i("Sync", "現在の要素数：" + taskList.size());
        }
         */
        taskList.clear();
        Log.i("Sync", "DBtoALSyncのAL全削除実行後の要素数：" + taskList.size());

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

    public static MainActivity getInstance(){
        if(instance == null){
            Log.e("Sync", "インスタンス送信できず");
        }
        return instance;
    }


}