package io.github.froodyapp.activity;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.froodyapp.R;
import io.github.froodyapp.api.invoker.ApiClient;
import io.github.froodyapp.api.invoker.Configuration;
import io.github.froodyapp.service.UserRegisterer;
import io.github.froodyapp.util.AppSettings;
import io.github.froodyapp.util.BlockCache;
import io.github.froodyapp.util.ContextUtils;
import io.github.froodyapp.util.MyEntriesHelper;

public class SettingActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    static final int ACTIVITY_ID = 10;

    static class RESULT {
        static final int NOCHANGE = -1;
        static final int CHANGED = 1;
        static final int RESTART_REQ = 2;
    }

    @BindView(R.id.settings__appbar)
    protected AppBarLayout appBarLayout;
    @BindView(R.id.settings__toolbar)
    protected Toolbar toolbar;

    private AppSettings appSettings;
    public static int activityRetVal = RESULT.NOCHANGE;

    public void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.settings__activity);
        ButterKnife.bind(this);
        toolbar.setTitle(R.string.settings);
        setSupportActionBar(toolbar);
        appSettings = AppSettings.get();
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_white_48px));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SettingActivity.this.onBackPressed();
            }
        });
        showFragment(SettingsFragmentMaster.TAG, false);
    }

    protected void showFragment(String tag, boolean addToBackStack) {
        PreferenceFragment fragment = (PreferenceFragment) getFragmentManager().findFragmentByTag(tag);
        if (fragment == null) {
            switch (tag) {
                case SettingsFragmentMaster.TAG:
                default:
                    fragment = new SettingsFragmentMaster();
                    toolbar.setTitle(R.string.settings);
                    break;
            }
        }
        FragmentTransaction t = getFragmentManager().beginTransaction();
        if (addToBackStack) {
            t.addToBackStack(tag);
        }
        t.replace(R.id.settings__fragment_container, fragment, tag).commit();
    }

    @Override
    protected void onResume() {
        appSettings.registerPreferenceChangedListener(this);
        super.onResume();
    }

    @Override
    protected void onPause() {
        appSettings.unregisterPreferenceChangedListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        activityRetVal = RESULT.CHANGED;
        if (key.equals(getString(R.string.pref_key__froody_server))) {
            BlockCache.getInstance().clearCache(this);
            ApiClient apiClient = Configuration.getDefaultApiClient();
            apiClient.setBasePath(appSettings.getFroodyServer());
            appSettings.setFroodyUserId(-1);
            new UserRegisterer(this).start();
        } else if (key.equals(getString(R.string.pref_key__language))) {
            activityRetVal = RESULT.RESTART_REQ;
        }
    }

    @Override
    protected void onStop() {
        setResult(activityRetVal);
        super.onStop();
    }

    public static class SettingsFragmentMaster extends PreferenceFragment {
        public static final String TAG = "io.github.froodyapp.settings.SettingsFragmentMaster";

        public void onCreate(Bundle savedInstances) {
            super.onCreate(savedInstances);
            getPreferenceManager().setSharedPreferencesName("app");
            addPreferencesFromResource(R.xml.preferences);
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen screen, Preference preference) {
            if (isAdded() && preference.hasKey()) {
                Context context = getActivity().getApplicationContext();
                AppSettings settings = AppSettings.get();
                switch (preference.getTitleRes()) {
                    case R.string.pref_title__clear_cache: {
                        BlockCache.getInstance().clearCache(context);
                        ContextUtils.get().restartApp(MainActivity.class);
                        break;
                    }

                    case R.string.pref_title__reset_app: {
                        BlockCache.getInstance().clearCache(context);
                        new MyEntriesHelper(context).deleteMyEntries();
                        settings.resetSettings();
                        ContextUtils.get().restartApp(MainActivity.class);
                        break;
                    }
                }
            }
            return super.onPreferenceTreeClick(screen, preference);
        }
    }
}
