package bucknell.edu.Activities;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import bucknell.edu.Data.RssResource;
import bucknell.edu.Data.RssItem;
import bucknell.edu.Fragments.RssItemFeedFragment;
import bucknell.edu.Fragments.RssItemsFragment;
import bucknell.edu.Interfaces.RssListener;
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

    public void loadRssResources() {
        rssResources = new ArrayList<RssResource>();
        String[] rssStringResources = getResources().getStringArray(R.array.rss_sources);
        for (String rssStringResource: rssStringResources) {
            String[] splitResult = rssStringResource.split("\\|", 2);
            RssResource rssResource = new RssResource(splitResult[0], splitResult[1]);
            rssResources.add(rssResource);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rssSQLiteDataSource = new RssSQLiteDataSource(this);
        rssSQLiteDataSource.open();

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
            RssJsonAsyncTask rssJsonAsyncTask = new RssJsonAsyncTask(resource.getName(), this);
            rssJsonAsyncTask.execute(resource.getUrl());
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
    }

    @Override
    public void onRssItemsFragmentInteraction(String title, String contentHTML) {
        // cancel all async tasks in the back ground

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
