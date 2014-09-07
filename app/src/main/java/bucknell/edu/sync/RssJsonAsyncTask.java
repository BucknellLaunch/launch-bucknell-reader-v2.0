package bucknell.edu.sync;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.CopyOnWriteArrayList;

import bucknell.edu.Data.RssItem;
import bucknell.edu.Interfaces.RssListener;

/**
 * Created by boolli on 8/24/14.
 */
public class RssJsonAsyncTask extends AsyncTask<String, Void, CopyOnWriteArrayList<RssItem>> {
    private CopyOnWriteArrayList<RssItem> rssItems;
    private RssListener rssListener;
    private String taskName;

    public RssJsonAsyncTask(String taskName, Activity activity){
        this.taskName = taskName;
        rssListener = (RssListener) activity;
        rssItems = new CopyOnWriteArrayList<RssItem>();
    }

    @Override
    protected CopyOnWriteArrayList<RssItem> doInBackground(String... strings) {
        String url = strings[0];
        try {
            JSONObject jObj = getJSONFromUrl(url);
            parseJSONToList(jObj);
        } catch (Exception e){
            e.printStackTrace();
        }
        return rssItems;
    }

    @Override
    protected void onPostExecute(CopyOnWriteArrayList<RssItem> result){
        rssListener.onRssFinishLoading(taskName, result);
    }

    private JSONObject getJSONFromUrl(String url) {
        InputStream is = null;
        JSONObject jObj = null;
        String json = null;
        try{
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);

            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();
            is = httpEntity.getContent();

            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine())!=null){
                sb.append(line + '\n');
            }
            is.close();
            json = sb.toString();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try{
            jObj = new JSONObject(json);
        } catch (JSONException e){
            e.printStackTrace();
        }
        return jObj;
    }

    private void parseJSONToList(JSONObject json){
        try {
            if (json.getString("status").equalsIgnoreCase("ok")){
                JSONArray posts = json.getJSONArray("posts");
                for (int i = 0; i < posts.length(); i++){
                    JSONObject post = (JSONObject) posts.getJSONObject(i);
                    RssItem rssItem = new RssItem();
                    rssItem.setTitle(post.getString("title"));
                    rssItem.setLink(post.getString("url"));
                    rssItem.setContent(post.getString("content"));
                    rssItem.setDate(post.getString("date"));
                    // get more info from JSON
                    rssItems.add(rssItem);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
