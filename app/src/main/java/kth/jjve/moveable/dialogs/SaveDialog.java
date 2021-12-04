package kth.jjve.moveable.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import kth.jjve.moveable.R;

public class SaveDialog extends AppCompatDialogFragment {
    private EditText inputFileName;
    private SaveDialogListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_save, null);

        builder.setView(view)
                .setTitle("Name for results")
                .setNegativeButton("cancel", (dialog, which) -> {
                    listener.savingCancelled();
                })
                .setPositiveButton("save result", (dialog, which) -> {
                    String resultName = inputFileName.getText().toString();
                    listener.applyName(resultName);
                });
        inputFileName = view.findViewById(R.id.edit_inputname);
        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (SaveDialogListener) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString() +
                    "must implement SaveDialogListener");
        }
    }

    public interface SaveDialogListener{
        void applyName(String name);

        void savingCancelled();
    }
}
