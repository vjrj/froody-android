package io.github.froodyapp;

import android.app.Application;
import android.support.v7.app.AppCompatDelegate;
import android.text.TextUtils;
import android.util.Log;

import io.github.froodyapp.api.invoker.Configuration;
import io.github.froodyapp.service.UserRegisterer;
import io.github.froodyapp.util.AppSettings;
import io.github.froodyapp.util.MyEntriesHelper;


/**
 * The App's Application Object. Available from every activity.
 */
public class App extends Application {
    //#####################
    //## Const
    //#####################
    public static final boolean LOGGING_ENABLED = true;

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

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        appSettings = new AppSettings(this);
        String server = appSettings.getFroodyServer();
        Configuration.getDefaultApiClient().setBasePath(server);
        UserRegisterer.registerUserIfNotRegistered(this);

        new MyEntriesHelper(this).processMyEntriesToBlockCache();
    }

    //#####################
    //## Getter & Setter
    //#####################
    public AppSettings getAppSettings() {
        return appSettings;
    }
}
