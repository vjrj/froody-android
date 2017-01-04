package io.github.froodyapp.model;

import android.text.TextUtils;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.Locale;

import ch.hsr.geohash.GeoHash;
import ch.hsr.geohash.WGS84Point;
import io.github.froodyapp.api.model_.FroodyEntry;

@SuppressWarnings("WeakerAccess")
public class FroodyEntryPlus extends FroodyEntry implements Serializable {
    //########################
    //## Member
    //########################
    protected FroodyEntry entry;
    protected Double longitude;
    protected Double latitude;

    //########################
    //## Methods
    //########################
    public FroodyEntryPlus(FroodyEntry entry) {
        this.entry = entry;
        loadLocationFromGeohash();
    }


    public boolean hasResolvedAddress() {
        return !TextUtils.isEmpty(entry.getAddress());
    }


    public boolean hasExtendedInfoLoaded() {
        return !TextUtils.isEmpty(entry.getContact()) && !TextUtils.isEmpty(entry.getDescription());
    }

    public boolean hasGeohash() {
        return !TextUtils.isEmpty(entry.getGeohash());
    }

    public void loadGeohashFromLocation(double latitude, double longitude, int precision) {
        GeoHash gh = GeoHash.withCharacterPrecision(latitude, longitude, precision);
        entry.setGeohash(gh.toBase32());

        // param will differ slightly from Geohash center point
        WGS84Point point = gh.getPoint();
        this.latitude = point.getLatitude();
        this.longitude = point.getLongitude();
    }

    protected boolean loadLocationFromGeohash() {
        if (hasGeohash()) {
            WGS84Point pt = GeoHash.fromGeohashString(entry.getGeohash()).getPoint();
            longitude = pt.getLongitude();
            latitude = pt.getLatitude();
            return true;
        }
        longitude = null;
        latitude = null;
        return false;
    }

    public String getGeohashWithPrecision(int precision) {
        String geohash = getGeohash();
        if (!TextUtils.isEmpty(geohash) && precision >= 0 && precision < geohash.length()) {
            return geohash.substring(0, precision);
        }
        return null;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public boolean canEntryBeRemovedFromCache() {
        if (getWasDeleted() != null && getWasDeleted()) {
            return true;
        }
        return getCreationDate() != null && getCreationDate().plusWeeks(3).isBeforeNow();
    }

    /**
     * Get a managementCode representation (for appsettings)
     */
    public String getManagementString() {
        return entry.getEntryId() + ";" + entry.getManagementCode() + ";" + System.currentTimeMillis();
    }

    /**
     * Gets the adress description (best available) of Froody Entry
     */
    public String getLocationInfo() {
        if (hasResolvedAddress()) {
            return entry.getAddress();
        } else {
            if (getLatitude() != null && getLongitude() != null) {
                return String.format(Locale.getDefault(), "(%.5f ; %.5f)", getLongitude(), getLatitude());
            }
        }
        return "";
    }

    //
    // Additional NPE checks
    //
    @Override
    public String getDescription() {
        return TextUtils.isEmpty(entry.getDescription()) ? "" : entry.getDescription();
    }

    @Override
    public String getContact() {
        return TextUtils.isEmpty(entry.getContact()) ? "" : entry.getContact();
    }

    @Override
    public String getAddress() {
        return TextUtils.isEmpty(entry.getAddress()) ? "" : entry.getAddress();
    }

    public FroodyEntry getContainedEntry() {
        return entry;
    }

    public void setContainedEntry(FroodyEntry entry) {
        this.entry = entry;
    }

    //########################
    //## Overrides / forwards to base
    //########################
    @Override
    public String toString() {
        return entry.toString();
    }

    @Override
    public boolean equals(Object o) {
        return entry.equals(o);
    }

    @Override
    public int hashCode() {
        return entry.hashCode();
    }

    @Override
    public FroodyEntry entryId(Long entryId) {
        return entry.entryId(entryId);
    }

    @Override
    public Long getEntryId() {
        return entry.getEntryId();
    }

    @Override
    public void setEntryId(Long entryId) {
        entry.setEntryId(entryId);
    }

    @Override
    public FroodyEntry userId(Long userId) {
        return entry.userId(userId);
    }

    @Override
    public Long getUserId() {
        return entry.getUserId();
    }

    @Override
    public void setUserId(Long userId) {
        entry.setUserId(userId);
    }

    @Override
    public FroodyEntry geohash(String geohash) {
        return entry.geohash(geohash);
    }

    @Override
    public String getGeohash() {
        return entry.getGeohash();
    }

    @Override
    public void setGeohash(String geohash) {
        entry.setGeohash(geohash);
    }

    @Override
    public FroodyEntry creationDate(DateTime creationDate) {
        return entry.creationDate(creationDate);
    }

    @Override
    public DateTime getCreationDate() {
        return entry.getCreationDate();
    }

    @Override
    public void setCreationDate(DateTime creationDate) {
        entry.setCreationDate(creationDate);
    }

    @Override
    public FroodyEntry modificationDate(DateTime modificationDate) {
        return entry.modificationDate(modificationDate);
    }

    @Override
    public DateTime getModificationDate() {
        return entry.getModificationDate();
    }

    @Override
    public void setModificationDate(DateTime modificationDate) {
        entry.setModificationDate(modificationDate);
    }

    @Override
    public FroodyEntry entryType(Integer entryType) {
        return entry.entryType(entryType);
    }

    @Override
    public Integer getEntryType() {
        return entry.getEntryType();
    }

    @Override
    public void setEntryType(Integer entryType) {
        entry.setEntryType(entryType);
    }

    @Override
    public FroodyEntry certificationType(Integer certificationType) {
        return entry.certificationType(certificationType);
    }

    @Override
    public Integer getCertificationType() {
        return entry.getCertificationType();
    }

    @Override
    public void setCertificationType(Integer certificationType) {
        entry.setCertificationType(certificationType);
    }

    @Override
    public FroodyEntry distributionType(Integer distributionType) {
        return entry.distributionType(distributionType);
    }

    @Override
    public Integer getDistributionType() {
        return entry.getDistributionType();
    }

    @Override
    public void setDistributionType(Integer distributionType) {
        entry.setDistributionType(distributionType);
    }

    @Override
    public FroodyEntry description(String description) {
        return entry.description(description);
    }

    @Override
    public void setDescription(String description) {
        entry.setDescription(description);
    }

    @Override
    public FroodyEntry contact(String contact) {
        return entry.contact(contact);
    }

    @Override
    public void setContact(String contact) {
        entry.setContact(contact);
    }

    @Override
    public FroodyEntry address(String address) {
        return entry.address(address);
    }

    @Override
    public void setAddress(String address) {
        entry.setAddress(address);
    }

    @Override
    public FroodyEntry wasDeleted(Boolean wasDeleted) {
        return entry.wasDeleted(wasDeleted);
    }

    @Override
    public Boolean getWasDeleted() {
        return entry.getWasDeleted();
    }

    @Override
    public void setWasDeleted(Boolean wasDeleted) {
        entry.setWasDeleted(wasDeleted);
    }

    @Override
    public FroodyEntry managementCode(Integer managementCode) {
        return entry.managementCode(managementCode);
    }

    @Override
    public Integer getManagementCode() {
        return entry.getManagementCode();
    }

    @Override
    public void setManagementCode(Integer managementCode) {
        entry.setManagementCode(managementCode);
    }
}
