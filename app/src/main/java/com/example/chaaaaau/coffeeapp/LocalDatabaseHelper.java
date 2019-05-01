package com.example.chaaaaau.coffeeapp;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by User on 8/8/16.
 * http://www.androidhive.info/2013/09/android-sqlite-database-with-multiple-tables/
 */
public class LocalDatabaseHelper extends SQLiteOpenHelper {

    // Logcat tag
    private static final String LOG = LocalDatabaseHelper.class.getName();

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "LocalDB_COFFEE";

    // Table Names
    private static final String TABLE_COFFEE_RECORD = "coffee_record";
    private static final String TABLE_CHART_DATA = "chart_data";

    // Table Create Statements
    // ACCOUNT table create statement

    // RECORD table create statement

    public LocalDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        System.out.println("***** ON CREATE  *****");
    }

    public void DropTable(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COFFEE_RECORD);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHART_DATA);
        System.out.println("***** DROP TABLE in here *****");
        closeDB();
    }

    public void CreateTable(){
        SQLiteDatabase db = this.getWritableDatabase();
        String sql1 ="CREATE TABLE IF NOT EXISTS " +  TABLE_COFFEE_RECORD + " (id NUMBER, name TEXT, overall NUMBER, bitterness NUMBER, concentration NUMBER, acidity NUMBER );";
        String sql2 ="CREATE TABLE IF NOT EXISTS " +  TABLE_CHART_DATA + " (id NUMBER, time NUMBER, temperature NUMBER, inVol NUMBER, outVol NUMBER ,groupID NUMBER);";
        db.execSQL(sql1);
        db.execSQL(sql2);
        System.out.println("***** CREATE TABLE in here *****");
        closeDB();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        ResetDB();
        System.out.println("***** onUpgrade TABLE in here *****");
        // create new tables
        onCreate(db);
    }

    public void ResetDB(){
        try {
            DropTable();
            CreateTable();
        }catch (Exception e){
            Log.e("[Local_DB]","ResetDB");
        }
    }

    public void CleanAllAndResetDB(){
        try {
            DropTable();
            System.out.println("***** DROP some TABLE in here *****");

            CreateTable();
            System.out.println("***** CREATE some TABLE in here *****");
        }catch (Exception e){
            Log.e("[Local_DB]","ResetDB");
        }
        closeDB();
    }


    public String SelectARow(String SQL, String output){
        //System.out.println("Get a row {SQL} = [" + SQL + "]");
        String result = null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = SelectFromLocalDB(SQL);
        if (cursor.moveToFirst()) {
            do {
                result = cursor.getString((cursor.getColumnIndex(output)));
            } while (cursor.moveToNext());
        }
        return result;
    }

    public Cursor SelectFromLocalDB(String SQL){
        System.out.println("{SQL} = [" + SQL + "]");

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(SQL, null);
        return cursor;
    }

    public void ExecuteSQL_Local(String SQL){
        System.out.println("{SQL} = [" + SQL + "]");

        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(SQL);
        System.out.println("***** Execute SQL *****");
    }

    // closing database
    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen())
            db.close();
    }
}
