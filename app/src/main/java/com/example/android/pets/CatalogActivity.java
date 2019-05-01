package com.example.android.pets;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.android.pets.data.PetContract.PetEntry;
import com.example.android.pets.data.PetDbHelper;

/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity {

    private PetDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        dbHelper = new PetDbHelper(this);
        displayDatabaseInfo();
    }

    /**
     * Temporary helper method to display information in the onscreen TextView about the state of
     * the pets database.
     */
    private void displayDatabaseInfo() {
        // To access our database, we instantiate our subclass of SQLiteOpenHelper
        // and pass the context, which is the current activity.
        PetDbHelper mDbHelper = new PetDbHelper(this);

        // Create and/or open a database to read from it
        SQLiteDatabase db = mDbHelper.getReadableDatabase();


        String[] projection = {
                PetEntry._ID,
                PetEntry.PET_NAME,
                PetEntry.PET_BREED,
                PetEntry.PET_GENDER,
                PetEntry.PET_WEIGHT
        };

        Cursor cursor = db.query(PetEntry.TABLE_NAME, projection, null, null, null, null, null);
        try {
            // Display the number of rows in the Cursor (which reflects the number of rows in the
            // pets table in the database).
            TextView displayView = (TextView) findViewById(R.id.text_view_pet);
            //displayView.setText("Number of rows in pets database table: " + cursor.getCount());

            displayView.setText("");
            while(cursor.moveToNext()){

                int currentID = cursor.getInt(cursor.getColumnIndex(PetEntry._ID));
                String currentName = cursor.getString(cursor.getColumnIndex(PetEntry.PET_NAME));
                String currentBreed = cursor.getString(cursor.getColumnIndex(PetEntry.PET_BREED));
                int currentGender = cursor.getInt(cursor.getColumnIndex(PetEntry.PET_GENDER));
                int currentWeight = cursor.getInt(cursor.getColumnIndex(PetEntry.PET_WEIGHT));
                displayView.append(("\n" + currentID + " - " + currentName + " - " + currentBreed + " - " + currentGender + " - " + currentWeight));

            }
        } finally {
            // Always close the cursor when you're done reading from it. This releases all its
            // resources and makes it invalid.
            cursor.close();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertData();
                displayDatabaseInfo();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                // Do nothing for now
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart(){
        super.onStart();
        displayDatabaseInfo();
    }

    private void insertData(){
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(PetEntry.PET_NAME, "Toby");
        values.put(PetEntry.PET_BREED, "Terrier");
        values.put(PetEntry.PET_GENDER, PetEntry.GENDER_MALE);
        values.put(PetEntry.PET_WEIGHT, 7);

        long newRowID = db.insert(PetEntry.TABLE_NAME, null, values);
    }
}