#RssJsonAsyncTask

This is a subclass of AsyncTask to update RSS feed in a separate thread. 

##How does it work?

If you haven't seen AsyncTask before, check out the documentation of AsyncTask [here](http://developer.android.com/reference/android/os/AsyncTask.html)!

Each RssJsonAsyncTask object can handle the job of reading RSS feeds from one source. You can pass in an RssResource object to its constructor to specify where you want it to read RSS from. You also need to give it a RssListener so that the when the task is finished, it will notify the listener. 
 
After you run the execute() method on an AsyncTask object, the doInBackground() method will run on another thread - this is where you want to read the RSS feeds and store them locally. In my implementation, RssJsonAsyncTask reads the feeds into a [JSONObject](http://developer.android.com/reference/org/json/JSONObject.html)(this is all done in the getJSONFromUrl() method), and parses the json object into an ArrayList(parseJSONToList() method). 

After doInBackground() finishes, anther method onPostExecute() will run on the main UI thread. This is designed for the AsyncTask to communicate with and update the main UI. In this method I notify the RssListener through its onRssFinishLoading() method. As you can see, the resulting list is being passed as an argument to the onPostExecute() method, and I simply forward it to the RssListener, which in this case is a RssUpdateService object. If you read the documentation on RssUpdateService carefully, you know that it actually runs on the main UI thread. The Service will then notify MainActivity, which will have the RssItemsFragment update the list. Here is the whole process the RSS feeds go through from the source to the UI:

RSS source -> RssJsonAsyncTask -> RssUpdateService -> MainActivity -> RssItemsFragment
<------ Separate thread --------><-------------- Main UI thread --------------------->


## License

Copyright (c) 2015 BoolLi, under the [MIT license](http://www.opensource.org/licenses/mit-license.php).
