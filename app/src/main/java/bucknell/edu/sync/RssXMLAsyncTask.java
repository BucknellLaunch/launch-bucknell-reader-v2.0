package bucknell.edu.sync;

import android.app.Activity;
import android.os.AsyncTask;

import java.util.concurrent.CopyOnWriteArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import bucknell.edu.Data.RssItem;
import bucknell.edu.Interfaces.RssListener;

/**
 * Created by boolli on 8/23/14.
 */
public class RssXMLAsyncTask extends AsyncTask<String, Void, CopyOnWriteArrayList<RssItem>> {
    RssListener rssListener;

    public RssXMLAsyncTask(Activity activity){
        this.rssListener = (RssListener) activity;
    }

    @Override
    protected CopyOnWriteArrayList<RssItem> doInBackground(String... strings) {
        String url = strings[0];
        try{
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            RssXMLHandler handler = new RssXMLHandler();
            saxParser.parse(url, handler);
            return handler.getRssItems();
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(CopyOnWriteArrayList<RssItem> result){
        rssListener.onRssFinishLoading("",result);
    }
}
