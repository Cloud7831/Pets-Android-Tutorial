package com.example.android.pets;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.pets.data.PetContract;
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
        String[] projection = {
                PetEntry._ID,
                PetEntry.PET_NAME,
                PetEntry.PET_BREED,
                PetEntry.PET_GENDER,
                PetEntry.PET_WEIGHT
        };

        Cursor cursor = getContentResolver().query(PetEntry.CONTENT_URI, projection, null, null, null);

        ListView petListView = (ListView) findViewById(R.id.list);

        // Set the emptyView incase there's no data in the cursor to display.
        View emptyView = findViewById(R.id.empty_view);
        petListView.setEmptyView(emptyView);


        PetCursorAdapter adapter = new PetCursorAdapter(this, cursor);

        petListView.setAdapter(adapter);
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
        ContentValues values = new ContentValues();
        values.put(PetEntry.PET_NAME, "Toby");
        values.put(PetEntry.PET_BREED, "Terrier");
        values.put(PetEntry.PET_GENDER, PetEntry.GENDER_MALE);
        values.put(PetEntry.PET_WEIGHT, 7);

        Uri uri = getContentResolver().insert(PetEntry.CONTENT_URI, values);
    }
}