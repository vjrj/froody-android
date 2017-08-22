package io.github.froodyapp.util;

import android.app.Activity;

@SuppressWarnings("WeakerAccess")
public class HelpersA extends io.github.gsantner.opoc.util.HelpersA {
    protected HelpersA(Activity activity) {
        super(activity);
    }

    public static HelpersA get(Activity activity) {
        return new HelpersA(activity);
    }
}
