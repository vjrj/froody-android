package io.github.froodyapp.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.support.v7.app.AlertDialog;
import android.webkit.WebView;

import io.github.froodyapp.R;
import io.github.froodyapp.model.FroodyEntryPlus;
import io.github.froodyapp.util.FroodyEntryFormatter;

/**
 * Some dialogs shown in the app
 */
public class CustomDialogs {
    /**
     * Show a dialog which asks to delete the entry
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
     * Show a sharing dialog
     *
     * @param context             context
     * @param entry               the entry
     * @param onConfirmedListener reuslt listener
     */
    public static void showShareDialog(Context context, FroodyEntryPlus entry, DialogInterface.OnClickListener onConfirmedListener) {
        TypedArray imgs = context.getResources().obtainTypedArray(R.array.entry_type__images);
        FroodyEntryFormatter util = new FroodyEntryFormatter(context, entry);
        AlertDialog.Builder dialog = new AlertDialog.Builder(context)
                .setTitle(R.string.share_entry_ask)
                .setIcon(imgs.getResourceId(entry.getEntryType(), -1))
                .setMessage(context.getString(R.string.limited_time_active_notice) + "\n\n" + util.summarize())
                .setNegativeButton(R.string.no, onConfirmedListener)
                .setPositiveButton(R.string.yes, onConfirmedListener);
        dialog.show();
        imgs.recycle();
    }

    /**
     * Show license infos
     *
     * @param context context
     */
    public static void showLicensesDialog(Context context) {
        WebView wv = new WebView(context);
        wv.loadUrl("file:///android_res/raw/licenses.html");
        AlertDialog.Builder dialog = new AlertDialog.Builder(context)
                .setPositiveButton(R.string.ok, null)
                .setTitle(R.string.license)
                .setView(wv);
        dialog.show();
    }

    /**
     * Show Dialog when location permission cant be acquired
     *
     * @param context context
     */
    public static void showErrorLocationPermDeniedDialog(Context context) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context)
                .setPositiveButton(R.string.ok, null)
                .setTitle(R.string.error)
                .setMessage(R.string.error_bad_permissions);
        dialog.show();
    }

}
