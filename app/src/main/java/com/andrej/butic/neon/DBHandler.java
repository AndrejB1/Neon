package com.andrej.butic.neon;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

// Use SQLite to set up a table, containing all the favorite venues of the user.
public class DBHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION =1;
    private static final String DATABASE_NAME ="favorites.db";
    public static final String TABLE_FAVORITES ="favorites";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME= "venueName";

    public DBHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_FAVORITES + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT " + "," +
                COLUMN_NAME + " TEXT" +
                ");";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS" + TABLE_FAVORITES);
        onCreate(db);
    }

    /* Add a venue to favorites. Done by turning on the toggle button in ExpandableListAdapter's group view. */
    public void addVenue(Venue venue){
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, venue.name);
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_FAVORITES + " WHERE " + COLUMN_NAME + "='" + venue.name + "';";
        Cursor c = db.rawQuery(query, null);
        if(c.getCount()>0){
            Log.d("WARNING", venue.name + " is already favorited");
        }
        else{
            db.insert(TABLE_FAVORITES, null, values);
        }
        db.close();
    }

    /* Delete a venue from favorites. Done by turning off the toggle button in ExpandableListAdapter's group view. */
    public void deleteVenue(String venueName){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_FAVORITES + " WHERE " + COLUMN_NAME + "=\"" + venueName + "\";");
    }

    /* Method to check if the Venue selected has been favorited by the user. */
    public boolean checkIfFavorite(String venueName){
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_FAVORITES + " WHERE " + COLUMN_NAME + "='" + venueName + "';";
        Cursor c = db.rawQuery(query, null);
        if(c.getCount()>0){
            return true;
        }
        else{
            return false;
        }
    }

    /* Optional method purely for Android Studio's logcat, to check if this class is functioning as intended
     * by printing out the current contents of the 'Favorites Table'. Is not called by any method presently.
     */
    public String databaseToString(){
        String dbString= "";
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_FAVORITES + " WHERE 1";

        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();
        while(!c.isAfterLast()){
            if(c.getString(c.getColumnIndex("venueName"))!= null){
                dbString += c.getString(c.getColumnIndex("venueName"));
                dbString += "\n";
            }
            c.moveToNext();
        }
        db.close();
        return dbString;
    }
}
