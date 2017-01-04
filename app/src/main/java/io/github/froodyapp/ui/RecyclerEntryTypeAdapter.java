package io.github.froodyapp.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.List;

import io.github.froodyapp.R;
import io.github.froodyapp.listener.EntryTypeSelectedListener;
import io.github.froodyapp.util.FroodyEntryFormatter;
import io.github.froodyapp.util.Helpers;

/**
 * A recyclerview adapter for froody entries
 */
public class RecyclerEntryTypeAdapter extends RecyclerView.Adapter<UiFroodyItemViewHolder> implements Filterable {
    //########################
    //## Static
    //########################
    public static final int MAX_ENTRYTYPE_SUGGESTIONS_COUNT__BY_HISTORY = 5;
    public static final int MAX_ENTRYTYPE_SUGGESTIONS_COUNT__FILL_UP_TO = 10;

    //########################
    //## Members
    //########################
    private final EntryTypeSelectedListener selectedListener;
    private final List<Integer> adapterData;
    private final List<Integer> adapterDataFiltered;
    private final Context context;
    private EntryTypeFilter filter;

    //########################
    //## Methods
    //########################

    /**
     * Constructor
     *
     * @param adapterData      Entry list
     * @param selectedListener callback listener for selection/click
     * @param context          context
     */
    public RecyclerEntryTypeAdapter(List<Integer> adapterData, EntryTypeSelectedListener selectedListener, Context context) {
        this.adapterData = adapterData;
        this.adapterDataFiltered = new ArrayList<>(adapterData);
        this.context = context.getApplicationContext();
        this.selectedListener = selectedListener;
    }

    @Override
    public UiFroodyItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.ui__froody_item, parent, false);
        return new UiFroodyItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(UiFroodyItemViewHolder holder, int position) {
        holder.setDividerVisible(position != 0);
        holder.textAddress.setVisibility(View.GONE);
        holder.textInfo.setVisibility(View.GONE);
        holder.textEntryTypeName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19);
        holder.textEntryTypeName.setTypeface(null, Typeface.NORMAL);
        holder.divider.setBackgroundColor(Helpers.getColorFromRes(context, R.color.divider));


        final int entryType = adapterDataFiltered.get(position);
        final FroodyEntryFormatter entryFormatter = new FroodyEntryFormatter(context);
        entryFormatter.setEntryType(entryType);
        holder.textEntryTypeName.setText(entryFormatter.getEntryTypeName());
        holder.imageTypeImage.setImageDrawable(entryFormatter.getEntryTypeImage());

        // Forward click as callback to listener
        holder.layoutRoot.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (selectedListener != null) {
                    selectedListener.onEntryTypeSelected(entryType);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return adapterDataFiltered.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new EntryTypeFilter(this, adapterData, context);
        }
        return filter;
    }


    //########################
    //##
    //## EntryTypeFilter
    //##
    //########################
    private static class EntryTypeFilter extends Filter {
        private RecyclerEntryTypeAdapter adapter;
        private final List<Integer> originalList;
        private final List<Integer> filteredList;
        private final List<String> originalListStrings;

        private EntryTypeFilter(RecyclerEntryTypeAdapter adapter, List<Integer> adapterData, Context context) {
            super();
            this.adapter = adapter;
            originalList = new ArrayList<>(adapterData);
            filteredList = new ArrayList<>();

            // Get an formatter for the EntryType
            FroodyEntryFormatter formatter = new FroodyEntryFormatter(context);
            originalListStrings = new ArrayList<>();

            // Get localized string versions of entryType names
            for (int entryType : originalList) {
                formatter.setEntryType(entryType);
                originalListStrings.add(formatter.getEntryTypeName());
            }
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            filteredList.clear();
            final FilterResults results = new FilterResults();

            if (constraint.length() == 0) {
                filteredList.addAll(originalList);
            } else {
                final String filterPattern = constraint.toString().toLowerCase().trim();
                for (int i = 0; i < originalList.size(); i++) {
                    int entryTypeId = originalList.get(i);
                    String entryTypeName = originalListStrings.get(i);
                    if (entryTypeName.toLowerCase().contains(filterPattern) && entryTypeId >= FroodyEntryFormatter.ENTRY_TYPE_MIN) {
                        filteredList.add(entryTypeId);
                    }
                }
                filteredList.add(0, FroodyEntryFormatter.ENTRY_TYPE_CUSTOM);
            }
            results.values = filteredList;
            results.count = filteredList.size();
            return results;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void publishResults(CharSequence constraint, FilterResults results) {
            adapter.adapterDataFiltered.clear();
            adapter.adapterDataFiltered.addAll((ArrayList<Integer>) results.values);
            adapter.notifyDataSetChanged();
        }
    }
}