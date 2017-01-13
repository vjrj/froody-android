package io.github.froodyapp.activity;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.froodyapp.R;
import io.github.froodyapp.listener.FroodyEntrySelectedListener;
import io.github.froodyapp.model.FroodyEntryPlus;
import io.github.froodyapp.ui.RecyclerEntryAdapter;
import io.github.froodyapp.util.AppCast;
import io.github.froodyapp.util.MyEntriesHelper;

/**
 * Froody Bottom Sheet (Multi)
 */
public class BotsheetEntryMulti extends BottomSheetDialogFragment implements FroodyEntrySelectedListener {
    //########################
    //## Static
    //########################
    public static final String FRAGMENT_TAG = "BotsheetEntryMulti";

    // Create new instance with list of entries
    public static BotsheetEntryMulti newInstance(List<FroodyEntryPlus> froodyEntries) {
        BotsheetEntryMulti f = new BotsheetEntryMulti();
        f.setFroodyEntries(froodyEntries);
        return f;
    }

    //Create new instance with entries and string resource id to set the header text
    public static BotsheetEntryMulti newInstance(List<FroodyEntryPlus> froodyEntries, int stringId) {
        BotsheetEntryMulti f = newInstance(froodyEntries);
        f.headerStringId = stringId;
        return f;
    }

    //########################
    //## Members
    //########################
    @BindView(R.id.botsheet__entry_multi__recyclerview)
    RecyclerView recyclerList;
    @BindView(R.id.botsheet__entry_multi__text_header)
    TextView textHeader;
    private RecyclerEntryAdapter recyclerAdapter;

    private List<FroodyEntryPlus> froodyEntries;
    private FroodyEntrySelectedListener froodyEntrySelectedListener;
    public int headerStringId = R.string.select_froody;
    private final BottomSheetBehavior.BottomSheetCallback bottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {
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
        View root = View.inflate(getContext(), R.layout.botsheet__entry_multi, null);
        sheet.setContentView(root);
        ButterKnife.bind(this, root);

        // Set Coordinator Behaviour
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) root.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();
        if (behavior != null && behavior instanceof BottomSheetBehavior) {
            BottomSheetBehavior bottomSheetBehavior = (BottomSheetBehavior) behavior;
            bottomSheetBehavior.setBottomSheetCallback(bottomSheetBehaviorCallback);
        }

        // Load entries
        if (froodyEntries != null) {
            textHeader.setText(headerStringId);
            recyclerAdapter = new RecyclerEntryAdapter(froodyEntries, this, getContext().getApplicationContext());
            recyclerList.setAdapter(recyclerAdapter);
            recyclerList.setLayoutManager(new LinearLayoutManager(getContext()));
        }
    }

    //Share button was pressed
    @OnClick(R.id.botsheet__entry_multi__btn_share)
    public void onShareButtonClicked(View view) {
        MyEntriesHelper.shareEntry(view.getContext(), froodyEntries.get(0));
    }

    @Override
    public void onDetach() {
        super.onDetach();
        froodyEntrySelectedListener = null;
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(localBroadcastReceiver);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FroodyEntrySelectedListener) {
            froodyEntrySelectedListener = (FroodyEntrySelectedListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement FroodyEntrySelectedListener");
        }
        LocalBroadcastManager.getInstance(context).registerReceiver(localBroadcastReceiver, AppCast.getLocalBroadcastFilter());
    }

    @Override
    public void onFroodyEntrySelected(FroodyEntryPlus entry) {
        if (froodyEntrySelectedListener != null) {
            froodyEntrySelectedListener.onFroodyEntrySelected(entry);
        }
    }

    @SuppressWarnings("unchecked")
    private final BroadcastReceiver localBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case AppCast.FROODY_ENTRY_DELETED.ACTION: {
                    FroodyEntryPlus entry = AppCast.getEntryFromIntent(intent);
                    if (intent.getBooleanExtra(AppCast.FROODY_ENTRY_DELETED.EXTRA_WAS_DELETED, false)) {
                        for (int i = 0; i < froodyEntries.size(); i++) {
                            if (froodyEntries.get(i).getEntryId().equals(entry.getEntryId())) {
                                froodyEntries.remove(i);
                                recyclerAdapter.notifyDataSetChanged();
                                if (froodyEntries.isEmpty()) {
                                    dismiss();
                                }
                            }
                        }
                    }
                    break;
                }

                case AppCast.FROODY_ENTRY_DETAILS_LOADED.ACTION: {
                    FroodyEntryPlus entry = AppCast.getEntryFromIntent(intent);
                    for (int i = 0; i < froodyEntries.size(); i++) {
                        if (froodyEntries.get(i).getEntryId().equals(entry.getEntryId())) {
                            froodyEntries.remove(i);
                            froodyEntries.add(i, entry);
                            recyclerAdapter.notifyDataSetChanged();
                        }
                    }
                    break;
                }
            }
        }
    };

    //########################
    //## Getter & Setter
    //########################
    public void setFroodyEntries(List<FroodyEntryPlus> froodyEntries) {
        this.froodyEntries = froodyEntries;
    }
}
