package bucknell.edu.Activities;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.concurrent.CopyOnWriteArrayList;

import bucknell.edu.Data.RssItem;
import bucknell.edu.Fragments.RssItemFeedFragment;
import bucknell.edu.Fragments.RssItemsFragment;
import bucknell.edu.Interfaces.RssListener;
import bucknell.edu.bucknellreader.R;
import bucknell.edu.Fragments.SplashScreenFragment;
import bucknell.edu.database.RssSQLiteDataSource;
import bucknell.edu.sync.RssJsonAsyncTask;
import bucknell.edu.sync.RssXMLAsyncTask;


public class MainActivity extends Activity implements RssListener, RssItemsFragment.OnRssItemsFragmentInteractionListener, RssItemFeedFragment.OnRssItemFeedFragmentInteractionListener {
    private SplashScreenFragment splashScreenFragment;
    private CopyOnWriteArrayList<RssItem> rssItems;
    private RssItemsFragment rssItemsFragment;
    private RssSQLiteDataSource rssSQLiteDataSource;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rssSQLiteDataSource = new RssSQLiteDataSource(this);
        rssSQLiteDataSource.open();

        if (rssSQLiteDataSource.isDatabaseEmpty()) {
            addSplashScreen();
            RssJsonAsyncTask bucknellianJSONAsyncTask = new RssJsonAsyncTask(this);
            bucknellianJSONAsyncTask.execute("http://bucknellian.net/category/news/?json=1");
        } else {
            // reads in the Rss Items from the database and renders them to RssItemsFragment
            rssItems = rssSQLiteDataSource.getAllRssItems();
            ShowRssItemsFragment(rssItems);
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
    public void onRssFinishLoading(CopyOnWriteArrayList<RssItem> rssItems) {
        this.rssItems = rssItems;
        removeSplashScreen();

        // add rssItems into database
        if (rssItems != null)
            rssSQLiteDataSource.addRssItems(this.rssItems);

        // * still need to store the most recent data so the app knows when to update itself

        ShowRssItemsFragment(rssItems);
    }
    @Override
    public void onRssItemsFragmentInteraction(String title, String contentHTML) {
        String contentPlainText = Html.fromHtml(contentHTML).toString();
        ShowRssItemFeedFragment(title, contentPlainText);
    }

    @Override
    public void onRssItemFeedFragmentInteraction(Uri uri) {
        // handle any interaction event on the "show" page
    }
}
