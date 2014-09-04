package bucknell.edu.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.concurrent.CopyOnWriteArrayList;

import bucknell.edu.Data.RssItem;

/**
 * Created by boolli on 8/31/14.
 */
public class RssSQLiteDataSource {
    private SQLiteDatabase database;
    private RssSQLiteHelper rssSQLiteHelper;
    private String[] allColumns = {RssSQLiteHelper.COLUMN_ID, RssSQLiteHelper.COLUMN_TITLE,
            RssSQLiteHelper.COLUMN_DATE, RssSQLiteHelper.COLUMN_CONTENT, RssSQLiteHelper.COLUMN_LINK};

    public RssSQLiteDataSource(Context context) {
        rssSQLiteHelper = new RssSQLiteHelper(context);
    }

    public void open() {
        try {
            database = rssSQLiteHelper.getWritableDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            rssSQLiteHelper.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public RssItem addRssItem(RssItem item) {
        ContentValues values = new ContentValues();
        values.put(RssSQLiteHelper.COLUMN_TITLE, item.getTitle());
        values.put(RssSQLiteHelper.COLUMN_DATE, item.getDate());
        values.put(RssSQLiteHelper.COLUMN_CONTENT, item.getContent());
        values.put(RssSQLiteHelper.COLUMN_LINK, item.getLink());

        long insertId = database.insert(RssSQLiteHelper.TABLE_RSS_ITEMS, null, values);
        if (insertId > 0) {
            return item;
        } else {
            return null;
        }
    }

    public CopyOnWriteArrayList<RssItem> addRssItems(CopyOnWriteArrayList<RssItem> items){
        for (RssItem item: items){
            if (addRssItem(item) == null){
                return null;
            }
        }
        return items;
    }

    public CopyOnWriteArrayList<RssItem> getAllRssItems(){
        CopyOnWriteArrayList<RssItem> items = new CopyOnWriteArrayList<RssItem>();
        Cursor cursor = database.query(RssSQLiteHelper.TABLE_RSS_ITEMS, allColumns, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            RssItem item = cursorToRssItem(cursor);
            items.add(item);
            cursor.moveToNext();
        }
        cursor.close();
        return items;
    }

    private RssItem cursorToRssItem(Cursor cursor) {
        RssItem item = new RssItem();
        item.setTitle(cursor.getString(1));
        item.setDate(cursor.getString(2));
        item.setContent(cursor.getString(3));
        item.setLink(cursor.getString(4));

        return item;
    }

    public boolean isDatabaseEmpty(){
        boolean isEmpty = true;
        Cursor cursor = database.rawQuery("SELECT * FROM " + RssSQLiteHelper.TABLE_RSS_ITEMS, null);
        if (cursor.moveToNext()){
            isEmpty = false;
        }
        return isEmpty;
    }
}
