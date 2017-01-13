package io.github.froodyapp.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.froodyapp.App;
import io.github.froodyapp.R;
import io.github.froodyapp.api.api.EntryApi;
import io.github.froodyapp.api.invoker.ApiCallback;
import io.github.froodyapp.api.invoker.ApiException;
import io.github.froodyapp.api.model_.ResponseOk;
import io.github.froodyapp.model.FroodyEntryPlus;
import io.github.froodyapp.service.EntryDetailsLoader;
import io.github.froodyapp.ui.CustomDialogs;
import io.github.froodyapp.util.AppCast;
import io.github.froodyapp.util.AppSettings;
import io.github.froodyapp.util.FroodyEntryFormatter;
import io.github.froodyapp.util.MyEntriesHelper;

public class BotsheetEntrySingle extends BottomSheetDialogFragment implements EntryDetailsLoader.EntryDetailsLoaderListener {
    //########################
    //## Static
    //########################


    // Create new instance with one entry
    public static BotsheetEntrySingle newInstance(FroodyEntryPlus froodyEntry) {
        BotsheetEntrySingle f = new BotsheetEntrySingle();
        f.setFroodyEntry(froodyEntry);
        return f;
    }

    //########################
    //## UI Binding
    //########################
    @BindView(R.id.botsheet__entry_single__text_address)
    TextView textAddress;
    @BindView(R.id.botsheet__entry_single__text_certification)
    TextView textCertification;
    @BindView(R.id.botsheet__entry_single__text_contact)
    TextView textContact;
    @BindView(R.id.botsheet__entry_single__text_description)
    TextView textDescription;
    @BindView(R.id.botsheet__entry_single__text_distribution)
    TextView textDistribution;
    @BindView(R.id.botsheet__entry_single__text_froodytype)
    TextView textFroodyType;
    @BindView(R.id.botsheet__entry_single__button_delete)
    FloatingActionButton buttonDelete;
    @BindView(R.id.froody_botsheet_single__layout_root)
    RelativeLayout layoutRoot;

    //########################
    //## Members
    //########################
    private MyEntriesHelper myEntriesHelper;
    private FroodyEntryPlus froodyEntry;
    private App app;
    private AppSettings appSettings;
    private int extendedInfoLoadTryCount = 0;
    private final BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss();
            }
        }

        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        }
    };

    @Override
    public void setupDialog(Dialog sheet, int style) {
        super.setupDialog(sheet, style);
        View root = View.inflate(getContext(), R.layout.botsheet__entry_single, null);
        sheet.setContentView(root);
        ButterKnife.bind(this, root);
        app = (App) getActivity().getApplication();
        appSettings = app.getAppSettings();
        myEntriesHelper = new MyEntriesHelper(getContext());

        // Set Coordinator Behaviour
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) ((View) root.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = layoutParams.getBehavior();
        if (behavior != null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }

        // Load froody entry
        if (froodyEntry != null) {
            loadFroodyEntry();
        }
    }

    // Load Froody Entry/Entries to UI
    private void loadFroodyEntry() {
        FroodyEntryFormatter entryFormatter = new FroodyEntryFormatter(getContext(), froodyEntry);

        if (!entryFormatter.hasExtendedInfoLoaded() && extendedInfoLoadTryCount < 3) {
            extendedInfoLoadTryCount++;
            new EntryDetailsLoader(getActivity(), froodyEntry, this, "BotSheetSingle").start();
        }

        // Apply to UI
        buttonDelete.setVisibility(myEntriesHelper.isMyEntry(froodyEntry.getEntryId()) ? View.VISIBLE : View.GONE);
        textAddress.setText(entryFormatter.getLocationInfo());
        textCertification.setText(entryFormatter.getCertification());
        textDistribution.setText(entryFormatter.getDistribution());
        textFroodyType.setText(entryFormatter.getEntryTypeName());
        textDescription.setText(froodyEntry.getDescription());
        textContact.setText(froodyEntry.getContact());
    }

    // Delete or Share button was pressed
    @OnClick({R.id.botsheet__entry_single__button_delete, R.id.botsheet__entry_single__button_share})
    public void onEntryButtonClicked(View view) {
        switch (view.getId()) {
            // Share entry
            case R.id.botsheet__entry_single__button_share: {
                MyEntriesHelper.shareEntry(view.getContext(), froodyEntry);
                break;
            }

            // Delete entry
            case R.id.botsheet__entry_single__button_delete: {
                if (myEntriesHelper.isMyEntry(froodyEntry.getEntryId())) {
                    dismiss();
                    CustomDialogs.showDeleteDialog(getContext(), froodyEntry, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dismiss();
                            myEntriesHelper.retrieveMyEntryDetails(froodyEntry);
                            deleteEntryViaApi();
                        }
                    });
                }
                break;
            }
        }
    }

    // Make an call to API to delete an Entry
    private void deleteEntryViaApi() {
        final EntryApi entryApi = new EntryApi();
        try {
            entryApi.entryDeleteGetAsync(froodyEntry.getUserId(), froodyEntry.getManagementCode(), froodyEntry.getEntryId(), new ApiCallback<ResponseOk>() {
                @Override
                public void onFailure(ApiException e, int statusCode, Map<String, List<String>> responseHeaders) {
                    AppCast.FROODY_ENTRY_DELETED.send(getContext(), froodyEntry, false);
                }

                @Override
                public void onSuccess(ResponseOk result, int statusCode, Map<String, List<String>> responseHeaders) {
                    if (result != null) {
                        if (result.getSuccess()) {
                            myEntriesHelper.removeFromMyEntries(froodyEntry);
                            dismiss();
                        }
                        AppCast.FROODY_ENTRY_DELETED.send(app.getApplicationContext(), froodyEntry, result.getSuccess());
                    }
                }

                public void onUploadProgress(long bytesWritten, long contentLength, boolean done) {
                }

                public void onDownloadProgress(long bytesRead, long contentLength, boolean done) {
                }
            });
        } catch (ApiException e) {
            App.log(getClass(), "ERROR: Cannot delete entry." + e.getMessage());
        }
    }

    @Override
    public void onFroodyEntryDetailsLoaded(FroodyEntryPlus entry) {
        if (getContext() != null && getActivity() != null) {
            loadFroodyEntry();
        }
    }

    //########################
    //## Getter & Setter
    //########################
    public void setFroodyEntry(FroodyEntryPlus froodyEntry) {
        this.froodyEntry = froodyEntry;
    }

}
