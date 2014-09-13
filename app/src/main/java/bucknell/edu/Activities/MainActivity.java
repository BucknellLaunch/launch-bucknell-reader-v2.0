package bucknell.edu.Activities;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import bucknell.edu.Data.RssResource;
import bucknell.edu.Data.RssItem;
import bucknell.edu.Fragments.RssItemFeedFragment;
import bucknell.edu.Fragments.RssItemsFragment;
import bucknell.edu.Interfaces.RssListener;
import bucknell.edu.Services.RssUpdateService;
import bucknell.edu.bucknellreader.R;
import bucknell.edu.Fragments.SplashScreenFragment;
import bucknell.edu.database.RssSQLiteDataSource;
import bucknell.edu.sync.RssJsonAsyncTask;


public class MainActivity extends Activity implements RssListener,
        RssItemsFragment.OnRssItemsFragmentInteractionListener,
        RssItemFeedFragment.OnRssItemFeedFragmentInteractionListener,
        SwipeRefreshLayout.OnRefreshListener {
    private SplashScreenFragment splashScreenFragment;
    private CopyOnWriteArrayList<RssItem> rssItems;
    private RssItemsFragment rssItemsFragment;
    private RssSQLiteDataSource rssSQLiteDataSource;
    private ArrayList<RssResource> rssResources;
    private HashMap<String, AsyncTask> rssAsyncTasksMap;
    private static final long INITIAL_ALARM_DELAY = 1000L;
    private static final long ALARM_INTERVAL = 3000L;

    public void loadRssResources() {
        rssResources = new ArrayList<RssResource>();
        String[] rssStringResources = getResources().getStringArray(R.array.rss_sources);
        for (String rssStringResource: rssStringResources) {
            String[] splitResult = rssStringResource.split("\\|", 3);
            RssResource rssResource = new RssResource(splitResult[0], splitResult[1], splitResult[2]);
            rssResources.add(rssResource);
        }
    }

    public void setRssUpdateServiceAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent rssUpdateServiceIntent = new Intent(MainActivity.this, RssUpdateService.class);
        PendingIntent pendingRssUpdateServiceIntent = PendingIntent.getService(MainActivity.this, 0,rssUpdateServiceIntent, 0);
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + INITIAL_ALARM_DELAY, ALARM_INTERVAL, pendingRssUpdateServiceIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rssSQLiteDataSource = new RssSQLiteDataSource(this);
        rssSQLiteDataSource.open();
        setRssUpdateServiceAlarm();

        // load the rss XML resources into the array list
        loadRssResources();
        rssAsyncTasksMap = new HashMap<String, AsyncTask>();

        // if the database is empty, then show splash screen and fetch Rss Items for the first time
        if (rssSQLiteDataSource.isDatabaseEmpty()) {
            addSplashScreen();
            fetchRssItemsFromResources();
        } else {
            // reads in the Rss Items from the database and renders them to RssItemsFragment
            rssItems = rssSQLiteDataSource.getAllRssItems();
            ShowRssItemsFragment(rssItems);
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

    private void addSplashScreen() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        splashScreenFragment = new SplashScreenFragment();
        fragmentTransaction.add(android.R.id.content, splashScreenFragment, "splash_screen");
        fragmentTransaction.commit();
    }

    private void removeSplashScreen(){
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.remove(splashScreenFragment);
        fragmentTransaction.commit();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    private void ShowRssItemsFragment(CopyOnWriteArrayList<RssItem> rssItems) {
        rssItemsFragment = new RssItemsFragment(rssItems);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.rss_items, rssItemsFragment, "rss_items_fragment");
        fragmentTransaction.commit();
    }


    private void ShowRssItemFeedFragment(String title, String content) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.rss_items, RssItemFeedFragment.newInstance(title, content), "rss_item_feed_fragment");
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }


    /**
     * performs actions after the RSS feeds finish loading. This will happen when the RssAsyncTask objects finish loading.
     *
     * @param rssItems an array of RssItems returned by the RssAsyncTask objects.
     */
    @Override
    public void onRssFinishLoading(String taskName, CopyOnWriteArrayList<RssItem> rssItems) {
        this.rssItems = rssItems;

        // if the database is empty, it means that it's the first time users open up the app. So need to update
        // the empty list with rss items and remove splash screen
        if (rssSQLiteDataSource.isDatabaseEmpty()) {
            removeSplashScreen();
            // add rssItems into database
            if (rssItems != null)
                rssSQLiteDataSource.addRssItems(this.rssItems);

            // * still need to store the most recent data so the app knows when to update itself
            ShowRssItemsFragment(rssItems);

        } else { // if the database is not empty, then it is because users swipe to refresh the list.
            rssSQLiteDataSource.replaceDatabaseWithRssItems(this.rssItems);
            rssItemsFragment.resetRssItems(this.rssItems);
            rssItemsFragment.stopRefreshing();
        }
        // remove the task from the map
        rssAsyncTasksMap.remove(taskName);

        // update last update time
        updateLastUpdateTime();

        // update the latest Rss item date

        updateLatestRssItemTime();
    }

    private void updateLatestRssItemTime() {
        if (this.rssItems==null || this.rssItems.isEmpty())
            return;
        // get the first Rss Item
        RssItem rssItem = this.rssItems.get(0);
        long time = rssItem.getDateInLong();
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("LatestRssItemTime", time);
        editor.commit();
    }

    private void updateLastUpdateTime() {
        Date date = Calendar.getInstance().getTime();
        long time = date.getTime();
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("LastUpdateTime", time);
        editor.commit();
    }

    public void cancelAllAsyncTasks() {
        if (rssAsyncTasksMap == null)
            return;
        Iterator it = rssAsyncTasksMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            AsyncTask task = (AsyncTask) pairs.getValue();
            task.cancel(true);
            it.remove();
        }
    }

    @Override
    public void onRssItemsFragmentInteraction(String title, String contentHTML) {
        // cancel all the async tasks when users click on any item
        cancelAllAsyncTasks();

        // load and display the new fragment
        String contentPlainText = Html.fromHtml(contentHTML).toString();
        ShowRssItemFeedFragment(title, contentPlainText);
    }

    @Override
    public void onRssItemFeedFragmentInteraction(Uri uri) {
        // handle any interaction event on the "show" page
    }

    @Override
    public void onRefresh() {
        fetchRssItemsFromResources();
    }
}
