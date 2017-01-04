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

public class BotsheetEntrySingle extends BottomSheetDialogFragment implements EntryDetailsLoader.ExtendedInfoLoaderListener {
    //########################
    //## Static
    //########################

    /**
     * @param froodyEntry Froody Entry/Entries
     * @return
     */
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
        View contentView = View.inflate(getContext(), R.layout.botsheet__entry_single, null);
        sheet.setContentView(contentView);
        ButterKnife.bind(this, contentView);
        app = (App) getActivity().getApplication();
        appSettings = app.getAppSettings();
        myEntriesHelper = new MyEntriesHelper(getContext());

        // Set Coordinator Behaviour
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();
        if (behavior != null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }

        // Load froody util
        if (froodyEntry != null) {
            loadFroodyEntry();
        }
    }

    /**
     * Load Froody Entry/Entries to UI
     */
    private void loadFroodyEntry() {
        FroodyEntryFormatter froodyUtil = new FroodyEntryFormatter(getContext(), froodyEntry);

        if (!froodyUtil.hasExtendedInfoLoaded() && extendedInfoLoadTryCount < 3) {
            extendedInfoLoadTryCount++;
            new EntryDetailsLoader(getActivity(), froodyEntry, this, "BotSheetSingle").start();
        }

        // Set UI
        buttonDelete.setVisibility(myEntriesHelper.isMyEntry(froodyEntry.getEntryId()) ? View.VISIBLE : View.GONE);
        textAddress.setText(froodyUtil.getLocationInfo());
        textCertification.setText(froodyUtil.getCertification());
        textDistribution.setText(froodyUtil.getDistribution());
        textFroodyType.setText(froodyUtil.getEntryTypeName());
        textDescription.setText(froodyEntry.getDescription());
        textContact.setText(froodyEntry.getContact());
    }

    /**
     * Share button was pressed
     *
     * @param view View
     */
    @OnClick(R.id.botsheet__entry_single__button_share)
    public void onShareButtonClicked(View view) {
        MyEntriesHelper.shareEntry(getContext(), froodyEntry);
    }

    /**
     * Delete Button was pressed
     *
     * @param view button
     */
    @OnClick(R.id.botsheet__entry_single__button_delete)
    public void onDeleteButtonClicked(View view) {
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
    }

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
                        AppCast.FROODY_ENTRY_DELETED.send(app.getApplicationContext(), froodyEntry, result.getSuccess());
                        if (result.getSuccess()) {
                            myEntriesHelper.removeFromMyEntries(froodyEntry);
                            dismiss();
                        }
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

    /**
     * Set the Froody Entry/Entries
     *
     * @param froodyEntry Froody Entry/Entries
     */
    public void setFroodyEntry(FroodyEntryPlus froodyEntry) {
        this.froodyEntry = froodyEntry;
    }

    @Override
    public void onFroodyEntryExtendedInfoLoaded(FroodyEntryPlus entry) {
        if (getContext() != null && getActivity() != null) {
            loadFroodyEntry();
        }
    }
}
