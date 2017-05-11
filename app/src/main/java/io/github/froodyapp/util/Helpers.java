package io.github.froodyapp.util;


import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.RawRes;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import ch.hsr.geohash.GeoHash;
import io.github.froodyapp.BuildConfig;
import io.github.froodyapp.R;
import io.github.froodyapp.activity.SplashActivity;

/**
 * Some quite useful helpers
 */
public class Helpers {
    /**
     * Animate to activity
     *
     * @param from               from activity
     * @param to                 to activity
     * @param finishFromActivity true if from should be finished
     */
    public static void animateToActivity(Activity from, Class to, boolean finishFromActivity) {
        Intent intent = new Intent(from, to);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        from.startActivity(intent);
        from.overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        if (finishFromActivity) {
            from.finish();
        }
    }

    public static void setTintColor(AppCompatButton button, @ColorRes int color) {
        button.setSupportBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(button.getContext(), color)
        ));
    }

    public static boolean isConnectedToInternet(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetInfo != null && activeNetInfo.isConnectedOrConnecting();
    }


    public static String latLngToGeohash(double lat, double lng, int precision) {
        return GeoHash.withCharacterPrecision(lat, lng, precision).toBase32();
    }

    public static Double[] geohashToLatLng(String geohash) {
        GeoHash gh = GeoHash.fromGeohashString(geohash);
        if (gh != null && gh.getPoint() != null) {
            return new Double[]{
                    gh.getPoint().getLatitude(),
                    gh.getPoint().getLongitude()
            };
        }
        return null;
    }

    public static void restartApp(Context context) {
        Intent restartIntent = new Intent(context, SplashActivity.class);
        PendingIntent restartIntentP = PendingIntent.getActivity(context, 555,
                restartIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, restartIntentP);
        System.exit(0);
    }

    public static DateTime getNow() {
        return DateTime.now(DateTimeZone.UTC);
    }

    public static Drawable getDrawableFromRes(Context c, @DrawableRes int resId) {
        return ContextCompat.getDrawable(c, resId);
    }

    public static int getColorFromRes(Context context, @ColorRes int resId) {
        return ContextCompat.getColor(context, resId);
    }

    public static String readTextfileFromRawRes(Context context, @RawRes int rawRessourceId, String linePrefix, String linePostfix) {
        StringBuilder sb = new StringBuilder();
        String line;
        BufferedReader br = null;
        linePrefix = linePrefix == null ? "" : linePrefix;
        linePostfix = linePostfix == null ? "" : linePostfix;

        try {
            br = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(rawRessourceId)));
            while ((line = br.readLine()) != null) {
                sb.append(linePrefix);
                sb.append(line);
                sb.append(linePostfix);
                sb.append("\n");
            }
        } catch (Exception ignored) {
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ignored) {
                }
            }
        }
        return sb.toString();
    }

    public static void donateBitcoinRequest(Context context) {
        if (!BuildConfig.IS_GPLAY_BUILD) {
            String btcUri = String.format("bitcoin:%s?amount=%s&label=%s&message=%s",
                    "1B9ZyYdQoY9BxMe9dRUEKaZbJWsbQqfXU5", "0.01", "Have some coke, and a nice day", "Have some coke, and a nice day");
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(btcUri));
            try {
                context.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                openWebpageWithExternalBrowser(context, "https://gsantner.github.io/donate/#donate");
            }
        }
    }

    public static void openWebpageWithExternalBrowser(Context context, String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        context.startActivity(intent);
    }

    public static String loadMarkdownFromRawForTextView(Context context, @RawRes int rawMdFile, String prepend) {
        try {
            return new SimpleMarkdownParser()
                    .parse(context.getResources().openRawResource(rawMdFile),
                            SimpleMarkdownParser.FILTER_ANDROID_TEXTVIEW, prepend)
                    .replaceColor("#000001", ContextCompat.getColor(context, R.color.accent))
                    .removeMultiNewlines().replaceBulletCharacter("*").getHtml();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    // Show HTML a TextView in a scrollable Dialog
    public static void showDialogWithHtmlTextView(Context context, DialogInterface.OnDismissListener dismissedListener, @StringRes int resTitleId, String html) {
        LinearLayout layout = new LinearLayout(context);
        TextView textView = new TextView(context);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        ScrollView root = new ScrollView(context);
        int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20,
                context.getResources().getDisplayMetrics());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(margin, 0, margin, 0);
        layout.setLayoutParams(layoutParams);

        layout.addView(textView);
        root.addView(layout);

        textView.setText(new SpannableString(Html.fromHtml(html)));
        AlertDialog.Builder dialog = new AlertDialog.Builder(context)
                .setPositiveButton(android.R.string.ok, null)
                .setTitle(resTitleId)
                .setOnDismissListener(dismissedListener)
                .setView(root);
        dialog.show();
    }
}
