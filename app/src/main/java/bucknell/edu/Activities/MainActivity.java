package bucknell.edu.Activities;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.concurrent.CopyOnWriteArrayList;

import bucknell.edu.Data.RssItem;
import bucknell.edu.Fragments.RssItemFeedFragment;
import bucknell.edu.Fragments.RssItemsFragment;
import bucknell.edu.Interfaces.RssListener;
import bucknell.edu.Services.RssUpdateService;
import bucknell.edu.bucknellreader.R;
import bucknell.edu.Fragments.SplashScreenFragment;



public class MainActivity extends Activity implements RssListener,
        RssItemsFragment.OnRssItemsFragmentInteractionListener,
        RssItemFeedFragment.OnRssItemFeedFragmentInteractionListener,
        SwipeRefreshLayout.OnRefreshListener {
    private SplashScreenFragment splashScreenFragment;
    private CopyOnWriteArrayList<RssItem> rssItems;
    private RssItemsFragment rssItemsFragment;

    // The state of the activity to indicate what the activity is doing right now
    enum MainActivityState {ON_FETCHING_NEW_DATA, ON_REFRESHING, ON_HOLD }
    MainActivityState mainActivityState;

    private ServiceConnection rssUpdateServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            RssUpdateService.RssUpdateBinder rssUpdateBinder = (RssUpdateService.RssUpdateBinder) iBinder;
            rssUpdateService = rssUpdateBinder.getService();
            rssUpdateService.setRssListener(MainActivity.this);

            if (rssUpdateService.isDatabaseEmpty()) {
                addSplashScreen();
                rssUpdateService.fetchRssItemsFromResources();
                mainActivityState = MainActivityState.ON_FETCHING_NEW_DATA;
            } else {
                rssItems = rssUpdateService.fetchRssItemsFromDatabase();
                showRssItemsFragment(rssItems);
                onNewIntent(getIntent());
                mainActivityState = MainActivityState.ON_HOLD;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            rssUpdateService = null;
        }
    };
    private RssUpdateService rssUpdateService;

    @Override
    public void onStop() {
        super.onStop();
        if (rssUpdateService != null) {
            rssUpdateService.setRssListener(null);
/*            remember to unbind the service when the activity exits. Otherwise ServiceConnectionLeaked will be raised
            http://stackoverflow.com/questions/18575903/serviceconnectionleaked-in-android*/
            unbindService(rssUpdateServiceConnection);
/*          ServiceConnection.onServiceDisConnected() is not supposed to be called. So need to do this manually
            Check out the link here:
            http://stackoverflow.com/questions/12277673/android-services-error-service-not-registered*/
            rssUpdateService = null;
        }
    }

    public void setRssUpdateServiceAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent rssUpdateServiceIntent = new Intent(MainActivity.this, RssUpdateService.class);
        PendingIntent pendingRssUpdateServiceIntent = PendingIntent.getService(MainActivity.this, 0,rssUpdateServiceIntent, 0);
        Resources res = getResources();
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + res.getInteger(R.integer.rss_update_service_initial_alarm_delay), res.getInteger(R.integer.rss_update_service_alarm_interval), pendingRssUpdateServiceIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRssUpdateServiceAlarm();
        bindRssUpdateService();
    }

    private void bindRssUpdateService() {
        Intent rssUpdateServiceIntent = new Intent(MainActivity.this, RssUpdateService.class);
        bindService(rssUpdateServiceIntent, rssUpdateServiceConnection, Context.BIND_AUTO_CREATE);
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
    protected void onNewIntent(Intent intent) {
        // the activity will receive the intent when users start it from the push notifications
        // TODO: parse the intent and start the corresponding fragment
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            String title = bundle.getString("title");
            String content = bundle.getString("content");
            showRssItemFeedFragment(title, content);
            // update the intent to be an empty intent
            setIntent(new Intent());
        }
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


    private void showRssItemsFragment(CopyOnWriteArrayList<RssItem> rssItems) {
        if (rssItemsFragment == null) {
            rssItemsFragment = new RssItemsFragment(rssItems);
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.rss_items, rssItemsFragment, "rss_items_fragment");
            fragmentTransaction.commit();
        } else {
            rssItemsFragment.resetRssItems(rssItems);
        }
    }

    private void showRssItemFeedFragment(String title, String content) {
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
        Log.i("success", "success");

        // if the current state is on_fetching_new_data
        if (mainActivityState == MainActivityState.ON_FETCHING_NEW_DATA) {
            removeSplashScreen();
            showRssItemsFragment(rssItems);
            mainActivityState = MainActivityState.ON_HOLD;

        } else if (mainActivityState == MainActivityState.ON_REFRESHING) { // if the current state is on_refreshing
            rssItemsFragment.resetRssItems(this.rssItems);
            rssItemsFragment.stopRefreshing();
            mainActivityState = MainActivityState.ON_HOLD;
        } else {
            // stop refreshing or remove splash screen anyway
            rssItemsFragment.stopRefreshing();
            if (splashScreenFragment != null) {
                removeSplashScreen();
            }
        }
    }

    @Override
    public void onRssItemsFragmentInteraction(String title, String contentHTML) {
        // load and display the new fragment
        String contentPlainText = Html.fromHtml(contentHTML).toString();
        showRssItemFeedFragment(title, contentPlainText);
    }

    @Override
    public void onRssItemFeedFragmentStart() {
        if (rssUpdateService != null) {
            rssUpdateService.setRssListener(null);
            unbindService(rssUpdateServiceConnection);
        }
        Log.i("On RssItemsFragmentStart", "On RssItemsFragmentStart");
    }

    @Override
    public void onRssItemFeedFragmentStop() {
        bindRssUpdateService();
    }

    @Override
    public void onRssItemFeedFragmentInteraction(Uri uri) {
        // handle any interaction event on the "show" page
    }

    @Override
    public void onRefresh() {
        rssUpdateService.fetchRssItemsFromResources();
        mainActivityState = MainActivityState.ON_REFRESHING;
    }
}
