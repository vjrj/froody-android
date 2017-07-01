package io.github.froodyapp;

import android.app.Application;
import android.support.v7.app.AppCompatDelegate;
import android.text.TextUtils;
import android.util.Log;

import io.github.froodyapp.service.UserRegisterer;
import io.github.froodyapp.util.AppSettings;
import io.github.froodyapp.util.Helpers;
import io.github.froodyapp.util.MyEntriesHelper;


/**
 * The App's Application Object. Available from every activity.
 */
public class App extends Application {
    //#####################
    //## Const
    //#####################
    public static final boolean LOGGING_ENABLED = true;
    private volatile static App app;

    /**
     * Log to console
     *
     * @param classWhereHappening Class where error occurred
     * @param text                text to log
     */
    public static void log(Class classWhereHappening, String text) {
        if (LOGGING_ENABLED && classWhereHappening != null && !TextUtils.isEmpty(text)) {
            Log.d(classWhereHappening.getName(), text);
        }
    }

    public static App get() {
        return app;
    }

    //#####################
    //##  Member
    //#####################
    private AppSettings appSettings;

    //#####################
    //## Methods
    //#####################
    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        appSettings = AppSettings.get();


        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        Helpers.get().setupFroodyApi();

        new UserRegisterer(this).start();
        new MyEntriesHelper(this).processMyEntriesToBlockCache();
    }

    //#####################
    //## Getter & Setter
    //#####################
    public AppSettings getAppSettings() {
        return appSettings;
    }
}
