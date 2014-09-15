package bucknell.edu.Services;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Handler;

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
    private Messenger messenger;
    public static final int MESSAGE_UPDATE_DATABASE = 0;

    public RssUpdateService() {
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
    public IBinder onBind(Intent intent) {
        Bundle extras = intent.getExtras();
        messenger = (Messenger) extras.get("MESSENGER");

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
        // if the database is empty, then show splash screen and fetch Rss Items for the first time
        fetchRssItemsFromResources();

        Log.i("Start command", "start command");
        return START_NOT_STICKY;
    }

    public void sendMessage(int state) {
        Message message = Message.obtain();
        message.arg1 = state;

    }

    @Override
    public void onRssFinishLoading(String taskName, CopyOnWriteArrayList<RssItem> rssItems) {
        this.rssItems = rssItems;
        Log.i("Service finish refreshing", "Service finish refreshing");
        stopSelf();
    }
}
