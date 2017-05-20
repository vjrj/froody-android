package io.github.froodyapp.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import ch.hsr.geohash.GeoHash;
import io.github.froodyapp.App;
import io.github.froodyapp.R;
import io.github.froodyapp.api.model_.FroodyEntry;
import io.github.froodyapp.listener.FroodyEntrySelectedListener;
import io.github.froodyapp.location.LocationTool;
import io.github.froodyapp.model.FroodyEntryPlus;
import io.github.froodyapp.service.EntryByBlockLoader;
import io.github.froodyapp.service.EntryDetailsLoader;
import io.github.froodyapp.ui.BaseFragment;
import io.github.froodyapp.util.AppCast;
import io.github.froodyapp.util.AppSettings;
import io.github.froodyapp.util.BlockCache;
import io.github.froodyapp.util.Helpers;
import io.github.gsantner.opoc.util.SimpleMarkdownParser;


/**
 * Main Activity of the app
 */
public class MainActivity extends AppCompatActivity implements FroodyEntrySelectedListener,
        PublishEntryFragment.FroodyEntryPublishedListener, TabLayout.OnTabSelectedListener {
    //########################
    //## Static
    //########################
    private static final String REQUEST_BY_SHARED_INTO_APP = "REQUEST_BY_SHARED_INTO_APP";

    //########################
    //## UI Binding
    //########################
    @BindView(R.id.main__activity__tablayout)
    TabLayout tabLayout;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.main__coordinator_layout)
    CoordinatorLayout coordinatorLayout;

    //#####################
    //## Members
    //#####################
    private FragmentManager fragmentManager;
    private App app;
    private AppSettings appSettings;
    private LocationTool locationTool;
    private LocationTool.LocationToolResponse lastFoundLocation;
    private Snackbar snackbarJumpToFoundLocation;
    private int lastSelectedTab = 0;
    private boolean onlyUpdateTabUi = false;

    //#####################
    //## Methods
    //#####################
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main__activity);
        ButterKnife.bind(this);

        app = (App) getApplication();
        appSettings = app.getAppSettings();
        locationTool = new LocationTool(appSettings.getAllowLocationListeningNetwork(), appSettings.getAllowLocationListeningGps());
        fragmentManager = getSupportFragmentManager();

        setupBars();

        // Load block cache & show map fragment
        try {
            BlockCache.getInstance().loadFromAppCache(this);
        } catch (Exception ignored) {
            App.log(getClass(), "Error: Cannot load CacheMap from settings");
        }
        if (savedInstanceState == null) {
            MapOSMFragment mapOSMFragment = (MapOSMFragment) getFragment(MapOSMFragment.FRAGMENT_TAG);
            showFragment(mapOSMFragment);
        }
        handleIntent(getIntent());


        // Show first start dialog / changelog
        String dialogHtml = null;
        int dialogTitleResId = 0;
        try {
            if (appSettings.isAppFirstStart()) {
                dialogHtml = new SimpleMarkdownParser().parse(
                        getResources().openRawResource(R.raw.licenses_3rd_party),
                        SimpleMarkdownParser.FILTER_ANDROID_TEXTVIEW, "").getHtml();
                dialogTitleResId = R.string.license;
            } else if (appSettings.isAppFirstStartCurrentVersion()) {
                dialogHtml = new SimpleMarkdownParser().parse(
                        getResources().openRawResource(R.raw.changelog),
                        SimpleMarkdownParser.FILTER_ANDROID_TEXTVIEW, "").getHtml();
                dialogTitleResId = R.string.changelog;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Show dialog
        if (dialogHtml != null) {
            Helpers.showDialogWithHtmlTextView(this, new DialogInterface.OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    requestLocation(getClass().getName());
                }
            }, dialogTitleResId, dialogHtml);
        } else {
            requestLocation(getClass().getName());
        }

        // Setup tabs
        TabLayout.Tab tab = tabLayout.newTab();
        tab.setText(R.string.map);
        tabLayout.addTab(tab);
        tab = tabLayout.newTab();
        tab.setText(R.string.publish_entry);
        tabLayout.addTab(tab);
        tab = tabLayout.newTab();
        tab.setText(R.string.more);
        tabLayout.addTab(tab);
        tabLayout.addOnTabSelectedListener(this);
        selectTab(0, false);
    }

    /**
     * Setup the toolbar & navigation bar
     */
    private void setupBars() {
        // Toolbar
        setSupportActionBar(toolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayShowTitleEnabled(true);
        }

        // Snackbar Location
        snackbarJumpToFoundLocation = Snackbar
                .make(coordinatorLayout, R.string.show_map_on_current_location, 1000 * 10)
                .setAction(R.string.yes, new View.OnClickListener() {
                    public void onClick(View view) {
                        BaseFragment baseFragment = getCurrentVisibleFragment();
                        if (baseFragment != null && baseFragment.isAdded()
                                && baseFragment.getFragmentTag().equals(MapOSMFragment.FRAGMENT_TAG)
                                && lastFoundLocation != null) {
                            MapOSMFragment osmFragment = (MapOSMFragment) baseFragment;
                            int zoomLevel = 12;
                            if (lastFoundLocation.provider.equals(LocationManager.GPS_PROVIDER)) {
                                zoomLevel = MapOSMFragment.ZOOMLEVEL_BLOCK5_TRESHOLD;
                            }
                            osmFragment.zoomToPosition(lastFoundLocation.lat, lastFoundLocation.lng, zoomLevel);
                        }
                    }
                });
    }

    public void showFragment(BaseFragment fragment) {
        // Close everything
        snackbarJumpToFoundLocation.dismiss();

        // Show fragment
        BaseFragment currentTop = (BaseFragment) fragmentManager.findFragmentById(R.id.main__activity__fragment_placeholder);
        if (currentTop == null || !currentTop.getFragmentTag().equals(fragment.getFragmentTag())) {
            fragmentManager.beginTransaction().addToBackStack(null).replace(R.id.main__activity__fragment_placeholder
                    , fragment, fragment.getFragmentTag()).commit();
        }
        supportInvalidateOptionsMenu();
    }


    public synchronized BaseFragment getFragment(String fragmentTag) {
        BaseFragment fragment = (BaseFragment) fragmentManager.findFragmentByTag(fragmentTag);
        if (fragment != null) {
            return fragment;
        }

        switch (fragmentTag) {
            case MapOSMFragment.FRAGMENT_TAG: {
                MapOSMFragment frag = MapOSMFragment.newInstance();
                fragmentManager.beginTransaction().add(frag, fragmentTag).commit();
                return frag;
            }
            case PublishEntryFragment.FRAGMENT_TAG: {
                PublishEntryFragment frag = PublishEntryFragment.newInstance();
                frag.setPublishedListener(this);
                fragmentManager.beginTransaction().add(frag, fragmentTag).commit();
                return frag;
            }
            case InfoFragment.FRAGMENT_TAG: {
                InfoFragment frag = InfoFragment.newInstance();
                fragmentManager.beginTransaction().add(frag, fragmentTag).commit();
                return frag;
            }
            case MoreFragment.FRAGMENT_TAG: {
                MoreFragment frag = MoreFragment.newInstance();
                fragmentManager.beginTransaction().add(frag, fragmentTag).commit();
                return frag;
            }
            case DevFragment.FRAGMENT_TAG: {
                DevFragment frag = DevFragment.newInstance();
                fragmentManager.beginTransaction().add(frag, fragmentTag).commit();
                return frag;
            }
            default: {
                return getCurrentVisibleFragment();
            }
        }

    }

    private BaseFragment getCurrentVisibleFragment() {
        return (BaseFragment) fragmentManager.findFragmentById(R.id.main__activity__fragment_placeholder);
    }

    @Override
    protected void onResume() {
        LocalBroadcastManager.getInstance(this).registerReceiver(localBroadcastReceiver, AppCast.getLocalBroadcastFilter());
        super.onResume();
    }

    public void requestLocation(String requestedBy) {
        if (locationTool.askForLocationPermission(this, false)) {
            if (appSettings.getAllowLocationListeningAny()) {
                boolean enabled = locationTool.requestLocation(this, requestedBy);
                if (!enabled && getClass().getName().equals(requestedBy)) {
                    Toast.makeText(getApplicationContext(), R.string.cannot_locate_user, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String action;
        Uri data;
        if (intent == null || (action = intent.getAction()) == null) {
            return;
        }

        switch (action) {
            case Intent.ACTION_VIEW: {
                if ((data = intent.getData()) != null) {
                    String server = data.getQueryParameter("server");
                    String sEntryId = data.getQueryParameter("entryId");
                    try {
                        long entryId = Long.parseLong(data.getQueryParameter("entryId"));
                        // Check if server is default server
                        boolean wasRequestForMyServer = server == null && appSettings.getFroodyServer().equals(getString(R.string.server_default));
                        if (server != null && appSettings.getFroodyServer().equals(server)) {
                            wasRequestForMyServer = true;
                        }
                        if (wasRequestForMyServer) {
                            FroodyEntryPlus entry = new FroodyEntryPlus(new FroodyEntry());
                            entry.setEntryId(entryId);
                            new EntryDetailsLoader(this, entry, null, REQUEST_BY_SHARED_INTO_APP).start();
                        }
                    } catch (NumberFormatException | NullPointerException ex) {
                        return;
                    }
                }
                break;
            }
        }

    }

    @Override
    public void onBackPressed() {
        BaseFragment visibleFrag = getCurrentVisibleFragment();
        if (visibleFrag != null) {
            if (!visibleFrag.onBackPressed()) {
                if (fragmentManager.getBackStackEntryCount() > 0) {
                    fragmentManager.popBackStack();
                    supportInvalidateOptionsMenu();
                    return;
                }
            }
        }
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        appSettings.setAppFirstStart(false);
        BlockCache.getInstance().saveToAppCache(this);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(localBroadcastReceiver);
        locationTool.disableLocationTool();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main__activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add: {
                showFragment(getFragment(PublishEntryFragment.FRAGMENT_TAG));
                return true;
            }

            case R.id.action_get_location: {
                requestLocation(getClass().getName());
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFroodyEntrySelected(FroodyEntryPlus entry) {
        BotsheetEntrySingle bottomSheetDialogFragment = BotsheetEntrySingle.newInstance(entry);
        bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
    }

    public void onLocationFound(LocationTool.LocationToolResponse location) {
        this.lastFoundLocation = location;
        BaseFragment frag = getCurrentVisibleFragment();

        if (location != null && frag != null && frag.isAdded()) {
            // Check if the map is visible
            if (frag.getFragmentTag().equals(MapOSMFragment.FRAGMENT_TAG)) {
                MapOSMFragment mapFragment = (MapOSMFragment) frag;
                new EntryByBlockLoader(this, location.lat, location.lng,
                        MapOSMFragment.ZOOMLEVEL_BLOCK4_TRESHOLD).start();


                // Show snackbar if far away
                boolean isNear = false;
                String curPosHash = Helpers.latLngToGeohash(location.lat, location.lng, 5);
                GeoHash center = mapFragment.getMapCenterAsGeohash(5);
                for (GeoHash gh : center.getAdjacent()) {
                    isNear = gh.toBase32().equals(curPosHash) || isNear;
                }
                isNear = center.toBase32().equals(curPosHash) || isNear;
                if (!isNear && !snackbarJumpToFoundLocation.isShownOrQueued()) {
                    snackbarJumpToFoundLocation.show();
                }
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        BaseFragment baseFrag = getCurrentVisibleFragment();
        if (baseFrag != null && baseFrag.isAdded() && baseFrag.getFragmentTag().equals(MapOSMFragment.FRAGMENT_TAG)) {
            MapOSMFragment mapFragment = (MapOSMFragment) baseFrag;
            mapFragment.clearEntries();
            mapFragment.loadEntriesFromBlockCache();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @SuppressWarnings("unchecked")
    private final BroadcastReceiver localBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {

                case AppCast.FROODY_ENTRIES_LOADED.ACTION: {
                    ArrayList<FroodyEntryPlus> entries = AppCast.getFroodyEntryListFromIntent(
                            intent, AppCast.FROODY_ENTRIES_LOADED.EXTRA_FROODY_ENTRIES);

                    BaseFragment baseFrag = getCurrentVisibleFragment();
                    if (baseFrag != null && baseFrag.isAdded() && baseFrag.getFragmentTag().equals(MapOSMFragment.FRAGMENT_TAG)) {
                        MapOSMFragment mapFragment = (MapOSMFragment) baseFrag;
                        mapFragment.addFroodyEntriesToCluster(entries);
                    }
                    break;
                }

                case AppCast.FROODY_ENTRY_TAPPED.ACTION: {
                    FroodyEntryPlus entry = AppCast.getEntryFromIntent(intent);
                    onFroodyEntrySelected(entry);
                    break;
                }

                case AppCast.FROODY_ENTRIES_TAPPED.ACTION: {
                    ArrayList<FroodyEntryPlus> entries = AppCast.getFroodyEntryListFromIntent(
                            intent, AppCast.FROODY_ENTRIES_LOADED.EXTRA_FROODY_ENTRIES);
                    BotsheetEntryMulti frag = BotsheetEntryMulti.newInstance(entries);
                    frag.show(getSupportFragmentManager(), BotsheetEntryMulti.FRAGMENT_TAG);
                    break;
                }

                case AppCast.MAP_POSITION_CHANGED.ACTION: {
                    double lat = intent.getDoubleExtra(AppCast.MAP_POSITION_CHANGED.EXTRA_LATITUDE, 0.0);
                    double lng = intent.getDoubleExtra(AppCast.MAP_POSITION_CHANGED.EXTRA_LONGITUDE, 0.0);
                    int zoom = intent.getIntExtra(AppCast.MAP_POSITION_CHANGED.EXTRA_ZOOM, 15);

                    new EntryByBlockLoader(context, lat, lng, zoom).start();

                    if (zoom >= 4) {
                        appSettings.setLastMapLocation(lat, lng, zoom);
                    }
                    break;
                }

                case AppCast.LOCATION_FOUND.ACTION: {
                    onLocationFound(AppCast.LOCATION_FOUND.getResponseFromIntent(intent));
                    break;
                }

                case AppCast.NO_FOUND_LOCATION.ACTION: {
                    Toast.makeText(getApplicationContext(), R.string.cannot_locate_user, Toast.LENGTH_LONG).show();
                    break;
                }

                case AppCast.FROODY_ENTRY_DELETED.ACTION: {
                    FroodyEntryPlus entry = AppCast.getEntryFromIntent(intent);
                    onFroodyEntryDeleted(entry, intent.getBooleanExtra(AppCast.FROODY_ENTRY_DELETED.EXTRA_WAS_DELETED, false));
                    break;
                }

                case AppCast.FROODY_ENTRY_DETAILS_LOADED.ACTION: {
                    if (REQUEST_BY_SHARED_INTO_APP.equals(intent.getStringExtra(AppCast.FROODY_ENTRY_DETAILS_LOADED.EXTRA_REQUESTED_BY))) {
                        FroodyEntryPlus entry = AppCast.getEntryFromIntent(intent);
                        onFroodyEntrySelected(entry);

                        BaseFragment baseFrag = getCurrentVisibleFragment();
                        if (baseFrag != null && baseFrag.isAdded() && baseFrag.getFragmentTag().equals(MapOSMFragment.FRAGMENT_TAG)) {
                            MapOSMFragment mapFragment = (MapOSMFragment) baseFrag;
                            mapFragment.addOrUpdateFroodyEntryToCluster(entry, true);
                            mapFragment.zoomToPosition(entry.getLatitude(), entry.getLongitude(), 17);
                        }
                    }

                    break;
                }
            }
        }
    };

    private void onFroodyEntryDeleted(FroodyEntryPlus entry, boolean successfullyDeleted) {
        if (!successfullyDeleted) {
            App.log(getClass(), "ERROR: Cannot delete entry");
            return;
        }

        BaseFragment baseFragment = getCurrentVisibleFragment();
        if (baseFragment != null && baseFragment.isAdded() && baseFragment.getFragmentTag().equals(MapOSMFragment.FRAGMENT_TAG)) {
            MapOSMFragment mapFragment = (MapOSMFragment) baseFragment;
            mapFragment.removeFroodyEntryFromCluster(entry);
        }
    }

    public void onFroodyEntryPublished(FroodyEntryPlus entry) {
        // Update Map
        MapOSMFragment frag = (MapOSMFragment) getFragment(MapOSMFragment.FRAGMENT_TAG);
        showFragment(frag);
        if (frag != null && frag.isAdded()) {
            frag.addOrUpdateFroodyEntryToCluster(entry, true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int req, @NonNull String[] perm, @NonNull int[] grantResults) {
        boolean somethingGranted = grantResults.length > 0;
        switch (req) {
            case LocationTool.REQUEST_LOCATION_PERM:
                if (somethingGranted && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestLocation(getClass().getName());
                    return;
                }
                break;
        }
    }

    @SuppressWarnings("ConstantConditions")
    public void selectTab(int pos, boolean onlyUpdateUi) {
        pos = pos >= 0 ? pos : tabLayout.getTabCount() - 1;
        pos = pos < tabLayout.getTabCount() ? pos : 0;
        if (pos != lastSelectedTab) {
            onlyUpdateTabUi = onlyUpdateUi;
            TabLayout.Tab tab = tabLayout.getTabAt(pos);
            tab.select();
        }
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        if (!onlyUpdateTabUi) {
            switch (tab.getPosition()) {
                case 0: {
                    showFragment(getFragment(MapOSMFragment.FRAGMENT_TAG));
                    break;
                }
                case 1: {
                    showFragment(getFragment(PublishEntryFragment.FRAGMENT_TAG));
                    break;
                }
                case 2: {
                    if (lastSelectedTab != 2) {
                        showFragment(getFragment(MoreFragment.FRAGMENT_TAG));
                    }
                    break;
                }
            }
        }
        onlyUpdateTabUi = false;
        lastSelectedTab = tab.getPosition();
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
    }
}
