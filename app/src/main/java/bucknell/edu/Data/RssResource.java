package bucknell.edu.Data;

import android.os.AsyncTask;

/**
 * Created by boolli on 9/7/14.
 */
public class RssResource {
    String name;
    String url;

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }
    public RssResource(String name, String url){
        this.name = name;
        this.url = url;
    }


}
