package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import com.example.android.pets.data.PetContract.PetEntry;

public class PetProvider extends ContentProvider {

    private static final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int PETS = 100;
    private static final int PET_ID = 101;
    static {
        /*
         * The calls to addURI() go here for all of the content URI patterns that the provider should recognize.
         */
        matcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS, PETS);
        matcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS + "/#", PET_ID);

    }


    public static final String LOG_TAG = PetProvider.class.getSimpleName();
    private PetDbHelper db;

    @Override
    public boolean onCreate(){
        db = new PetDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder){
            // Get readable database
            SQLiteDatabase database = db.getReadableDatabase();

            // This cursor will hold the result of the query
            Cursor cursor = null;

            // Figure out if the URI matcher can match the URI to a specific code
            int match = matcher.match(uri);
            switch (match) {
                case PETS:
                    // For the PETS code, query the pets table directly with the given
                    // projection, selection, selection arguments, and sort order. The cursor
                    // could contain multiple rows of the pets table.

                    cursor = database.query(PetEntry.TABLE_NAME, projection, selection, selectionArgs,
                            null, null, sortOrder);

                    break;
                case PET_ID:
                    // For the PET_ID code, extract out the ID from the URI.
                    // For an example URI such as "content://com.example.android.pets/pets/3",
                    // the selection will be "_id=?" and the selection argument will be a
                    // String array containing the actual ID of 3 in this case.
                    //
                    // For every "?" in the selection, we need to have an element in the selection
                    // arguments that will fill in the "?". Since we have 1 question mark in the
                    // selection, we have 1 String in the selection arguments' String array.
                    selection = PetEntry._ID + "=?";
                    selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                    // This will perform a query on the pets table where the _id equals 3 to return a
                    // Cursor containing that row of the table.
                    cursor = database.query(PetEntry.TABLE_NAME, projection, selection, selectionArgs,
                            null, null, sortOrder);
                    break;
                default:
                    throw new IllegalArgumentException("Cannot query unknown URI " + uri);
            }
            return cursor;
    }

    @Override
    public String getType(Uri uri){
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues){
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs){
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs){
        return 0;
    }

}
