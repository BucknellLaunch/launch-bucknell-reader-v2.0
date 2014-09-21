# MainActivity

This Acitivity displays the main UI which contains the list of RSS items and handles various events such as click events to open new articles or Service events after loading is finished.

## How does it work?

Right after it gets created, the MainActivity sets up an alarm to run the RssUpdatingService periodically (setRssUpdateServiceAlarm method)and binds itself to the RssUpdatingService (bindRssUpdateService method). Once the binding is done, it queries the Service for RssItems. If the database is empty, a splash screen will show up and the activity will wait for the RssUpdateService to return a list of RssItems. If the database is not empty, the RssUpdateService will return the RssItems directly. After getting the RssItems from the RssUpdateService, the MainActivity loads them into the RssItemsFragment, which contains a ListView. Then the MainActivity listens to various events that might happen during the life time of the Activity.

When users click on any RssItem in the ListView, the ShowRssItemFeedFragment() method will be triggered, which starts a new wRssItemFeedFragment containting the content of the article. Once the users swipe down the screen or the RssUpdatingService finishes updating the database, the onRssFinishLoading() method gets triggered, which loads the RssItems into the RssItemsFragment.

Note that the MainAcitivity defines a enum calld MainActivityState, which holds one of the three following values: ON_FETCHING_NEW_DATA, ON_REFRESHING, and N_HOLD. Depending on different states, the MainActivity will react differently when new RssItems get returned from the RssUpdatingService.

One other thing to note is that when the Activity stops (triggering the onStop() method), it needs to disconnect from the RssUpdateService so that the Service won't notifiy the Acitity when it finishes reading RssItems.

## License

Copyright (c) 2014 BoolLi, under the [MIT license](http://www.opensource.org/licenses/mit-license.php).
