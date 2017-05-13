package io.github.froodyapp.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.webkit.WebView;

import io.github.froodyapp.BuildConfig;
import io.github.froodyapp.R;
import io.github.froodyapp.model.FroodyEntryPlus;
import io.github.froodyapp.util.FroodyEntryFormatter;
import io.github.froodyapp.util.MyEntriesHelper;

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
     * @param c           context
     * @param froodyEntry the froodyEntry
     */
    public static void showShareDialog(final Context c, final FroodyEntryPlus froodyEntry) {
        FroodyEntryFormatter formatter = new FroodyEntryFormatter(c, froodyEntry);
        AlertDialog.Builder dialog = new AlertDialog.Builder(c)
                .setTitle(R.string.share_entry_ask)
                .setIcon(formatter.getEntryTypeImage())
                .setMessage(formatter.summarize() + "\n\n" + c.getString(
                        R.string.limited_time_active_notice, BuildConfig.ENTRY_LIFETIME_DAYS))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.share, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        MyEntriesHelper.shareEntry(c, froodyEntry);
                    }
                });
        dialog.show();
    }

    // Show license infos
    public static void showDialogWithRawFileInWebView(Context context, String fileInRaw, @StringRes int resTitleId) {
        WebView wv = new WebView(context);
        wv.loadUrl("file:///android_res/raw/" + fileInRaw);
        AlertDialog.Builder dialog = new AlertDialog.Builder(context)
                .setPositiveButton(R.string.ok, null)
                .setTitle(resTitleId)
                .setView(wv);
        dialog.show();
    }

    // Show Toast when location permission cant be acquired
    public static void showLocationPermissionNeeededDialog(Context context, DialogInterface.OnDismissListener dismissedListener) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context)
                .setOnDismissListener(dismissedListener)
                .setTitle(R.string.location)
                .setPositiveButton(R.string.ok, null)
                .setIcon(R.drawable.ic_my_location_black_24dp)
                .setMessage(R.string.error_bad_permissions);
        dialog.show();
    }

}
