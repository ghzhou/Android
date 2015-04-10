package com.zj.emailnotification;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public class EmailNotificationDataProvider extends ContentProvider {

    public static String MIME = "vnd.android.cursor.item/vnd.com.zj.emailnotification.provider.email_notification";
    public static String TABLE = "email_notification_setting";

    private SQLiteDatabase db;

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        return MIME;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean onCreate() {
        db =  new MainDatabaseHelper(getContext()).getReadableDatabase();
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        return db.query(
                TABLE, null, null, null, null, null, null);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        db.update(TABLE,values,null,null);
        return 1;
    }


    protected class MainDatabaseHelper extends SQLiteOpenHelper {

        private final String SQL_CREATE_MAIN = "CREATE TABLE " +
                TABLE +
                "(" +                           // The columns in the table
                " _ID INTEGER PRIMARY KEY, " +
                Settings.SMTP_SERVER  +" TEXT," +
                Settings.PORT_NUMBER +" TEXT, " +
                Settings.EMAIL_ADDRESS +" TEXT, " +
                Settings.PASSWORD +" TEXT, " +
                Settings.EMAIL_RECIPIENT +" TEXT )";
        private final String SQL_INSERT_ONE_ROW = "INSERT INTO " +
                TABLE + " values(1,'smtp.mail.yahoo.com','587','yh_zhoujie@yahoo.com','','zhou_jack@live.com')";

        private static final String DBNAME = "email_notification_setting_db";

        /*
          * Instantiates an open helper for the provider's SQLite data repository
          * Do not do database creation and upgrade here.
          */
        MainDatabaseHelper(Context context) {
            super(context, DBNAME, null, 1);
        }

        /*
         * Creates the data repository. This is called when the provider attempts to open the
         * repository and SQLite reports that it doesn't exist.
         */
        public void onCreate(SQLiteDatabase db) {

            // Creates the main table
            db.execSQL(SQL_CREATE_MAIN);
            db.execSQL(SQL_INSERT_ONE_ROW);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }


}
