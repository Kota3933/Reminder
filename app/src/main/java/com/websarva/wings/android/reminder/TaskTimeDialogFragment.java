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

    String taskName; //前のダイアログで入力されたタスク名

    @Override
    public void onCreate(@Nullable Bundle savedInstance){
        super.onCreate(savedInstance);
        //setContentView(R.layout.activity_main);
        //ダイアログの生成時にFragmentManagerからタスク名を受け取る
        //親FragmentManagerを取得
        FragmentManager manager = getParentFragmentManager();
        //requestKeyで指定したBundleの中からタスク名の文字列を取得する
        manager.setFragmentResultListener("taskNameRequest", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                taskName = result.getString("taskName");
                Log.i("BundleCheck", "受け取った文字列："+taskName);
            }
        });
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        TimePickerDialog dialog = new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
        return dialog;
    }



    public void onTimeSet(TimePicker view, int hourOfDay, int minute){
        String msg = "「" + taskName + "」を"+ hourOfDay + "時" + minute + "分に設定しました";
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
