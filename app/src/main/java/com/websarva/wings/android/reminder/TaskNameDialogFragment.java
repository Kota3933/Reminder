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
                        //時刻入力ダイアログを呼び出す
                    }
                })
                .setNegativeButton(R.string.taskNameInput_bt_ng, new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        TaskNameDialogFragment.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }

    public class DialogButtonClickListener implements DialogInterface.OnClickListener{
        @Override
        public void onClick(DialogInterface dialog, int which){
            switch(which){
                case DialogInterface.BUTTON_POSITIVE:
                    TaskNameDialogFragment dialogFragment = new TaskNameDialogFragment();
                    dialogFragment.show(getParentFragmentManager(), "TaskNameDialogFragment");
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        }
    }
}
