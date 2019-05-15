/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.pets;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.pets.data.PetContract.PetEntry;
import com.example.android.pets.data.PetDbHelper;

/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_PET_LOADER = 0; // CursorLoader ID number

    /** EditText field to enter the pet's name */
    private EditText mNameEditText;

    /** EditText field to enter the pet's breed */
    private EditText mBreedEditText;

    /** EditText field to enter the pet's weight */
    private EditText mWeightEditText;

    /** EditText field to enter the pet's gender */
    private Spinner mGenderSpinner;

    private Uri currentPetUri;

    private boolean petHasChanged = false;

    private boolean editMode = false; // True if we're in edit mode, false if we're in add mode.

    /** dbHelper for getting a readable or writable database */
    private PetDbHelper dbHelper;

    /**
     * Gender of the pet. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
     */
    private int mGender = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        currentPetUri = intent.getData();

        if (currentPetUri == null){
            // This is a new pet, so change the app bar to say "Add a Pet".
            setTitle(getString(R.string.editor_activity_title_new_pet));
            editMode = false;

            // It doesn't make sense to delete a pet so we can hide that option.
            invalidateOptionsMenu();
        }
        else{
            // This is an existing pet, so change the app bar to say "Edit Pet".
            setTitle("Edit Pet");
            editMode = true;
            getSupportLoaderManager().initLoader(EXISTING_PET_LOADER, null, this);
        }


        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);

        // Set the OnTouchListener so we know if they've modified data.
        mNameEditText.setOnTouchListener(touchListener);
        mBreedEditText.setOnTouchListener(touchListener);
        mWeightEditText.setOnTouchListener(touchListener);
        mGenderSpinner.setOnTouchListener(touchListener);



        dbHelper = new PetDbHelper(this);

        setupSpinner();
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = PetEntry.GENDER_MALE;
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = PetEntry.GENDER_FEMALE;
                    } else {
                        mGender = PetEntry.GENDER_UNKNOWN;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = 0; // Unknown
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save the pet data to the database.
                if(savePet()){
                    finish();
                }

                // Exit the EditorActivity and go back to the CatalogActivity
                return true;

            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Do nothing for now
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the pet hasn't changed, continue with navigating up to parent activity
                if(!petHasChanged){
                    // Navigate back to parent activity (CatalogActivity)
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }

                // There are unsaved changes potentially so warn the user.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean savePet(){

        ContentValues values = new ContentValues();

        String nameString = mNameEditText.getText().toString().trim();
        String weightString = mWeightEditText.getText().toString().trim();

        if(!TextUtils.isEmpty(weightString)){
            values.put(PetEntry.PET_WEIGHT,     Integer.parseInt(weightString));
        }
        else{
            // Default value for weight is 0.
            values.put(PetEntry.PET_WEIGHT,     0);
        }

        if(TextUtils.isEmpty(nameString)){
            Toast.makeText(this, "You cannot save a pet without a name.", Toast.LENGTH_SHORT).show();
            return false;
        }

        values.put(PetEntry.PET_NAME,       mNameEditText.getText().toString().trim());
        values.put(PetEntry.PET_BREED,      mBreedEditText.getText().toString().trim());
        values.put(PetEntry.PET_GENDER,     mGender);

        if(editMode){
            // Edit the details of a pet in the database
            int rowsAffected = getContentResolver().update(currentPetUri, values, null, null);

            if(rowsAffected == 0){
                Toast.makeText(this, "Update pet failed", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, "Update Pet successful", Toast.LENGTH_SHORT).show();
            }
        }
        else if(editMode == false){
            // Add the pet to the database

            Uri uri = getContentResolver().insert(PetEntry.CONTENT_URI, values);

            if(uri == null){
                Toast.makeText(this, "Error with saving pet data.", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, "Pet saved successfully.", Toast.LENGTH_SHORT).show();
            }
        }

        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                PetEntry._ID,
                PetEntry.PET_NAME,
                PetEntry.PET_BREED,
                PetEntry.PET_GENDER,
                PetEntry.PET_WEIGHT
        };

        return new CursorLoader(this,   // Parent activity context
                currentPetUri,                  // Query the content URI for the current pet
                projection,                     // Columns to include in the resulting Cursor
                null,                  // No selection clause
                null,               // No selection arguments
                null                   // Default sort order
                );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data.moveToFirst()){
            // Extract out the value from the Cursor for the given column index.
            String name = data.getString(data.getColumnIndex(PetEntry.PET_NAME));
            String breed = data.getString(data.getColumnIndex(PetEntry.PET_BREED));
            int gender = data.getInt(data.getColumnIndex(PetEntry.PET_GENDER));
            int weight = data.getInt(data.getColumnIndex(PetEntry.PET_WEIGHT));

            // Update the TextViews in the Editor to the data retreived.
            mNameEditText.setText(name);
            mBreedEditText.setText(breed);
            mWeightEditText.setText(Integer.toString(weight));

            switch(gender){
                case PetEntry.GENDER_MALE:
                    mGenderSpinner.setSelection(1);
                    break;
                case PetEntry.GENDER_FEMALE:
                    mGenderSpinner.setSelection(2);
                    break;
                default:
                    mGenderSpinner.setSelection(0);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }


    private View.OnTouchListener touchListener = new View.OnTouchListener(){
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent){
            petHasChanged = true;
            return false; // What does return false do?
        }
    };

    private void showUnsavedChangesDialog(DialogInterface .OnClickListener discardButtonClickListener){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Discard your changes and quit editing?");
        builder.setPositiveButton("Discard", discardButtonClickListener);
        builder.setNegativeButton("Keep editing", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int id){
                // User clicked the "Keep editing" button, so dismiss the dialog and continue editing
                if(dialog != null){
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed(){
        // If the pet hasn't changed, continue with handling back button press
        if(!petHasChanged){
          super.onBackPressed();
          return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        super.onPrepareOptionsMenu(menu);

        // If this is a new pet, hide the "Delete" menu item.

        if(!editMode){
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }
}