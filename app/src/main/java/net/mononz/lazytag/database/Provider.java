package net.mononz.lazytag.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import net.mononz.lazytag.database.Database;

public class Provider extends ContentProvider {

    public static final String CONTENT_AUTHORITY = "net.mononz.lazytag";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private Database mOpenHelper;

    private static final int MESSAGES = 100;

    private static UriMatcher buildUriMatcher(){
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = CONTENT_AUTHORITY;

        matcher.addURI(authority, Database.Messages.TABLE_NAME, MESSAGES);
        return matcher;
    }

    @Override
    public boolean onCreate(){
        mOpenHelper = new Database(getContext());
        return true;
    }

    @Override
    public String getType(@NonNull Uri uri){
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MESSAGES:
                return Database.Messages.CONTENT_DIR_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder){
        Cursor retCursor;
        Log.d("query", uri.toString());
        switch (sUriMatcher.match(uri)) {
            case MESSAGES:
                retCursor = mOpenHelper.getReadableDatabase().query(Database.Messages.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Uri returnUri;
        switch (sUriMatcher.match(uri)) {
            case MESSAGES: {
                long _id = db.insert(Database.Messages.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = Database.Messages.buildUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into: " + uri);
                }
                break;
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
        getContext().getContentResolver().notifyChange(uri, null);
        Log.d("Insert", "notify changed: " + uri.toString());
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Unknown uri: " + uri);
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        return super.bulkInsert(uri, values);
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int numUpdated;
        if (contentValues == null) {
            throw new IllegalArgumentException("Cannot have null content values");
        }
        switch (sUriMatcher.match(uri)) {
            case MESSAGES:
                numUpdated = db.update(Database.Messages.TABLE_NAME, contentValues, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (numUpdated > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
            Log.d("Update", "notify changed (" + numUpdated + ")");
        }
        return numUpdated;
    }

}