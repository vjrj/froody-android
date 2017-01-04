package io.github.froodyapp.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import io.github.froodyapp.R;
import io.github.froodyapp.listener.EntryTypeSelectedListener;
import io.github.froodyapp.util.AppSettings;
import io.github.froodyapp.util.FroodyEntryFormatter;

public class DialogEntryTypeSelection extends DialogFragment implements EntryTypeSelectedListener, TabLayout.OnTabSelectedListener {
    //########################
    //## Static
    //########################
    public static final String FRAGMENT_TAG = "DialogEntryTypeSelection";

    public static DialogEntryTypeSelection newInstance(EntryTypeSelectedListener entryTypeSelectedListener, boolean showResetButton) {
        DialogEntryTypeSelection f = new DialogEntryTypeSelection();
        f.setShowResetButton(showResetButton);
        f.setEntryTypeSelectedListener(entryTypeSelectedListener);
        return f;
    }

    //########################
    //## Member
    //########################
    private RecyclerEntryTypeAdapter recyclerEntryTypeAdapterAll;
    private RecyclerEntryTypeAdapter recyclerEntryTypeAdapterSuggested;
    private TabLayout.Tab tabAll;
    private TabLayout.Tab tabSuggested;
    private boolean showResetButton;
    private EntryTypeSelectedListener entryTypeSelectedListener;

    @BindView(R.id.dialog__entry_type_selection__recycler)
    RecyclerView recyclerEntryType;

    @BindView(R.id.dialog__entry_type_selection__tab_layout)
    TabLayout tabLayout;

    @BindView(R.id.dialog__entry_type_selection__button_reset)
    Button buttonReset;

    //########################
    //## Methods
    //########################
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.dialog__entry_type_selection, container, false);
        ButterKnife.bind(this, root);
        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Context context = getContext();
        AppSettings appSettings = new AppSettings(context);
        Random random = new Random();

        // More UI
        tabLayout.addOnTabSelectedListener(this);
        tabSuggested = tabLayout.getTabAt(0);
        tabAll = tabLayout.getTabAt(1);
        buttonReset.setVisibility(showResetButton ? View.VISIBLE : View.GONE);

        //########
        //## Setup recycler view
        //########
        final String[] entryTypeResource = context.getResources().getStringArray(R.array.entry_type__names);

        // Setup suggested adapter
        ArrayList<Integer> list = appSettings.getLastEntryTypes();
        list.add(0, FroodyEntryFormatter.ENTRY_TYPE_CUSTOM);
        for (int tries = 0; tries < 20 && list.size() < RecyclerEntryTypeAdapter.MAX_ENTRYTYPE_SUGGESTIONS_COUNT__FILL_UP_TO; tries++) {
            int rnd = random.nextInt(entryTypeResource.length);
            if (rnd > 1 && !list.contains(rnd)) {
                list.add(rnd);
            }
        }
        recyclerEntryTypeAdapterSuggested = new RecyclerEntryTypeAdapter(list, this, context);

        // Setup all adapter
        list = new ArrayList<>();
        for (int i = FroodyEntryFormatter.ENTRY_TYPE_MIN; i < entryTypeResource.length; i++) {
            list.add(i);
        }
        Collections.sort(list, new Comparator<Integer>() {
            public int compare(final Integer o1, final Integer o2) {
                return entryTypeResource[o1].compareTo(entryTypeResource[o2]);
            }
        });
        list.add(0, FroodyEntryFormatter.ENTRY_TYPE_CUSTOM);
        recyclerEntryTypeAdapterAll = new RecyclerEntryTypeAdapter(list, this, context);

        recyclerEntryType.setAdapter(recyclerEntryTypeAdapterSuggested);
    }

    @Override
    public void onEntryTypeSelected(int entryType) {
        if (entryTypeSelectedListener != null) {
            entryTypeSelectedListener.onEntryTypeSelected(entryType);
        }
        dismiss();
    }

    @OnTextChanged(value = R.id.dialog__entry_type_selection__search, callback = OnTextChanged.Callback.TEXT_CHANGED)
    public void searchAdapter(CharSequence s, int start, int before, int count) {
        tabAll.select();
        recyclerEntryTypeAdapterAll.getFilter().filter(s.toString());
        recyclerEntryTypeAdapterSuggested.getFilter().filter(s.toString());
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        if (tab == tabSuggested) {
            recyclerEntryType.setAdapter(recyclerEntryTypeAdapterSuggested);
        } else {
            recyclerEntryType.setAdapter(recyclerEntryTypeAdapterAll);
        }
    }

    @OnClick(R.id.dialog__entry_type_selection__button_cancel)
    public void onCancelButtonClicked(View view) {
        dismiss();
    }

    @OnClick(R.id.dialog__entry_type_selection__button_reset)
    public void onResetButtonClicked(View view) {
        if (entryTypeSelectedListener != null) {
            entryTypeSelectedListener.onEntryTypeSelected(FroodyEntryFormatter.ENTRY_TYPE_ALL);
        }
        dismiss();
    }


    public void setShowResetButton(boolean showResetButton) {
        this.showResetButton = showResetButton;
    }

    public void setEntryTypeSelectedListener(EntryTypeSelectedListener entryTypeSelectedListener) {
        this.entryTypeSelectedListener = entryTypeSelectedListener;
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        onTabReselected(tab);
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
    }
}