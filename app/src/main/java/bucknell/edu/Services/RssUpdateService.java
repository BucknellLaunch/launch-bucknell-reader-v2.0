package bucknell.edu.Services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;

import bucknell.edu.Data.RssItem;
import bucknell.edu.Data.RssResource;
import bucknell.edu.Interfaces.RssListener;
import bucknell.edu.bucknellreader.R;
import bucknell.edu.database.RssSQLiteDataSource;
import bucknell.edu.sync.RssJsonAsyncTask;

public class RssUpdateService extends Service implements RssListener{
    private RssSQLiteDataSource rssSQLiteDataSource;
    private CopyOnWriteArrayList<RssItem> rssItems;
    private ArrayList<RssResource> rssResources;
    private final IBinder binder = new RssUpdateBinder();
    private RssListener rssListener;


    public class RssUpdateBinder extends Binder {
        public RssUpdateService getService() {
            return RssUpdateService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void setRssListener(RssListener listener) {
        this.rssListener = listener;
    }


    public void loadRssResources() {
        rssResources = new ArrayList<RssResource>();
        String[] rssStringResources = getResources().getStringArray(R.array.rss_sources);
        for (String rssStringResource: rssStringResources) {
            String[] splitResult = rssStringResource.split("\\|", 3);
            RssResource rssResource = new RssResource(splitResult[0], splitResult[1], splitResult[2]);
            rssResources.add(rssResource);
        }
    }

    public void fetchRssItemsFromResources() {
        for (int i = 0; i < rssResources.size(); i++) {
            RssResource resource = rssResources.get(i);
            RssJsonAsyncTask rssJsonAsyncTask = new RssJsonAsyncTask(resource, this);
            rssJsonAsyncTask.execute();
        }
    }

    public boolean isDatabaseEmpty() {
        if (rssSQLiteDataSource == null)
            return true;
        return rssSQLiteDataSource.isDatabaseEmpty();
    }

    public CopyOnWriteArrayList<RssItem> fetchRssItemsFromDatabase() {
        this.rssItems = rssSQLiteDataSource.getAllRssItems();
        return this.rssItems;
    }

    private void updateLatestRssItemTime() {
        if (rssItems==null || rssItems.isEmpty())
            return;
        // get the first Rss Item
        RssItem rssItem = rssItems.get(0);
        long time = rssItem.getDateInLong();
        SharedPreferences prefs = getApplicationContext().getSharedPreferences(getString(R.string.shared_preferences_file_name), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(getString(R.string.shared_preferences_latest_rss_item_time), time);
        editor.apply();
    }

    private void updateLastUpdateTime() {
        Date date = Calendar.getInstance().getTime();
        long time = date.getTime();
        SharedPreferences prefs = getApplicationContext().getSharedPreferences(getString(R.string.shared_preferences_file_name), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(getString(R.string.shared_preferences_last_update_time), time);
        editor.apply();
    }

    private boolean hasNewRssItems () {
        return false;
    }

    private void sendPushNotifications () {

    }

    private void checkAndSendPushNotifications() {
        if (hasNewRssItems()) {
            sendPushNotifications();
        }
    }

    @Override
    public void onCreate() {
        rssSQLiteDataSource = new RssSQLiteDataSource(this);
        rssSQLiteDataSource.open();
        loadRssResources();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startid) {
        fetchRssItemsFromResources();
        return START_NOT_STICKY;
    }

    @Override
    public void onRssFinishLoading(String taskName, CopyOnWriteArrayList<RssItem> rssItems) {
        this.rssItems = rssItems;
        // update the database
        rssSQLiteDataSource.replaceDatabaseWithRssItems(this.rssItems);
        checkAndSendPushNotifications();
        updateLatestRssItemTime();
        updateLastUpdateTime();

        if (rssListener != null) {
            rssListener.onRssFinishLoading(taskName, this.rssItems);
        }
        stopSelf();
    }
}
