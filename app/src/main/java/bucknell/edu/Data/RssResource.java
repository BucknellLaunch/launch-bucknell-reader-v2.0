package bucknell.edu.Data;

import android.os.AsyncTask;

/**
 * Created by boolli on 9/7/14.
 */
public class RssResource {
    String name;
    String url;


    String dateFormat;

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }
    public RssResource(String name, String url, String dateFormat){
        this.name = name;
        this.url = url;
        this.dateFormat = dateFormat;
    }


    public String getDateFormat() {
        return dateFormat;
    }


}
