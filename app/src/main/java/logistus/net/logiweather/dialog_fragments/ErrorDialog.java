package logistus.net.logiweather.dialog_fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;

import logistus.net.logiweather.R;

public class ErrorDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getActivity();
        Bundle bundle = getArguments();
        String message = bundle.getString("message");
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(R.string.error)
                .setMessage(message)
                .setPositiveButton(R.string.ok, null);
        return builder.create();
    }
}
