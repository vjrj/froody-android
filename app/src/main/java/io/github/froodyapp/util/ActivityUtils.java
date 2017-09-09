package io.github.froodyapp.util;

import android.app.Activity;

@SuppressWarnings("WeakerAccess")
public class ActivityUtils extends net.gsantner.opoc.util.ActivityUtils {
    protected ActivityUtils(Activity activity) {
        super(activity);
    }

    public static ActivityUtils get(Activity activity) {
        return new ActivityUtils(activity);
    }
}
