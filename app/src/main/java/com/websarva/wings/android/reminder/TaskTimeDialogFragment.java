package com.websarva.wings.android.reminder;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;

import java.util.Calendar;

public class TaskTimeDialogFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener, TimePickerDialog.OnCancelListener{

    Bundle extras = new Bundle();
    String preTaskName;
    int preHour=0, preMin=0;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        TimePickerDialog dialog = new TimePickerDialog(getActivity(), this, hour, minute+1, DateFormat.is24HourFormat(getActivity()));
        //引継ぎデータの取得
        extras = getArguments();
        if(extras != null){
            preTaskName = extras.getString("preTaskName");
            preHour = extras.getInt("preHour");
            preMin = extras.getInt("preMin");
            //時計を編集前の時刻に設定
            dialog = new TimePickerDialog(getActivity(), this, preHour, preMin, DateFormat.is24HourFormat(getActivity()));
            Log.i("TaskEdit", "@TaskTimeDF 引継ぎデータあり。タスク名：" + preTaskName);
            Log.i("TaskEdit", "@TaskTimeDF 引継ぎデータあり。変更前時間は" + preHour + ":" + preMin);
        }else{
            Log.i("TaskEdit", "@TaskTimeDF 引継ぎデータなし");
        }
        return dialog;
    }



    public void onTimeSet(TimePicker view, int hourOfDay, int minute){
        //String msg = "「" + taskName + "」を"+ hourOfDay + "時" + minute + "分に設定しました";
        //Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();

        //設定時刻をBundleに格納し送信
        Bundle result = new Bundle();
        result.putInt("taskTime_hour", hourOfDay);
        result.putInt("taskTime_minute", minute);
        //引継ぎデータがあれば、前のタスク名を送信
        if(extras != null){
            result.putString("preTaskName", preTaskName);
        }
        FragmentManager manager = getParentFragmentManager();
        manager.setFragmentResult("taskTimeRequest", result);
    }

    @Override
    public void onCancel(DialogInterface dialog){
        //「キャンセル」をタップするとタスク名入力ダイアログを表示する
        TaskNameDialogFragment dialogFragment = new TaskNameDialogFragment();
        dialogFragment.show(getParentFragmentManager(), "TaskNameDialog");
    }

}
