package io.github.froodyapp.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import io.github.froodyapp.App;
import io.github.froodyapp.R;
import io.github.froodyapp.api.model_.FroodyEntry;
import io.github.froodyapp.api.model_.ResponseEntryAdd;
import io.github.froodyapp.listener.EntryTypeSelectedListener;
import io.github.froodyapp.location.LocationTool;
import io.github.froodyapp.model.FroodyEntryPlus;
import io.github.froodyapp.model.GPS_Types;
import io.github.froodyapp.service.EntryPublisher;
import io.github.froodyapp.service.EntryReverseGeocoder;
import io.github.froodyapp.ui.BaseFragment;
import io.github.froodyapp.ui.CustomDialogs;
import io.github.froodyapp.ui.EntryTypeSelectionDialog;
import io.github.froodyapp.ui.RecyclerEntryTypeAdapter;
import io.github.froodyapp.util.AppCast;
import io.github.froodyapp.util.AppSettings;
import io.github.froodyapp.util.BlockCache;
import io.github.froodyapp.util.FroodyEntryFormatter;
import io.github.froodyapp.util.Helpers;
import io.github.froodyapp.util.MyEntriesHelper;

/**
 * Activity for adding froodys
 */
public class PublishEntryFragment extends BaseFragment implements EntryPublisher.EntryPublishListener, EntryTypeSelectedListener {
    //#####################
    //##      Statics
    //#####################
    public static final String FRAGMENT_TAG = "PublishEntryFragment";

    public static PublishEntryFragment newInstance() {
        return new PublishEntryFragment();
    }

    public interface FroodyEntryPublishedListener {
        void onFroodyEntryPublished(final FroodyEntryPlus entry);
    }

    public void setPublishedListener(FroodyEntryPublishedListener publishedListener) {
        this.publishedListener = publishedListener;
    }

    //####################
    //##  UI-Binding
    //####################

    @BindView(R.id.publish_entry__fragment__text_location_header)
    TextView textLocationHeader;

    @BindView(R.id.publish_entry__fragment__text_contact_header)
    TextView textContactHeader;

    @BindView(R.id.publish_entry__fragment__text_description_header)
    TextView textDescriptionHeader;

    @BindView(R.id.publish_entry__fragment__text_location)
    TextView textLocation;

    @BindView(R.id.publish_entry__fragment__entry_type_image)
    AppCompatImageView imageEntryTypeImage;

    @BindView(R.id.publish_entry__fragment__entry_type_name)
    AppCompatButton textEntryTypeName;

    @BindViews({R.id.publish_entry__fragment__button_distribution__free, R.id.publish_entry__fragment__button_distribution__voluntary_donation, R.id.publish_entry__fragment__button_distribution__sale, R.id.publish_entry__fragment__button_distribution__swap})
    AppCompatButton[] buttonDistribution;

    @BindViews({R.id.publish_entry__fragment__button_certification_none, R.id.publish_entry__fragment__button_certification_bio, R.id.publish_entry__fragment__button_certification_demeter})
    AppCompatButton[] buttonCertification;

    @BindView(R.id.publish_entry__fragment__edit_description)
    EditText editDescription;

    @BindView(R.id.publish_entry__fragment__edit_contact)
    EditText editContact;

    @BindView(R.id.publish_entry__fragment__button_submit_entry)
    TextView buttonSubmitFroodyEntry;

    @BindView(R.id.publish_entry__fragment__scrollview)
    ScrollView scrollView;

    //####################
    //##  Members
    //####################
    private App app;
    private FroodyEntryPlus froodyEntry;
    private LocationTool.LocationToolResponse location;      // Last found location
    private AppSettings appSettings;
    private FroodyEntryPublishedListener publishedListener;

    //####################
    //##  Methods
    //####################
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.publish_entry__fragment, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        app = (App) getActivity().getApplication();
        appSettings = app.getAppSettings();

        // Request the current location
        requestLocationFromMainActivity();

        // Init froody entry
        froodyEntry = new FroodyEntryPlus(new FroodyEntry());
        froodyEntry.setEntryId(-1L);
        froodyEntry.setEntryType(FroodyEntryFormatter.ENTRY_TYPE_UNKNOWN);

        // Load some previously selected options from AppSettingsBase
        setCertificationSelection(appSettings.getLastCertification());
        setDistributionSelection(appSettings.getLastDistribution());
        editContact.setText(appSettings.getLastContactInfo());

