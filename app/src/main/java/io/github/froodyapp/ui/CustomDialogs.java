package io.github.froodyapp.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.webkit.WebView;
import android.widget.Toast;

import io.github.froodyapp.R;
import io.github.froodyapp.model.FroodyEntryPlus;
import io.github.froodyapp.util.FroodyEntryFormatter;

/**
 * Some dialogs to be shown in the app
 */
public class CustomDialogs {
    /**
     * Show a dialog which asks to delete a FroodyEntry
     *
     * @param context             context
     * @param entry               entry
     * @param onConfirmedListener result listener
     */
    public static void showDeleteDialog(Context context, FroodyEntryPlus entry, DialogInterface.OnClickListener onConfirmedListener) {
        String msg = new FroodyEntryFormatter(context, entry).summarize();
        AlertDialog.Builder dialog = new AlertDialog.Builder(context)
                .setTitle(R.string.really_delete_entry)
                .setNegativeButton(R.string.no, null)
                .setMessage(msg)
                .setPositiveButton(R.string.yes, onConfirmedListener);
        dialog.show();
    }

    /**
     * Show a sharing dialog for FroodyEntry
     *
     * @param context             context
     * @param entry               the entry
     * @param onConfirmedListener reuslt listener
     */
    public static void showShareDialog(Context context, FroodyEntryPlus entry, DialogInterface.OnClickListener onConfirmedListener) {
        FroodyEntryFormatter formatter = new FroodyEntryFormatter(context, entry);
        AlertDialog.Builder dialog = new AlertDialog.Builder(context)
                .setTitle(R.string.share_entry_ask)
                .setIcon(formatter.getEntryTypeImage())
                .setMessage(formatter.summarize() + "\n\n" + context.getString(R.string.limited_time_active_notice))
                .setNegativeButton(R.string.no, onConfirmedListener)
                .setPositiveButton(R.string.yes, onConfirmedListener);
        dialog.show();
    }

    // Show license infos
    public static void showLicensesDialog(Context context) {
        WebView wv = new WebView(context);
        wv.loadUrl("file:///android_res/raw/licenses.html");
        AlertDialog.Builder dialog = new AlertDialog.Builder(context)
                .setPositiveButton(R.string.ok, null)
                .setTitle(R.string.license)
                .setView(wv);
        dialog.show();
    }

    // Show Toast when location permission cant be acquired
    public static void showErrorLocationPermDeniedDialog(Context context) {
        Toast.makeText(context, R.string.error_bad_permissions, Toast.LENGTH_LONG).show();
    }

}
