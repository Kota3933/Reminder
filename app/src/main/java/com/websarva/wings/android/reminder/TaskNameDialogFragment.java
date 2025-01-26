package com.websarva.wings.android.reminder;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

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

                        //タスク名を取得、Bundleに格納・送信
                        Bundle result = new Bundle();
                        String taskName;
                        EditText etTaskName = view.findViewById(R.id.etTaskName);
                        taskName = etTaskName.getText().toString();
                        //Log.i("BundleCheck", "格納した文字列："+taskName);
                        result.putString("taskName", taskName);
                        manager.setFragmentResult("taskNameRequest", result);

                        //タスク名が要件を満たしているかチェックを行う
                        //タスク名が空欄かチェック
                        if(!taskName.isEmpty()){
                            //タスク名が20文字を超えていないかチェック
                            if(taskName.length() <= 20){
                                //タスク名が条件を満たしている。時刻入力ダイアログを呼び出す
                                dialogFragment.show(manager, "TimePicker");
                            }else{
                                //タスク名が20文字より多いので再度入力させる
                                String msg = getString(R.string.taskNameDialog_warning_tooLong);
                                Toast.makeText(getActivity(),msg,Toast.LENGTH_SHORT).show();
                                TaskNameDialogFragment dialogFragment_self = new TaskNameDialogFragment();
                                dialogFragment_self.show(getParentFragmentManager(), "TaskNameDialogFragment");
                            }
                        }else{
                            //タスク名が空欄なので再度入力させる
                            String msg = getString(R.string.taskNameDialog_warning_empty);
                            Toast.makeText(getActivity(),msg,Toast.LENGTH_SHORT).show();
                            TaskNameDialogFragment dialogFragment_self = new TaskNameDialogFragment();
                            dialogFragment_self.show(getParentFragmentManager(), "TaskNameDialogFragment");
                        }
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
