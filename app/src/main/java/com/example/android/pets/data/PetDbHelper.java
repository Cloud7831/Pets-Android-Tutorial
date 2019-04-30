package com.example.android.pets.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.android.pets.data.PetContract.PetEntry;

public class PetDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "shelter.db";

    public PetDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        String SQL_CREATE_PETS_TABLE = "CREATE TABLE "  + PetEntry.TABLE_NAME + "("
                                                        + PetEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                                                        + PetEntry.PET_NAME + " TEXT NOT NULL, "
                                                        + PetEntry.PET_BREED + " TEXT, "
                                                        + PetEntry.PET_GENDER + " INTEGER NOT NULL, "
                                                        + PetEntry.PET_WEIGHT + " INTEGER NOT NULL DEFAULT 0);";
        db.execSQL(SQL_CREATE_PETS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1){

    }


}
