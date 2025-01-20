package com.websarva.wings.android.reminder;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView lvTask = findViewById(R.id.lvTask);
        List<Map<String,String>> taskList = new ArrayList<>();

        Map<String, String> task = new HashMap<>();
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

        String[] from = {"name", "time"};
        int[] to = {android.R.id.text1, android.R.id.text2};
        SimpleAdapter adapter = new SimpleAdapter(MainActivity.this, taskList, android.R.layout.simple_list_item_2, from, to);
        lvTask.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new FABListener());
    }

    private class FABListener implements View.OnClickListener{
        @Override
        public void onClick(View view){
            TaskNameDialogFragment dialogFragment = new TaskNameDialogFragment();
            dialogFragment.show(getSupportFragmentManager(), "TaskNameDialogFragment");
        }
    }
}