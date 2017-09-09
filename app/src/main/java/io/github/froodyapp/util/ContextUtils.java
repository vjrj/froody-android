package io.github.froodyapp.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import ch.hsr.geohash.GeoHash;
import io.github.froodyapp.App;
import io.github.froodyapp.api.invoker.ApiClient;
import io.github.froodyapp.api.invoker.Configuration;

@SuppressWarnings("WeakerAccess")
public class ContextUtils extends net.gsantner.opoc.util.ContextUtils {
    protected ContextUtils(Context context) {
        super(context);
    }


    public static ContextUtils get() {
        return new ContextUtils(App.get());
    }


    public static DateTime getNow() {
        return DateTime.now(DateTimeZone.UTC);
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

    public void setupFroodyApi() {
        AppSettings appSettings = AppSettings.get();
        String server = appSettings.getFroodyServer();
        ApiClient apiClient = Configuration.getDefaultApiClient();
        apiClient.setBasePath(server);

        if (appSettings.isNetworkHttpProxyEnabled()) {
            apiClient.getHttpClient().setProxy(appSettings.getNetworkHttpProxy());
        }
    }

    public Bitmap drawTextToBitmap(@DrawableRes int resId, String text, int textSize) {
        Resources resources = _context.getResources();
        float scale = resources.getDisplayMetrics().density;
        Bitmap bitmap = getBitmapFromDrawable(resId);

        bitmap = bitmap.copy(bitmap.getConfig(), true);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.rgb(61, 61, 61));
        paint.setTextSize((int) (textSize * scale));
        paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);

        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        int x = (bitmap.getWidth() - bounds.width()) / 2;
        int y = (bitmap.getHeight() + bounds.height()) / 2;
        canvas.drawText(text, x, y, paint);

        return bitmap;
    }

    public Bitmap getBitmapFromDrawable(int drawableId) {
        Bitmap bitmap = null;
        Drawable drawable = ContextCompat.getDrawable(_context, drawableId);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && (drawable instanceof VectorDrawable || drawable instanceof VectorDrawableCompat)) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                drawable = (DrawableCompat.wrap(drawable)).mutate();
            }

            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
        } else if (drawable instanceof BitmapDrawable) {
            bitmap = ((BitmapDrawable) drawable).getBitmap();
        }
        return bitmap;
    }

}
