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

    private boolean isEditMode = false;
    private String preTaskName = "";
    private int preHour = 0;
    private int preMin = 0;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.task_name_input_layout, null);

        EditText etTaskName = view.findViewById(R.id.etTaskName);
        //引継ぎデータを確認し、タスクの新規登録か編集かを判別する
        Bundle extras = getArguments();
        if(extras != null){
            isEditMode = true;
            preTaskName = (String)extras.get("preTaskName");
            preHour = (int)extras.get("preHour");
            preMin = (int)extras.get("preMin");
            etTaskName.setText(preTaskName);
            Log.i("TaskEdit", "@TaskNameDF 引継ぎデータあり。タスク名：" + preTaskName);
            Log.i("TaskEdit", "@TaskNameDF 引継ぎデータあり。タスク時間は" + preHour + ":" + preMin);
        }else{
            Log.i("TaskEdit", "@TaskNameDF 引継ぎデータなし");
        }


        builder.setView(view)
                .setPositiveButton(R.string.taskNameInput_bt_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        TaskTimeDialogFragment dialogFragment = new TaskTimeDialogFragment();
                        FragmentManager manager = getParentFragmentManager();


                        //タスク名を取得、Bundleに格納・送信
                        Bundle result = new Bundle();
                        String taskName;
                        taskName = etTaskName.getText().toString();
                        result.putString("taskName", taskName);
                        manager.setFragmentResult("taskNameRequest", result);


                        //タスク名が要件を満たしているかチェックを行う
                        //タスク名が空欄かチェック
                        if(!taskName.isEmpty()){
                            //タスク名が20文字を超えていないかチェック
                            if(taskName.length() <= 20){
                                //タスク名が条件を満たしている
                                //タスク編集モードなら、編集前タスク名を送信
                                if(isEditMode){
                                    Bundle export = new Bundle();
                                    export.putString("preTaskName", preTaskName);
                                    export.putInt("preHour", preHour);
                                    export.putInt("preMin", preMin);
                                    dialogFragment.setArguments(export);
                                    Log.i("TaskEdit", "@TaskNameDF Bundleの中身は" + export.getInt("preHour") + ":" + export);
                                    Log.i("TaskEdit", "@TaskNameDF 変更前タスク名" + preTaskName + "を送信");
                                    Log.i("TaskEdit", "@TaskNameDF 変更前タスク時刻" + preHour + ":" + preMin + "を送信");
                                }
                                //タスク時刻入力ダイアログを表示
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
