package com.websarva.wings.android.reminder;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class TaskNameDialogFragment extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.task_name_input_layout, null))
                .setPositiveButton(R.string.taskNameInput_bt_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //「次へ」をタップすると時刻入力ダイアログを表示
                        TaskTimeDialogFragment dialogFragment = new TaskTimeDialogFragment();
                        dialogFragment.show(getParentFragmentManager(), "TimePicker");
                    }
                })
                .setNegativeButton(R.string.taskNameInput_bt_ng, new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        //「キャンセル」をタップすると消える
                        TaskNameDialogFragment.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }

}
