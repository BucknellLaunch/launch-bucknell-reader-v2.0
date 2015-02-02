# RssUpdateService

This is a Service class to download RSS feeds in the background and update the main UI.

## How does it work?

This service is started by MainActivity either when the user opens up the app or swipes down the view to refresh the list. Since Services are running in the main thread (UI thread) of the hosting process, it still needs to use AsyncTask to download the Rss feeds. Once the Service is started, the onCreated() method is triggered, in which it simply opens the database and loads the Rss resources, which will be used later to download the Rss feeds. 

From now on different methods will be called depending on how the service is started. If the service is started by the activity using bindService() ,a binder will be passed down to the hosting activity so that it can access the Service. This is done by the onBind() method, which creates a RssUpdateBinder object extended from Binder. The RssUpdateBinder class has a getService() method, so that once the activity gets the binder from onServiceConnected(), it can retrieve the service by calling the getService().

If the service is started by the Alarm service using startService() method, the onStartCommand() method will be triggered. It doesn't create a binder because the hosting activity is not necessarily running at the time the alarm is triggered.

Then the Service reads in RSS (fetchRssItemsFromResources()) and updates the database (rssSQLiteDataSource.replaceDatabaseWithRssItems()) when the RSS task finishes loading. This is mainly done by RssJsonAsyncTask. After that, it will notify the activity to update the UI using the RssListener.onRssFinishLoading() method if the reference to the listener is not null.

Note that when the RssItemsFragment object in MainActivity becomes invisible either from using stop() or from opening up individual RssItemFeedFragments, it will unbind itself from the service so that it won't get notified by the service running in the background. Once MainActivity is opened up again, it will bind itself with the service again.

## License

Copyright (c) 2014 BoolLi, under the [MIT license](http://www.opensource.org/licenses/mit-license.php).



