package io.github.froodyapp.ui;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.froodyapp.R;

/**
 * View holder for an froody entry
 */
class UiFroodyItemViewHolder extends RecyclerView.ViewHolder {
    //########################
    //## UI Binding
    //########################
    @BindView(R.id.ui__froody_item__type_image)
    ImageView imageTypeImage;
    @BindView(R.id.ui__froody_item__address)
    TextView textAddress;
    @BindView(R.id.ui__froody_item__cert_and_dist)
    TextView textInfo;
    @BindView(R.id.ui__froody_item__type_name)
    TextView textEntryTypeName;
    @BindView(R.id.ui__froody_item__root)
    LinearLayout layoutRoot;
    @BindView(R.id.ui__froody_item__divider)
    View divider;

    //########################
    //## Methods
    //########################

    /**
     * Constructor
     *
     * @param row for row
     */
    UiFroodyItemViewHolder(View row) {
        super(row);
        ButterKnife.bind(this, row);
    }

    /**
     * Set the visiblity of the divider
     *
     * @param visible Ture if should be visible
     */
    public void setDividerVisible(boolean visible) {
        divider.setVisibility(visible ? View.VISIBLE : View.GONE);
    }
}
