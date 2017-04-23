package io.github.froodyapp.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v7.widget.AppCompatDrawableManager;

import java.util.HashSet;
import java.util.Set;

import io.github.froodyapp.R;
import io.github.froodyapp.api.model_.FroodyEntry;
import io.github.froodyapp.model.FroodyEntryPlus;

/**
 * Froody entry util (formatting, helpers)
 */
public class FroodyEntryFormatter extends FroodyEntryPlus {
    //########################
    //## Static
    //########################
    public static final int ENTRY_TYPE_ALL = -2;
    public static final int ENTRY_TYPE_CUSTOM = 0;
    public static final int ENTRY_TYPE_UNKNOWN = 1;
    public static final int ENTRY_TYPE_MIN = 2;

    // Constant in app version. Only set once. Reduces expensive res array calls
    private static int COUNT_ENTRY_TYPE_LOCAL = -1;
    private final static Set<Integer> typesAllowedNotToSell = new HashSet<>();
    private final static Set<Integer> typesAllowedNotToCertify = new HashSet<>();

    //########################
    //## Members
    //########################
    private Context context;

    //########################
    //## Methods
    //########################


    // Default constructor
    public FroodyEntryFormatter(Context context, FroodyEntryPlus froodyEntry) {
        super(froodyEntry);
        this.context = context.getApplicationContext();
    }

    // Constructor (empty data, use with caution!)
    public FroodyEntryFormatter(Context context) {
        super(new FroodyEntryPlus(new FroodyEntry()));
        this.context = context.getApplicationContext();
    }

    // Meant to be chained  ( changeEntryTo(entry).getEntryTypeName() )
    public FroodyEntryFormatter changeEntryTo(FroodyEntry froodyEntry) {
        this.entry = froodyEntry;
        //loadLocationFromGeohash();
        return this;
    }

    // Summarize an Froody Entry (more infos than toString()
    public String summarize() {
        String msg = getEntryTypeName() + "\n" +
                getLocationInfo() + "\n" +
                getDescription() + "\n" +
                getContact() + "\n";
        return msg.trim();
    }

    // Get certification of Froody Entry
    public String getCertification() {
        String[] d = context.getResources().getStringArray(R.array.certification_types);
        Integer cert = entry.getCertificationType();
        if (cert != null && cert < d.length && cert >= 0) {
            return d[cert];
        }
        return context.getString(R.string.unknown);
    }

    // Get froody type of Froody Entry
    public String getEntryTypeName() {
        String[] entryTypeNames = context.getResources().getStringArray(R.array.entry_type__names);
        return entryTypeNames[getEntryTypeResArrayIndex()];
    }


    private int getEntryTypeResArrayIndex() {
        if (COUNT_ENTRY_TYPE_LOCAL < 0) {
            String[] resources = context.getResources().getStringArray(R.array.entry_type__names);
            COUNT_ENTRY_TYPE_LOCAL = resources.length;
        }
        Integer entryType = entry.getEntryType();
        return entryType == null || entryType < 0 || entryType >= COUNT_ENTRY_TYPE_LOCAL
                ? ENTRY_TYPE_UNKNOWN : entryType;
    }

    //Get distribution type of entry
    public String getDistribution() {
        String[] d = context.getResources().getStringArray(R.array.distribution_types);
        Integer dist = entry.getDistributionType();
        if (dist != null && dist < d.length && dist >= 0) {
            return d[dist];
        }
        return context.getString(R.string.unknown);
    }

    public boolean isAllowedToSell() {
        if (typesAllowedNotToSell.isEmpty()) {
            for (int type : context.getResources().getIntArray(R.array.entry_type__distribution_types__not_allowed_sell)) {
                typesAllowedNotToSell.add(type);
            }
        }
        return !typesAllowedNotToSell.contains(getEntryType());
    }

    public boolean isAllowedToCertify() {
        if (typesAllowedNotToCertify.isEmpty()) {
            for (int type : context.getResources().getIntArray(R.array.entry_type__certification_types__not_allowed_certify)) {
                typesAllowedNotToCertify.add(type);
            }
        }
        return !typesAllowedNotToCertify.contains(getEntryType());
    }

    // Combined string of certification and distribution
    public String getCertificationAndDistributionInfo() {
        StringBuilder sb = new StringBuilder(getDistribution());
        Integer certType = entry.getCertificationType();
        if (certType != null && certType != 0) {
            sb.append(",");
            sb.append(getCertification());
        }
        return sb.toString();
    }

    public Drawable getEntryTypeImage() {
        TypedArray imgs = context.getResources().obtainTypedArray(R.array.entry_type__images);
        int resId = imgs.getResourceId(getEntryTypeResArrayIndex(), R.drawable.entry_type__special__unknown);
        imgs.recycle();

        return getBitmapFromDrawable(context, resId);
    }

    public @DrawableRes int getEntryTypeImageId(@DrawableRes int unknownId){
        TypedArray imgs = context.getResources().obtainTypedArray(R.array.entry_type__images);
        int resId = imgs.getResourceId(getEntryTypeResArrayIndex(), unknownId);
        imgs.recycle();
        return resId;
    }

    public static Drawable getBitmapFromDrawable(Context context, @DrawableRes int drawableId) {
        Drawable drawable = AppCompatDrawableManager.get().getDrawable(context, drawableId);
        if (drawable instanceof VectorDrawableCompat) {
        }
        return drawable;
    }

}
