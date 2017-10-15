package io.github.froodyapp.util;

import android.content.Context;
import android.content.Intent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import io.github.froodyapp.App;
import io.github.froodyapp.R;
import io.github.froodyapp.api.model_.FroodyEntry;
import io.github.froodyapp.model.FroodyEntryPlus;

/**
 * Methods for managing My FroodyEntries
 */
public class MyEntriesHelper {
    //#####################
    //## Members
    //#####################
    private Context context;

    //#####################
    //## Methods
    //#####################
    public MyEntriesHelper(Context context) {
        this.context = context.getApplicationContext();
    }

    public void addToMyEntries(FroodyEntryPlus entry) {
        List<FroodyEntryPlus> entries = getMyEntries();
        entries.add(new FroodyEntryPlus(entry.getContainedEntry()));
        setMyEntries(entries);
    }

    public void removeFromMyEntries(FroodyEntryPlus entry) {
        List<FroodyEntryPlus> entries = getMyEntries();
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).getEntryId().equals(entry.getEntryId())) {
                entries.remove(i);
                break;
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


    public String getMyEntriesExport() {
        long userId = AppSettings.get().getFroodyUserId();
        StringBuilder sb = new StringBuilder("START__FROODY_ENTRIES\n");

        // UserID:EntryId:Code\n
        for (FroodyEntryPlus entry : getMyEntries()) {
            sb.append(Long.toString(userId));
            sb.append(":");
            sb.append(Long.toString(entry.getEntryId()));
            sb.append(":");
            sb.append(Long.toString(entry.getManagementCode()));
            sb.append("\n");
        }

        sb.append("END__FROODY_ENTRIES\n");
        return sb.toString();
    }

    public int getMyEntriesCount() {
        return getMyEntries().size();
    }

    // Writes my entries to BlockCache
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
                cache.processEntryWithDetails(entry);
            }
        }
    }

    public static boolean shareEntry(Context context, FroodyEntry entry) {
        if (entry == null || entry.getEntryId() == null) {
            return false;
        }


        AppSettings appSettings = AppSettings.get();
        String subject = context.getString(R.string.share_my_entry_at_froody);
        String url = context.getString(R.string.share_link_url_with_entryid_param, entry.getEntryId());
        if (!context.getString(R.string.server_default).equals(appSettings.getFroodyServer())) {
            try {
                url = context.getString(R.string.share_link_url_with_entryid__and_server_param, entry.getEntryId(),
                        URLEncoder.encode(appSettings.getFroodyServer(), "UTF-8"));
            } catch (UnsupportedEncodingException ignored) {
            }
        }

        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT, subject);
        i.putExtra(Intent.EXTRA_TEXT, url);
        context.startActivity(Intent.createChooser(i, context.getString(R.string.share_chooser_share_entry)));
        return true;
    }
}
