package bucknell.edu.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.text.Html;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;

import bucknell.edu.Activities.MainActivity;
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
    private Integer startid;


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
        SharedPreferences prefs = getApplicationContext().
                getSharedPreferences(getString(R.string.shared_preferences_file_name), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(getString(R.string.shared_preferences_last_update_time), time);
        editor.apply();
    }

    private boolean hasNewRssItems () {
        SharedPreferences prefs = getApplicationContext().
                getSharedPreferences(getString(R.string.shared_preferences_file_name), Context.MODE_PRIVATE);
        long localLatestTime = prefs.getLong(getString(R.string.shared_preferences_latest_rss_item_time), 0);
        long remoteLatestTime = this.rssItems.get(0).getDateInLong();
        if (remoteLatestTime > localLatestTime) {
            return true;
        } else {
            return false;
        }
    }

    private void sendPushNotifications () {
        Intent intent = new Intent(this, MainActivity.class);
        // set the flag of the intent to FLAG_ACTIVITY_SINGLE_TOP
        // http://stackoverflow.com/questions/1198558/how-to-send-parameters-from-a-notification-click-to-an-activity
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        // we have already made sure that this.rssItems is not null in checkAndSendPushNotifications() function
        RssItem item = this.rssItems.get(0);
        String title = item.getTitle();
        String content = item.getContent();
        content = Html.fromHtml(content).toString();

        intent.putExtra("title", title);
        intent.putExtra("content", content);

        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.launch_logo)
                .setContentTitle(title)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setContentText(content);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1,builder.build());
    }

    private void checkAndSendPushNotifications() {
        if (this.rssItems == null || this.rssItems.size() == 0)
            return;
        if (hasNewRssItems() && this.rssListener == null) {
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
        this.startid = startid;
        return START_NOT_STICKY;
    }

    @Override
    public void onLowMemory() {
        Log.i("RssUpdateService", "low memory");
        super.onLowMemory();
    }

    @Override
    public void onDestroy() {
        Log.i("RssUpdateServie", "onDestroy");
        if (this.startid != null && this.rssSQLiteDataSource != null) {
            Log.i("RssUpdateService", "closing database");
            rssSQLiteDataSource.close();
        }
        super.onDestroy();
    }

    @Override
    public void onRssFinishLoading(String taskName, CopyOnWriteArrayList<RssItem> rssItems) {
        Log.i("RssUpdateService", "onRssFinishLoading");
        this.rssItems = rssItems;
        // update the database
        try {
            rssSQLiteDataSource.replaceDatabaseWithRssItems(this.rssItems);
            checkAndSendPushNotifications();
            updateLatestRssItemTime();
            updateLastUpdateTime();

            if (rssListener != null) {
                rssListener.onRssFinishLoading(taskName, this.rssItems);
            }
            stopSelf();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
