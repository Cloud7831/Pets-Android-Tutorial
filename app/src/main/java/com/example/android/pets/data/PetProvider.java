package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

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
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = matcher.match(uri);
        switch (match) {
            case PETS:

                if(sanityCheck(contentValues)){
                    return insertPet(uri, contentValues);
                }
                else{
                    throw new IllegalArgumentException("Insertion values not accepted");
                }

            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a pet into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertPet(Uri uri, ContentValues values) {

        SQLiteDatabase database = db.getWritableDatabase();

        long id = database.insert(PetEntry.TABLE_NAME, null, values);

        if (id==1){
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = db.getWritableDatabase();

        final int match = matcher.match(uri);
        switch (match) {
            case PETS:
                // Delete all rows that match the selection and selection args
                return database.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
            case PET_ID:
                // Delete a single row given by the ID in the URI
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return database.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = matcher.match(uri);
        if(contentValues.size() == 0){
            // No reason to try to update if there's nothing to update.
            return 0;
        }
        switch (match) {
            case PETS:
                return updatePet(uri, contentValues, selection, selectionArgs);
            case PET_ID:
                // For the PET_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updatePet(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update pets in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more pets).
     * Return the number of rows that were successfully updated.
     */
    private int updatePet(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        updateSanityCheck(values);

        SQLiteDatabase database = db.getWritableDatabase();

        int rowsAltered = database.update(PetEntry.TABLE_NAME, values, selection, selectionArgs);
        return rowsAltered;
    }

    @Override
    public String getType(Uri uri) {
        final int match = matcher.match(uri);
        switch (match) {
            case PETS:
                return PetEntry.CONTENT_LIST_TYPE;
            case PET_ID:
                return PetEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    public boolean sanityCheck(ContentValues values){

        if(values.getAsString(PetEntry.PET_NAME) == null){
            throw new IllegalArgumentException("Pet requires a name.");
        }

        Integer gender = values.getAsInteger(PetEntry.PET_GENDER);
        if(gender == null || !PetEntry.isValidGender(gender)){
            throw new IllegalArgumentException("Pet gender is not one of the accepted values.");
        }

        Integer weight = values.getAsInteger(PetEntry.PET_WEIGHT);
        if(weight != null && weight < 0){
            throw new IllegalArgumentException("Pet weight can't be negative.");
        }

        return true;
    }

    public boolean updateSanityCheck(ContentValues values){

        if(values.containsKey(PetEntry.PET_WEIGHT)){
            Integer weight = values.getAsInteger(PetEntry.PET_WEIGHT);
            if(weight != null && weight < 0){
                throw new IllegalArgumentException("Pet weight can't be negative.");
            }
        }
        if(values.containsKey(PetEntry.PET_GENDER)){
            Integer gender = values.getAsInteger(PetEntry.PET_GENDER);
            if(gender == null || !PetEntry.isValidGender(gender)){
                throw new IllegalArgumentException("Pet gender is not one of the accepted values.");
            }
        }
        if(values.containsKey(PetEntry.PET_NAME)){
            if(values.getAsString(PetEntry.PET_NAME) == null){
                throw new IllegalArgumentException("Pet requires a name.");
            }
        }

        return true;
    }
}
