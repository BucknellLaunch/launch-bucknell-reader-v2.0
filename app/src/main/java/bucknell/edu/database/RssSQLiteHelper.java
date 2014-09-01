package bucknell.edu.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by boolli on 8/26/14.
 */
public class RssSQLiteHelper extends SQLiteOpenHelper {
    public static final String TABLE_RSS_ITEMS = "RssItems";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_LINK = "link";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_CONTENT = "content";

    public static final String DATABASE_NAME = "rss_items.db";
    public static final int DATABASE_VERSION = 1;

    public RssSQLiteHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_RSS_ITEMS + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_TITLE
            + " text not null, " + COLUMN_LINK + " text not null, "
            + COLUMN_DATE + " text not null, "  + COLUMN_CONTENT + " text not null);";

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_RSS_ITEMS);
        onCreate(sqLiteDatabase);
    }
}
