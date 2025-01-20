package com.websarva.wings.android.reminder;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import java.util.Calendar;

public class TaskTimeDialogFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener, TimePickerDialog.OnCancelListener{
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        TimePickerDialog dialog = new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
        return dialog;
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute){
        String msg = hourOfDay + "時" + minute + "分を選択しました";
        FragmentManager manger = getParentFragmentManager();
        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onCancel(DialogInterface dialog){
        //「キャンセル」をタップするとタスク名入力ダイアログを表示
        TaskNameDialogFragment dialogFragment = new TaskNameDialogFragment();
        dialogFragment.show(getParentFragmentManager(), "TaskNameDialog");
    }



}