        // Simulate a location
        String[] lastFoundLocation = appSettings.getLastFoundLocation();
        if (!lastFoundLocation[0].isEmpty()) {
            Double[] latlng = Helpers.geohashToLatLng(lastFoundLocation[0]);
            if (latlng != null) {
                LocationTool.LocationToolResponse location = new LocationTool.LocationToolResponse();
                location.provider = "net";
                location.lat = latlng[0];
                location.lng = latlng[1];
                this.location = location;
                froodyEntry.loadGeohashFromLocation(latlng[0], latlng[1], 9);
                applyLocationToUi(GPS_Types.PREVIOUS);
                textLocation.setText(lastFoundLocation[1]);
                recheckUserInput();
            }
        }
    }

    @OnClick({R.id.publish_entry__fragment__button_distribution__free, R.id.publish_entry__fragment__button_distribution__voluntary_donation, R.id.publish_entry__fragment__button_distribution__swap, R.id.publish_entry__fragment__button_distribution__sale})
    public void onDistributionButtonClicked(View v) {
        switch (v.getId()) {
            case R.id.publish_entry__fragment__button_distribution__free:
                setDistributionSelection(0);
                break;
            case R.id.publish_entry__fragment__button_distribution__voluntary_donation:
                setDistributionSelection(1);
                break;
            case R.id.publish_entry__fragment__button_distribution__sale:
                setDistributionSelection(2);
                break;
            case R.id.publish_entry__fragment__button_distribution__swap:
                setDistributionSelection(3);
                break;
        }
    }

    private void setDistributionSelection(int index) {
        // Apply to entry
        froodyEntry.setDistributionType(index);
        appSettings.setLastDistribution(index);

        // Apply to UI
        for (AppCompatButton b : buttonDistribution) {
            Helpers.setTintColor(b, R.color.default_button_bgcolor);
            //b.setTextColor(ContextCompat.getColor(getContext(), R.color.primary_text));
        }
        Helpers.setTintColor(buttonDistribution[index], R.color.app_some_yellow);
        //buttonDistribution[index].setTextColor(ContextCompat.getColor(getContext(), R.color.white));
    }

    @OnClick({R.id.publish_entry__fragment__button_certification_bio, R.id.publish_entry__fragment__button_certification_none, R.id.publish_entry__fragment__button_certification_demeter})
    public void onCertificationButtonClicked(View v) {
        switch (v.getId()) {
            case R.id.publish_entry__fragment__button_certification_none:
                setCertificationSelection(0);
                break;
            case R.id.publish_entry__fragment__button_certification_bio:
                setCertificationSelection(1);
                break;
            case R.id.publish_entry__fragment__button_certification_demeter:
                setCertificationSelection(2);
                break;
        }
    }

    private void setCertificationSelection(int index) {
        // Apply to entry
        froodyEntry.setCertificationType(index);
        appSettings.setLastCertification(index);

        // Apply to UI
        for (AppCompatButton b : buttonCertification) {
            Helpers.setTintColor(b, R.color.default_button_bgcolor);
            //b.setTextColor(ContextCompat.getColor(getContext(), R.color.primary_text));
        }
        Helpers.setTintColor(buttonCertification[index], R.color.app_some_yellow);
        //buttonCertification[index].setTextColor(ContextCompat.getColor(getContext(), R.color.white));
    }

    // Checks if form is valid ; True if entry can be submitted
    private boolean hasValidInput() {
        return editContact.getText().toString().length() > 0
                && editDescription.getText().toString().length() > 0
                && location != null && !TextUtils.isEmpty(froodyEntry.getGeohash())
                && (froodyEntry.getEntryType() >= FroodyEntryFormatter.ENTRY_TYPE_MIN
                || froodyEntry.getEntryType() == FroodyEntryFormatter.ENTRY_TYPE_CUSTOM);
    }

    /**
     * Triggers recheck of user input
     */
    private void recheckUserInput() {
        boolean valid = hasValidInput();
        int black = Helpers.getColorFromRes(getContext(), R.color.primary_text);
        int red = Helpers.getColorFromRes(getContext(), R.color.app_very_red);

        textDescriptionHeader.setTextColor(editDescription.getText().toString().isEmpty() ? red : black);
        textContactHeader.setTextColor(editContact.getText().toString().isEmpty() ? red : black);
    }


    // User clicked the submit button
    @OnClick(R.id.publish_entry__fragment__button_submit_entry)
    public void onSubmitFroodyEntryButtonClicked(View view) {
        if (!hasValidInput() || !appSettings.hasFroodyUserId()) {
            showInputHelp();
            return;
        }

        Context context = view.getContext();
        FroodyEntryFormatter entryFormatter = new FroodyEntryFormatter(context, froodyEntry);

        // Apply details to entry
        //froodyEntry.setAddress(textLocation.getText().toString());    // done by EntryReverseGeocoder
        //froodyEntry.setEntryType(); // Done by callback
        entryFormatter.loadGeohashFromLocation(location.lat, location.lng, 9);
        froodyEntry.setUserId(appSettings.getFroodyUser().getUserId());
        froodyEntry.setContact(editContact.getText().toString());
        froodyEntry.setDescription(editDescription.getText().toString());

        // Save last contact info
        appSettings.setLastContactInfo(editContact.getText().toString());

        // Start publishing entry
        new EntryPublisher(getActivity(), froodyEntry, this).start();
    }


    private void showInputHelp() {
        MainActivity activity = (MainActivity) getActivity();
        Snackbar.make(activity.coordinatorLayout, R.string.error_incomplete_input, Snackbar.LENGTH_SHORT).show();

        if (froodyEntry.getEntryType() < FroodyEntryFormatter.ENTRY_TYPE_MIN) {
            focusView(textEntryTypeName);
            return;
        }
        if (editContact.getText().toString().isEmpty()) {
            focusView(editContact);
            return;
        }
        if (editDescription.getText().toString().isEmpty()) {
            focusView(editDescription);
            return;
        }
        if (location == null || TextUtils.isEmpty(froodyEntry.getGeohash())) {
            focusView(textLocation);
            return;
        }
    }

    private final void focusView(final View view) {
      /*  scrollView.post(new Runnable() {
            public void run() {
                scrollView.smoothScrollTo(0, view.getBottom());
                scrollView.requestChildFocus(view, view);
            }
        });*/
        view.getParent().requestChildFocus(view, view);
    }


    // TextChangedListener for EditText's
    @OnTextChanged(callback = OnTextChanged.Callback.TEXT_CHANGED, value = {R.id.publish_entry__fragment__edit_contact, R.id.publish_entry__fragment__edit_description})
    public void onEditTextChanged(CharSequence newText) {
        recheckUserInput();
    }


    //Sets all UI texts related to location
    public void applyLocationToUi(int newGPS_Type) {
        int color = getResources().getIntArray(R.array.gps_colors)[newGPS_Type];
        String locationText = getResources().getStringArray(R.array.gps_types_text)[newGPS_Type];

        // Additional stuff related to GPS-Type
        switch (newGPS_Type) {
            case GPS_Types.NONE: {
                location = null;
                break;
            }
            case GPS_Types.CURRENT: {
                froodyEntry.loadGeohashFromLocation(location.lat, location.lng, 9);

                // Set Location while no more date available
                locationText = locationText.replace("$LOCATION$", String.format(Locale.getDefault(), "(%.5f ; %.5f)", location.lat, location.lng));

                // Replace later with reverse geocode data
                new EntryReverseGeocoder(getContext(), froodyEntry).start();
                break;
            }
            case GPS_Types.PREVIOUS: {
                new EntryReverseGeocoder(getContext(), froodyEntry).start();
                break;
            }
        }
        textLocationHeader.setText(getResources().getStringArray(R.array.gps_types_header)[newGPS_Type]);
        textLocationHeader.setTextColor(color);
        textLocation.setText(locationText);
    }

    // The GPS Button was pressed
    @OnClick(R.id.publish_entry__fragment__entry_type_name)
    public void onSelectFroodyEntryTypeSelectorClicked(View view) {
        EntryTypeSelectionDialog yourDialogFragment = EntryTypeSelectionDialog.newInstance(this, false);
        yourDialogFragment.show(getFragmentManager(), EntryTypeSelectionDialog.FRAGMENT_TAG);
    }

    public void requestLocationFromMainActivity() {
        Activity a = getActivity();
        if (a != null && a instanceof MainActivity) {
            ((MainActivity) a).requestLocation(FRAGMENT_TAG);
        }
    }


    @SuppressWarnings("unchecked")
    private final BroadcastReceiver localBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case AppCast.FROODY_ENTRY_GEOCODED.ACTION: {
                    froodyEntry.setAddress(AppCast.getEntryFromIntent(intent).getAddress());
                    textLocation.setText(froodyEntry.getAddress());
                    recheckUserInput();
                    break;
                }

                case AppCast.LOCATION_FOUND.ACTION: {
                    location = AppCast.LOCATION_FOUND.getResponseFromIntent(intent);
                    applyLocationToUi(GPS_Types.CURRENT);
                    recheckUserInput();
                    break;
                }

            }
        }
    };

    @Override
    public void onResume() {
        Context context = getContext();
        if (context != null) {
            LocalBroadcastManager.getInstance(context).registerReceiver(localBroadcastReceiver, AppCast.getLocalBroadcastFilter());
        }
        MainActivity activity = (MainActivity) getActivity();
        activity.setTitle(R.string.app_name);
        activity.selectTab(1, true);
        super.onResume();
    }

    @Override
    public void onPause() {
        Context context = getContext();
        if (context != null) {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(localBroadcastReceiver);
        }
        super.onPause();
    }

    @Override
    public void onFroodyEntryPublished(ResponseEntryAdd response, boolean wasAdded) {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity == null) {
            return;
        }

        if (wasAdded) {
            // Cleanup UI
            editDescription.setText("");
            imageEntryTypeImage.setImageResource(R.drawable.ic_search_black_64dp);
            textEntryTypeName.setText(R.string.select_froody);

            // Save entry to cache
            froodyEntry.setEntryId(response.getEntryId());
            froodyEntry.setManagementCode(response.getManagementCode());
            froodyEntry.setCreationDate(response.getCreationDate());
            froodyEntry.setModificationDate(response.getCreationDate());
            BlockCache.getInstance().processEntryWithDetails(froodyEntry);
            new MyEntriesHelper(mainActivity).addToMyEntries(froodyEntry);

            CustomDialogs.showShareDialog(mainActivity, froodyEntry);
            mainActivity.getFragmentManager().popBackStack();
            if (publishedListener != null) {
                publishedListener.onFroodyEntryPublished(froodyEntry);
            }
        } else {
            // Could not publish
            Toast.makeText(mainActivity, R.string.error_cannot_publish_entry, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onEntryTypeSelected(int entryType) {
        if ((entryType < FroodyEntryFormatter.ENTRY_TYPE_MIN
                && entryType != FroodyEntryFormatter.ENTRY_TYPE_CUSTOM)
                || getContext() == null) {
            App.log(getClass(), "Error: Bad selection of entry type");
            return;
        }

        // Format entry type line
        FroodyEntryFormatter formatter = new FroodyEntryFormatter(getContext(), froodyEntry);
        froodyEntry.setEntryType(entryType);
        imageEntryTypeImage.setImageResource(formatter.getEntryTypeImageId(R.drawable.general__finger_leading));
        textEntryTypeName.setText(formatter.getEntryTypeName());

        // Disable not allowed elements
        for (AppCompatButton b : buttonCertification) {
            b.setEnabled(formatter.isAllowedToCertify());
        }
        for (AppCompatButton b : buttonDistribution) {
            b.setEnabled(formatter.isAllowedToSell());
        }
        if (!formatter.isAllowedToCertify()) {
            buttonCertification[0].setEnabled(true);
            setDistributionSelection(0);
        }

        if (!formatter.isAllowedToSell()) {
            buttonDistribution[0].setEnabled(true);
            setCertificationSelection(0);
        }


        // Add to history list
        if (entryType != FroodyEntryFormatter.ENTRY_TYPE_CUSTOM) {
            AppSettings settings = app.getAppSettings();
            ArrayList<Integer> history = settings.getLastSelectedEntryTypes();
            if (history.contains(entryType)) {
                history.remove(history.indexOf(entryType));
            }
            history.add(0, entryType);
            while (history.size() >= RecyclerEntryTypeAdapter.MAX_ENTRYTYPE_SUGGESTIONS_COUNT__BY_HISTORY) {
                history.remove(history.get(history.size() - 1));
            }
            settings.setLastSelectedEntryTypes(history);
        }
        recheckUserInput();
    }

    @Override
    public String getFragmentTag() {
        return FRAGMENT_TAG;
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }
}
