package bucknell.edu.Data;

import android.os.Parcel;
import android.os.Parcelable;

import bucknell.edu.Fragments.RssItemsFragment;

/**
 * Created by boolli on 8/23/14.
 */
public class RssItem implements Parcelable{
    private String title;
    private String link;
    private String content;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String toString(){
        return title;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeString(link);
        parcel.writeString(content);
        // add more fields here
    }

    public RssItem(Parcel parcel){
        title = parcel.readString();
        link = parcel.readString();
        content = parcel.readString();
        // add more fields here
    }

    public RssItem(){

    }

    public static final Creator<RssItem> CREATOR = new Creator<RssItem>(){

        @Override
        public RssItem createFromParcel(Parcel parcel) {
            return new RssItem(parcel);
        }

        @Override
        public RssItem[] newArray(int size) {
            return new RssItem[size];
        }
    };
}
