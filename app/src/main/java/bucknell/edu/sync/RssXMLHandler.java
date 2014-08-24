package bucknell.edu.sync;

import android.util.Log;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.concurrent.CopyOnWriteArrayList;

import bucknell.edu.Data.RssItem;

/**
 * Created by boolli on 8/23/14.
 */
public class RssXMLHandler extends DefaultHandler{
    protected CopyOnWriteArrayList<RssItem> rssItems;
    private RssItem currentItem;
    private boolean parsingTitle;
    private boolean parsingLink;
    private boolean parsingContent;
    private boolean parsingPubDate;
    private boolean parsingCategory;


    public RssXMLHandler(){
        super();
        this.rssItems = new CopyOnWriteArrayList<RssItem>();
    }

    public CopyOnWriteArrayList<RssItem> getRssItems(){
        return this.rssItems;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException{
        if (qName.equals("item")){
            currentItem = new RssItem();
        } else if (qName.equals("title")){
            parsingTitle = true;
        } else if (qName.equals("link")){
            parsingLink = true;
        } else if (qName.equals("pubDate")){
            parsingPubDate = true;
        } else if (qName.equals("category")){
            parsingCategory = true;
        } else if (qName.equals("content:encoded")){
            parsingContent = true;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException{
        if (parsingTitle){
            if (currentItem != null){
                currentItem.setTitle(new String(ch, start, length));
            }
        } else if (parsingLink){
            if (currentItem != null){
                currentItem.setLink(new String(ch, start, length));
            }
        } else if (parsingContent){
            if (currentItem != null){
                currentItem.setContent(new String(ch, start, length));
            }
        }
        // parse pubDate and category
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException{
        if (qName.equals("item")){
            rssItems.add(currentItem);
            Log.i("item title", currentItem.getTitle());
            Log.i("item content", currentItem.getContent());
        } else if (qName.equals("title")){
            parsingTitle = false;
        } else if (qName.equals("link")){
            parsingLink = false;
        } else if (qName.equals("pubDate")){
            parsingPubDate = false;
        } else if (qName.equals("category")){
            parsingCategory = false;
        } else if (qName.equals("content:encoded")){
            parsingContent = false;
        }
    }
}
