package bucknell.edu.Interfaces;

import java.util.concurrent.CopyOnWriteArrayList;

import bucknell.edu.Data.RssItem;

/**
 * Created by boolli on 8/23/14.
 */
public interface RssListener {
    public void onRssFinishLoading(CopyOnWriteArrayList<RssItem> rssItems);
}
