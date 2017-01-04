package io.github.froodyapp.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;

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

    /**
     * Constructor
     *
     * @param context     context
     * @param froodyEntry Froody Entry
     */
    public FroodyEntryFormatter(Context context, FroodyEntryPlus froodyEntry) {
        super(froodyEntry);
        this.context = context.getApplicationContext();
    }

    /**
     * Constructor (empty dataset, use with caution!)
     *
     * @param context context
     */
    public FroodyEntryFormatter(Context context) {
        super(new FroodyEntryPlus(new FroodyEntry()));
        this.context = context.getApplicationContext();
    }

    public FroodyEntryFormatter changeEntryTo(FroodyEntry froodyEntry) {
        // Meant to be chained  ( changeEntyTo(enty).getEntryTypeName() )
        this.entry = froodyEntry;
        //loadLocationFromGeohash();
        return this;
    }

    /**
     * Summarize an Froody Entry
     *
     * @return
     */
    public String summarize() {
        String msg = getEntryTypeName() + "\n" +
                getDescription() + "\n" +
                getContact() + "\n" +
                getLocationInfo() + "\n";
        return msg.trim();
    }

    /**
     * Get certification of Froody Entry
     *
     * @return
     */
    public String getCertification() {
        String[] d = context.getResources().getStringArray(R.array.certification_types);
        int i = 0;
        if (entry.getCertificationType() != null && entry.getCertificationType() < d.length) {
            i = entry.getCertificationType();
        }
        return d[i];
    }

    /**
     * Get froody type of Froody Entry
     *
     * @return
     */
    public String getEntryTypeName() {
        String[] entryTypeNames = context.getResources().getStringArray(R.array.entry_type__names);
        return entryTypeNames[getEntryTypeResArrayIndex()];
    }

    public int getEntryTypeResArrayIndex() {
        if (COUNT_ENTRY_TYPE_LOCAL < 0) {
            String[] resources = context.getResources().getStringArray(R.array.entry_type__names);
            COUNT_ENTRY_TYPE_LOCAL = resources.length;
        }
        Integer entryType = entry.getEntryType();
        return entryType == null || entryType < 0 || entryType >= COUNT_ENTRY_TYPE_LOCAL
                ? ENTRY_TYPE_UNKNOWN : entryType;
    }

    /**
     * Get distribution type of entry
     *
     * @return
     */
    public String getDistribution() {
        String[] d = context.getResources().getStringArray(R.array.distribution_types);
        int i = 0;
        if (entry.getDistributionType() != null && entry.getDistributionType() < d.length) {
            i = entry.getDistributionType();
        }
        return d[i];
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
        int a = getEntryTypeResArrayIndex();
        Drawable drawable = imgs.getDrawable(getEntryTypeResArrayIndex());
        imgs.recycle();
        return drawable;
    }
}
