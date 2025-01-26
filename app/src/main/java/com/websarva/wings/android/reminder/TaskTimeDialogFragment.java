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

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        TimePickerDialog dialog = new TimePickerDialog(getActivity(), this, hour, minute+1, DateFormat.is24HourFormat(getActivity()));
        return dialog;
    }



    public void onTimeSet(TimePicker view, int hourOfDay, int minute){
        //String msg = "「" + taskName + "」を"+ hourOfDay + "時" + minute + "分に設定しました";
        //Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();

        //設定時刻をBundleに格納し送信
        Bundle result = new Bundle();
        result.putInt("taskTime_hour", hourOfDay);
        result.putInt("taskTime_minute", minute);
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
