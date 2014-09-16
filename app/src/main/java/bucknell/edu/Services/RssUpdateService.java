package bucknell.edu.Services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import bucknell.edu.Data.RssItem;
import bucknell.edu.Data.RssResource;
import bucknell.edu.Interfaces.RssListener;
import bucknell.edu.bucknellreader.R;
import bucknell.edu.database.RssSQLiteDataSource;
import bucknell.edu.sync.RssJsonAsyncTask;

public class RssUpdateService extends Service implements RssListener{
    // TODO: move these two numbers into XML
    public static final long INITIAL_ALARM_DELAY = 1000L;
    public static final long ALARM_INTERVAL = 15000L;
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

        if (rssListener != null) {
            rssListener.onRssFinishLoading(taskName, this.rssItems);
        }
        stopSelf();
    }
}
