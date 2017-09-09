package io.github.froodyapp.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import io.github.froodyapp.R;
import io.github.froodyapp.util.ContextUtils;
import io.github.froodyapp.util.MyEntriesHelper;

@SuppressWarnings({"unused", "WeakerAccess"})
public class MyEntryCountPreference extends Preference {
    protected ImageView icon;
    protected int currentValue;
    protected Bitmap bitmap;

    public MyEntryCountPreference(Context context) {
        super(context);
    }

    public MyEntryCountPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyEntryCountPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        icon = (ImageView) view.findViewById(android.R.id.icon);
        reloadPreference();
    }

    public void reloadPreference() {
        int newValue = new MyEntriesHelper(getContext()).getMyEntriesCount();
        if (bitmap == null || currentValue != newValue) {
            currentValue = newValue;
            bitmap = ContextUtils.get().drawTextToBitmap(R.drawable.empty_64dp, String.valueOf(currentValue), 32);
        }
        icon.setImageBitmap(bitmap);
    }

    @Override
    protected void onPrepareForRemoval() {
        super.onPrepareForRemoval();
        if (bitmap != null) {
            bitmap.recycle();
        }
        bitmap = null;
    }
}
