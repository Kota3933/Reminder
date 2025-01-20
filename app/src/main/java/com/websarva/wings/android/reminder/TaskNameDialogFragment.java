package com.websarva.wings.android.reminder;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

public class TaskNameDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.task_name_input_layout, null);

        builder.setView(view)
                .setPositiveButton(R.string.taskNameInput_bt_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        TaskTimeDialogFragment dialogFragment = new TaskTimeDialogFragment();
                        FragmentManager manager = getParentFragmentManager();

                        //タスク名を取得、Bundleに格納
                        Bundle result = new Bundle();
                        String taskName;
                        EditText etTaskName = view.findViewById(R.id.etTaskName);
                        taskName = etTaskName.getText().toString();
                        Log.i("BundleCheck", "格納した文字列："+taskName);
                        result.putString("taskName", taskName);
                        manager.setFragmentResult("taskNameRequest", result);

                        //時刻入力ダイアログを呼び出す
                        dialogFragment.show(manager, "TimePicker");
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
