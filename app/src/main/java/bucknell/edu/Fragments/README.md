# Fragments

This package includes all the Fragment classes needed in the app.

## RssItemFeedFragment

This Fragment displays a single article, which currently includes the title and the content of the article.

### How does it work?

An instance of a RssItemFeedFragment is created via the factory method RssItemFeedFragment.newInstance(String title, String content). Once a new object is created, it inflates the title and the content to R.layout.fragment_rss_item_feed layout. Note that the object also registers the calling activity as a OnRssItemFeedFragmentInteractionListener, and notifies the listener when certain events happen. In the current stage, when the fragment stops and evokes the onStop() method, it will trigger the listener's onRssItemFeedFragmentStop() method.

## License

Copyright (c) 2014 BoolLi, under the [MIT license](http://www.opensource.org/licenses/mit-license.php).
