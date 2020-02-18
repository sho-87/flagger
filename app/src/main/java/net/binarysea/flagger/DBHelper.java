package net.binarysea.flagger;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DBHelper extends SQLiteOpenHelper {
    private static final String TAG = "DBHelper";
    //Constants for db name and version
    static final String DATABASE_NAME = "flagger.db";
    private static final int DATABASE_VERSION = 1;
    //Constants for identifying events table and fields
    private static final String EVENTS_TABLE_NAME = "events";
    private static final String EVENTS_TABLE_NAME_TEMP = "events_temp";
    private static final String EVENTS_ID = "id";
    private static final String EVENTS_TYPE = "type";
    private static final String EVENTS_TIME = "time";
    //Events table structure
    private static final String EVENTS_TABLE_STRUCTURE =
            " (" + EVENTS_ID + " INTEGER, " +
                    EVENTS_TYPE + " TEXT, " +
                    EVENTS_TIME + " INTEGER" +
                    ")";
    //SQL to create events table
    private static final String EVENTS_TABLE_CREATE =
            "CREATE TABLE " + EVENTS_TABLE_NAME + EVENTS_TABLE_STRUCTURE;
    //SQL to create TEMP events table
    private static final String EVENTS_TABLE_CREATE_TEMP =
            "CREATE TEMP TABLE " + EVENTS_TABLE_NAME_TEMP + EVENTS_TABLE_STRUCTURE;
    private static Handler messageHandler;
    private static DBHelper sInstance;
    private SQLiteDatabase db;

    /**
     * Constructor should be private to prevent direct instantiation.
     * make call to static method "getInstance()" instead.
     */
    private DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        // String databasePath = context.getDatabasePath(DATABASE_NAME).getPath();
        db = this.getWritableDatabase();
    }

    /**
     * The static getInstance() method ensures that only one DatabaseHelper will ever exist at any given time.
     * If the sInstance object has not been initialized, one will be created.
     * If one has already been created then it will simply be returned.
     * You should not initialize your helper object using with new DatabaseHelper(context)!
     * Instead, always use DatabaseHelper.getInstance(context), as it guarantees that only
     * one database helper will exist across the entire application’s lifecycle.
     */
    static synchronized DBHelper getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new DBHelper(context.getApplicationContext());
            Log.d(TAG, "New DBHelper created");
        }

        return sInstance;
    }

    public static synchronized DBHelper getInstance(Context context, Handler handler) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new DBHelper(context.getApplicationContext());
            Log.d(TAG, "New DBHelper created");
        }

        messageHandler = handler;
        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate called");

        //Create persistent tables
        db.execSQL(EVENTS_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade called");

        //Drop persistent tables
        db.execSQL("DROP TABLE IF EXISTS " + EVENTS_TABLE_NAME);

        //Recreate tables
        onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        createTempTables(db);
    }

    void closeDB() {
        if (db != null) {
            Log.d(TAG, "closeDB: closing db");
            db.close();
            db = null;
        }

        if (sInstance != null) {
            Log.d(TAG, "closeDB: closing DBHelper instance");
            sInstance.close();
            sInstance = null;
        }
    }

    private void createTempTables(SQLiteDatabase db) {
        Log.d(TAG, "Creating Temp tables");

        //Create temp tables
        db.execSQL(EVENTS_TABLE_CREATE_TEMP);
    }

    boolean hasID(Short subNum) throws SQLException {
        //Check if subject exists in persistent data table
        String query = "SELECT * FROM " + EVENTS_TABLE_NAME + " WHERE " + EVENTS_ID + "=" + subNum;

        Cursor c = db.rawQuery(query, null);
        boolean exists = (c.getCount() > 0);
        c.close();

        return exists;
    }

    boolean hasTempData(Short subNum) throws SQLException {
        //Check if data, for this subject, exists in the temp data table
        String query = "SELECT * FROM " + EVENTS_TABLE_NAME_TEMP + " WHERE " + EVENTS_ID + "=" + subNum;

        Cursor c = db.rawQuery(query, null);
        boolean exists = (c.getCount() > 0);
        c.close();

        return exists;
    }

    void deleteTempData() throws SQLException {
        //Delete temp data. Table is not removed.
        db.delete(EVENTS_TABLE_NAME_TEMP, null, null);
    }

    void insertTempData(short id, String type, long time) throws SQLException {
        ContentValues data = new ContentValues();

        data.put(EVENTS_ID, id);
        data.put(EVENTS_TYPE, type);
        data.put(EVENTS_TIME, time);

        db.insertOrThrow(EVENTS_TABLE_NAME_TEMP, null, data);
    }

    void saveTempData() throws SQLException {
        String saveDataSQL = "INSERT INTO " + EVENTS_TABLE_NAME + " SELECT * FROM " + EVENTS_TABLE_NAME_TEMP;
        db.execSQL(saveDataSQL);
    }

    void exportSubjectData(File outputFile, String id) throws IOException, SQLException {
        CSVWriter csvWrite = new CSVWriter(new FileWriter(outputFile));

        Cursor curCSV = db.rawQuery("SELECT * FROM " + EVENTS_TABLE_NAME + " WHERE id = " + id, null);

        csvWrite.writeNext(curCSV.getColumnNames());

        int writeCounter = 0;
        int numRows = curCSV.getCount();

        while (curCSV.moveToNext()) {
            writeCounter++;

            String[] arrStr = {curCSV.getString(0), curCSV.getString(1), curCSV.getString(2)};

            csvWrite.writeNext(arrStr);

            if ((writeCounter % 100) == 0) {
                csvWrite.flush();
            }

            Double progressPercent = Math.ceil(((float) writeCounter / (float) numRows) * 100);
            Message msg = Message.obtain();
            msg.obj = progressPercent;
            msg.setTarget(messageHandler);
            msg.sendToTarget();
        }

        csvWrite.close();
        curCSV.close();
    }
}
