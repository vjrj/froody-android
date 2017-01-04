package io.github.froodyapp.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import io.github.froodyapp.R;
import io.github.froodyapp.listener.FroodyEntrySelectedListener;
import io.github.froodyapp.model.FroodyEntryPlus;
import io.github.froodyapp.util.FroodyEntryFormatter;

/**
 * A recyclerview adapter for froody entries
 */
public class RecyclerEntryAdapter extends RecyclerView.Adapter<UiFroodyItemViewHolder> {
    //########################
    //## Members
    //########################
    private final FroodyEntrySelectedListener selectedListener;
    private List<FroodyEntryPlus> data;
    private Context context;

    //########################
    //## Methods
    //########################

    /**
     * Constructor
     *
     * @param list             Entry list
     * @param selectedListener callback listener for selection/click
     * @param context          context
     */
    public RecyclerEntryAdapter(List<FroodyEntryPlus> list, FroodyEntrySelectedListener selectedListener, Context context) {
        this.data = list;
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
        final FroodyEntryPlus entry = data.get(position);
        FroodyEntryFormatter entryFormatter = new FroodyEntryFormatter(context, entry);
        holder.setDividerVisible(position != 0);
        holder.textEntryTypeName.setText(entryFormatter.getEntryTypeName());
        holder.imageTypeImage.setImageDrawable(entryFormatter.getEntryTypeImage());
        holder.textInfo.setText(entryFormatter.getCertificationAndDistributionInfo());
        holder.textAddress.setVisibility(entryFormatter.hasResolvedAddress() ? View.VISIBLE : View.GONE);
        if (entryFormatter.hasResolvedAddress()) {
            holder.textAddress.setText(entryFormatter.getLocationInfo());
        }
        holder.layoutRoot.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                selectedListener.onFroodyEntrySelected(entry);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}