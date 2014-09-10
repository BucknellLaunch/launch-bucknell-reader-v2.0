package bucknell.edu.Data;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by boolli on 8/23/14.
 */
public class RssItem implements Parcelable {
    private String title;
    private String link;
    private String content;
    private String date;
    private String dateFormat;

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getDateInLong() {
        try {
            Date dateObject = new SimpleDateFormat(dateFormat).parse(this.date);
            return dateObject.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return -1;
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

    public String toString() {
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
        parcel.writeString(date);
        // add more fields here
    }

    public RssItem(Parcel parcel) {
        title = parcel.readString();
        link = parcel.readString();
        content = parcel.readString();
        date = parcel.readString();
        // add more fields here
    }

    public RssItem() {

    }

    public static final Creator<RssItem> CREATOR = new Creator<RssItem>() {

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
