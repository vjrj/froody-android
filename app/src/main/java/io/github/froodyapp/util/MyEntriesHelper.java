package io.github.froodyapp.util;

import android.content.Context;
import android.content.Intent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import io.github.froodyapp.App;
import io.github.froodyapp.R;
import io.github.froodyapp.api.model_.FroodyEntry;
import io.github.froodyapp.model.FroodyEntryPlus;

public class MyEntriesHelper {
    //#####################
    //## Members
    //#####################

    private Context context;

    //#####################
    //## Methods
    //#####################
    public MyEntriesHelper(Context context) {
        this.context = context;
    }

    public void addToMyEntries(FroodyEntryPlus entry) {
        List<FroodyEntryPlus> entries = getMyEntries();
        entries.add(new FroodyEntryPlus(entry.getContainedEntry()));
        setMyEntries(entries);
    }

    public void removeFromMyEntries(FroodyEntryPlus entry) {
        List<FroodyEntryPlus> entries = getMyEntries();
        for (FroodyEntryPlus my : entries) {
            if (my.getEntryId().equals(entry.getEntryId())) {
                entries.remove(my);
            }
        }
        setMyEntries(entries);
    }

    public boolean deleteMyEntries() {
        try {
            return getFilepathOfEntries().delete();
        } catch (Exception ignored) {
            return false;
        }
    }

    public boolean isMyEntry(long entryId) {
        for (FroodyEntryPlus cur : getMyEntries()) {
            if (cur.getEntryId().equals(entryId)) {
                return true;
            }
        }
        return false;
    }

    public boolean retrieveMyEntryDetails(FroodyEntryPlus entry) {
        for (FroodyEntryPlus cur : getMyEntries()) {
            if (cur.getEntryId().equals(entry.getEntryId())) {
                entry.setManagementCode(cur.getManagementCode());
                entry.setUserId(cur.getUserId());
                return true;
            }
        }
        return false;
    }

    private File getFilepathOfEntries() {
        return new File(context.getFilesDir(), "myEntries.dat");
    }

    public void setMyEntries(List<FroodyEntryPlus> entries) {
        try {
            File file = getFilepathOfEntries();
            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file));
            outputStream.writeObject(entries);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            App.log(getClass(), "Error: Cannot save MyEntries to data---" + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public List<FroodyEntryPlus> getMyEntries() {
        try {
            File file = getFilepathOfEntries();
            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(file));
            List<FroodyEntryPlus> dat = (List<FroodyEntryPlus>) inputStream.readObject();
            inputStream.close();
            return dat;
        } catch (Exception e) {
            App.log(getClass(), "Error: Cannot load MyEntries from cache---" + e.getMessage());
            return new ArrayList<>();
        }
    }

    public void processMyEntriesToBlockCache() {
        BlockCache cache = BlockCache.getInstance();
        for (FroodyEntryPlus entry : getMyEntries()) {
            FroodyEntryPlus inCache = cache.tryGetEntryByIdFromCache(entry);
            if (inCache != null) {
                inCache.setEntryId(entry.getEntryId());
                inCache.setUserId(entry.getUserId());
                inCache.setManagementCode(entry.getManagementCode());
                inCache.setDescription(entry.getDescription());
                inCache.setAddress(entry.getAddress());
                inCache.setContact(entry.getContact());
            } else {
                cache.processExtendedEntry(entry);
            }
        }
    }

    public static boolean shareEntry(Context context, FroodyEntry entry) {
        if (entry == null || entry.getEntryId() == null) {
            return false;
        }

        String subject = context.getString(R.string.share_my_entry_at_froody);
        String url = context.getString(R.string.share_link_url_with_entryid_param, entry.getEntryId());

        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT, subject);
        i.putExtra(Intent.EXTRA_TEXT, url);
        context.startActivity(Intent.createChooser(i, context.getString(R.string.share_chooser_share_entry)));

        return true;
    }
}
