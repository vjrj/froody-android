package io.github.froodyapp.model;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.Serializable;

import io.github.froodyapp.api.model_.BlockInfo;
import io.github.froodyapp.util.Helpers;

/**
 * BlockInfo with more details
 */
public class BlockInfoPlus extends BlockInfo implements Serializable {
    //########################
    //## Member
    //########################
    private BlockInfo blockInfo;
    private BlockInfo previousBlockInfo;

    //########################
    //## Methods
    //########################
    public BlockInfoPlus(String geohash) {
        blockInfo = new BlockInfo();
        setGeohash(geohash);
        setHasBlockBeenModified(true);
        setModificationDateToThreeWeeksAgo();
    }

    public BlockInfoPlus(String geohash, DateTime modificationDate) {
        blockInfo = new BlockInfo();
        setGeohash(geohash);
        setHasBlockBeenModified(true);
        setModificationDate(modificationDate);
    }

    public BlockInfoPlus(BlockInfo blockInfo) {
        this.blockInfo = blockInfo;
    }


    private void setModificationDateToThreeWeeksAgo() {
        setModificationDate(Helpers.getNow().minusWeeks(3));
    }

    public void setModificationDateToNow() {
        setModificationDate(Helpers.getNow());
    }

    public void setNewerBlockInfo(BlockInfo newerBlockInfo) {
        previousBlockInfo = blockInfo;
        blockInfo = newerBlockInfo;
    }

    public DateTime getPreviousModificationDate() {
        if (previousBlockInfo != null) {
            return previousBlockInfo.getModificationDate();
        }
        return new DateTime(DateTimeZone.UTC).minusWeeks(3);
    }

    //#################
    //## Overrides
    //#################
    @Override
    public boolean equals(Object o) {
        return blockInfo.equals(o);
    }

    @Override
    public int hashCode() {
        return blockInfo.hashCode();
    }

    @Override
    public String toString() {
        return blockInfo.toString();
    }

    @Override
    public BlockInfo geohash(String geohash) {
        return blockInfo.geohash(geohash);
    }

    @Override
    public String getGeohash() {
        return blockInfo.getGeohash();
    }

    @Override
    public void setGeohash(String geohash) {
        blockInfo.setGeohash(geohash);
    }

    @Override
    public BlockInfo modificationDate(DateTime modificationDate) {
        return blockInfo.modificationDate(modificationDate);
    }

    @Override
    public DateTime getModificationDate() {
        return blockInfo.getModificationDate();
    }

    @Override
    public void setModificationDate(DateTime modificationDate) {
        blockInfo.setModificationDate(modificationDate);
    }

    @Override
    public BlockInfo hasBlockBeenModified(Boolean hasBlockBeenModified) {
        return blockInfo.hasBlockBeenModified(hasBlockBeenModified);
    }

    @Override
    public Boolean getHasBlockBeenModified() {
        return blockInfo.getHasBlockBeenModified();
    }

    @Override
    public void setHasBlockBeenModified(Boolean hasBlockBeenModified) {
        blockInfo.setHasBlockBeenModified(hasBlockBeenModified);
    }
}
