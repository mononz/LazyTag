package net.mononz.lazytag.database;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.provider.BaseColumns;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

public class Database extends SQLiteAssetHelper {

    private static final String DATABASE_NAME = "database.sqlite3";

    public Database(Context context) {
        super(context, DATABASE_NAME, null, 1);
        //setForcedUpgrade();
    }

    public static final class Messages implements BaseColumns {

        public static final String TABLE_NAME = "messages";

        public static final String _id = "_id";
        public static final String label = "label";
        public static final String message = "message";
        public static final String created_at = "created_at";
        public static final String deleted_at = "deleted_at";

        public static final Uri CONTENT_URI = Provider.BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME).build();
        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + Provider.CONTENT_AUTHORITY + "/" + TABLE_NAME;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +"/" + Provider.CONTENT_AUTHORITY + "/" + TABLE_NAME;

        public static Uri buildUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

    }

}