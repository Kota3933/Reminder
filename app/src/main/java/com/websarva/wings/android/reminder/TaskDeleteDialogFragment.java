package com.websarva.wings.android.reminder;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class TaskDeleteDialogFragment extends DialogFragment {

    private String name;
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle SavedInstance){
        Bundle extras = getArguments();
        name = extras.getString("name");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("「" + name + "」を削除しますか？");
        builder.setPositiveButton(R.string.dialog_btn_ok, new DialogButtonClickListener());
        builder.setNegativeButton(R.string.dialog_btn_ng, new DialogButtonClickListener());
        AlertDialog dialog = builder.create();
        return dialog;
    }

    private class DialogButtonClickListener implements DialogInterface.OnClickListener{
        @Override
        public void onClick(DialogInterface dialog, int which){
            switch(which){
                case DialogInterface.BUTTON_POSITIVE:
                    DataProcess.TaskDelete(name, getActivity());
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        }
    }
}
