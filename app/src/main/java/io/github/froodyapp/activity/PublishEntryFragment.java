package io.github.froodyapp.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatSpinner;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
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
import io.github.froodyapp.ui.DialogEntryTypeSelection;
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

    @BindView(R.id.publish_entry__fragment__text_entry_type_header)
    TextView textEntryTypeHeader;

    @BindView(R.id.publish_entry__fragment__text_certification_header)
    TextView textCertificationHeader;

    @BindView(R.id.publish_entry__fragment__text_distribution_header)
    TextView textDistributionHeader;

    @BindView(R.id.publish_entry__fragment__text_contact_header)
    TextView textContactHeader;

    @BindView(R.id.publish_entry__fragment__text_description_header)
    TextView textDescriptionHeader;

    @BindView(R.id.publish_entry__fragment__text_location)
    TextView textLocation;

    @BindView(R.id.publish_entry__fragment__entry_type_image)
    ImageView imageEntryTypeImage;

    @BindView(R.id.publish_entry__fragment__entry_type_name)
    TextView textEntryTypeName;

    @BindView(R.id.publish_entry__fragment__spinner_certificate_selector)
    AppCompatSpinner spinnerCertification;

    @BindView(R.id.publish_entry__fragment__spinner_distribution_selector)
    AppCompatSpinner spinnerDistribution;

    @BindView(R.id.publish_entry__fragment__edit_description)
    EditText editDescription;

    @BindView(R.id.publish_entry__fragment__edit_contact)
    EditText editContact;

    @BindView(R.id.publish_entry__fragment__button_submit_entry)
    AppCompatButton buttonSubmitFroodyEntry;

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

        loadSpinnerDataSources();


        // Load some previously selected options from AppSettings
        spinnerCertification.setSelection(appSettings.getLastCertification());
        spinnerDistribution.setSelection(appSettings.getLastDistribution());
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

    // Load Data source for spinner
    private void loadSpinnerDataSources() {
        Context context = getContext();

        // Certification Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.certification_types, android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCertification.setAdapter(adapter);

        // Distribution Spinner
        adapter = ArrayAdapter.createFromResource(context, R.array.distribution_types, android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDistribution.setAdapter(adapter);
    }


    // Checks if form is valid ; True if entry can be submitted
    private boolean hasValidInput() {
        return editContact.getText().toString().length() > 0
                && editDescription.getText().toString().length() > 0
                && location != null
                && (froodyEntry.getEntryType() >= FroodyEntryFormatter.ENTRY_TYPE_MIN
                || froodyEntry.getEntryType() == FroodyEntryFormatter.ENTRY_TYPE_CUSTOM);
    }

    /**
     * Triggers recheck of user input
     */
    private void recheckUserInput() {
        boolean valid = hasValidInput();
        buttonSubmitFroodyEntry.setEnabled(valid);
        int red = Helpers.getColorFromRes(getContext(), R.color.very_red);
        int green = Helpers.getColorFromRes(getContext(), R.color.much_green);

        textDescriptionHeader.setTextColor(editDescription.getText().toString().isEmpty() ? red : green);
        textContactHeader.setTextColor(editContact.getText().toString().isEmpty() ? red : green);
        textEntryTypeHeader.setTextColor(froodyEntry.getEntryType() != FroodyEntryFormatter.ENTRY_TYPE_UNKNOWN ? green : red);
    }


    // User clicked the submit button
    @OnClick(R.id.publish_entry__fragment__button_submit_entry)
    public void onSubmitFroodyEntryButtonClicked(View view) {
        if (!hasValidInput() || !appSettings.hasFroodyUserId()) {
            return;
        }

        Context context = view.getContext();
        FroodyEntryFormatter entryFormatter = new FroodyEntryFormatter(context, froodyEntry);

        // Apply details to entry
        //froodyEntry.setEntryType(); // Done by callback
        entryFormatter.loadGeohashFromLocation(location.lat, location.lng, 9);
        //froodyEntry.setAddress(textLocation.getText().toString());    // done by EntryReverseGeocoder
        froodyEntry.setUserId(appSettings.getFroodyUser().getUserId());
        froodyEntry.setCertificationType(spinnerCertification.getSelectedItemPosition());
        froodyEntry.setDistributionType(spinnerDistribution.getSelectedItemPosition());
        froodyEntry.setContact(editContact.getText().toString());
        froodyEntry.setDescription(editDescription.getText().toString());

        // Save latest settings
        appSettings.setLastCertification(spinnerCertification.getSelectedItemPosition());
        appSettings.setLastContactInfo(editContact.getText().toString());
        appSettings.setLastDistribution(spinnerDistribution.getSelectedItemPosition());

        // Start publishing entry
        new EntryPublisher(getActivity(), froodyEntry, this).start();
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
    @OnClick(R.id.publish_entry__fragment__entry_type_selector)
    public void onSelectFroodyEntryTypeSelctorClicked(View view) {
        DialogEntryTypeSelection yourDialogFragment = DialogEntryTypeSelection.newInstance(this, false);
        yourDialogFragment.show(getFragmentManager(), DialogEntryTypeSelection.FRAGMENT_TAG);
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
        activity.setTitle(R.string.publish_entry);
        activity.navigationView.setCheckedItem(R.id.nav_publish_entry);
        requestLocationFromMainActivity();
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
        Context context = getContext();
        if (context == null) {
            return;
        }

        if (wasAdded) {
            // successfuly published
            froodyEntry.setEntryId(response.getEntryId());
            froodyEntry.setManagementCode(response.getManagementCode());
            froodyEntry.setCreationDate(response.getCreationDate());
            froodyEntry.setModificationDate(response.getCreationDate());
            BlockCache.getInstance().processEntryWithDetails(froodyEntry);
            new MyEntriesHelper(context).addToMyEntries(froodyEntry);

            CustomDialogs.showShareDialog(context, froodyEntry, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (which != -2 && getContext() != null) {
                        MyEntriesHelper.shareEntry(getContext(), froodyEntry);
                    }
                    if (publishedListener != null) {
                        publishedListener.onFroodyEntryPublished(froodyEntry);
                    }
                }
            });
        } else {
            // Couln't publish
            Toast.makeText(context, "Fehler: Konnte Eintrag nicht veröffentlichen", Toast.LENGTH_LONG).show();
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
        imageEntryTypeImage.setImageDrawable(formatter.getEntryTypeImage());
        textEntryTypeName.setText(formatter.getEntryTypeName());

        // Disable if not allowed
        spinnerCertification.setEnabled(formatter.isAllowedToCertify());
        spinnerDistribution.setEnabled(formatter.isAllowedToSell());

        if (!formatter.isAllowedToCertify()) {
            spinnerCertification.setSelection(0);
        }

        if (!formatter.isAllowedToSell()) {
            spinnerDistribution.setSelection(0);
        }


        // Add to history list
        if (entryType != FroodyEntryFormatter.ENTRY_TYPE_CUSTOM) {
            AppSettings settings = app.getAppSettings();
            ArrayList<Integer> history = settings.getLastEntryTypes();
            if (history.contains(entryType)) {
                history.remove(history.indexOf(entryType));
            }
            history.add(0, entryType);
            while (history.size() >= RecyclerEntryTypeAdapter.MAX_ENTRYTYPE_SUGGESTIONS_COUNT__BY_HISTORY) {
                history.remove(history.get(history.size() - 1));
            }
            settings.setLastEntryTypes(history);
        }
        textEntryTypeHeader.setTextColor(Helpers.getColorFromRes(getContext(), R.color.much_green));
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
