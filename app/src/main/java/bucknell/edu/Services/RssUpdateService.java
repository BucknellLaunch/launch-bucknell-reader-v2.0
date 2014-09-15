package bucknell.edu.Services;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
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
    private HashMap<String, AsyncTask> rssAsyncTasksMap;
    private final IBinder binder = new RssUpdateBinder();
    private RssListener rssListener;
    public static final int MESSAGE_UPDATE_DATABASE = 0;

    public class RssUpdateBinder extends Binder {
        public RssUpdateService getService() {
            return RssUpdateService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void addRssListener(RssListener listener) {
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
            rssAsyncTasksMap.put(resource.getName(), rssJsonAsyncTask);
        }
    }

    @Override
    public void onCreate() {
        Log.i("Services started", "OnCreate");
        rssSQLiteDataSource = new RssSQLiteDataSource(this);
        rssSQLiteDataSource.open();
        loadRssResources();
        rssAsyncTasksMap = new HashMap<String, AsyncTask>();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startid) {
        fetchRssItemsFromResources();
        Log.i("Start command", "start command");
        return START_NOT_STICKY;
    }

    @Override
    public void onRssFinishLoading(String taskName, CopyOnWriteArrayList<RssItem> rssItems) {
        this.rssItems = rssItems;
        Log.i("Service finish refreshing", "Service finish refreshing");
        if (rssListener != null) {
            rssListener.onRssFinishLoading(taskName, this.rssItems);
        }
        stopSelf();
    }
}
