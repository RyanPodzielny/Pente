//
//  The file select activity is used to select a file to load from
//

package edu.ramapo.rpodziel.pente.viewcontrol;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.io.IOException;
import java.util.ArrayList;


import edu.ramapo.rpodziel.pente.R;
import edu.ramapo.rpodziel.pente.model.Codes;
import edu.ramapo.rpodziel.pente.model.Round;
import edu.ramapo.rpodziel.pente.model.Serialize;

public class FileSelectActivity extends Activity {

    /**
     * Creates the file select activity, setting the content view and showing the available files.
     * @param a_savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle a_savedInstanceState) {
        super.onCreate(a_savedInstanceState);
        setContentView(R.layout.activity_file_select);

        // Show which files the user can select from
        ShowFileSelect();
    }

    /**
     * The resume button on the view, loads the selected file and goes to the main activity.
     * @param a_view The view that was clicked, the resume button.
     * @throws IOException If the file cannot be read.
     */
    /*
    Algorithm:
        1) Get the selected file name
        2) Get round object from intent
        3) Read from the save
        4) If couldn't read - we have a real problem. Go back to launch activity
        5) Go to main activity for the board, once again passing the board
    Assistance Received:
        https://developer.android.com/develop/ui/views/components/spinner
     */
    public void Resume(View a_view) throws IOException {
        // Get the selected file name
        Spinner spinner = findViewById(R.id.FS_available_files);
        String fileName = spinner.getSelectedItem().toString();

        // Get round object from intent
        Round round = (Round) getIntent().getSerializableExtra("ROUND");

        // Read from the save
        Codes.ReturnCode status = Serialize.ReadSave(this, round, fileName);

        // If couldn't read - we have a real problem. Go back to launch activity
        if (status != Codes.ReturnCode.SUCCESS) { finish(); }

        // Go to main activity for the board, once again passing the board
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("ROUND", round);
        startActivity(intent);
    }


    /* View Functions */

    /**
     * Show which files the user can select from.
     */
    /*
    Algorithm:
        1) Get the file names from our intent. Check if its valid is done before hand
        2) Get the spinner of the file drop down
        3) Set the available options in the spinner to the file names
    Assistance Received: https://developer.android.com/develop/ui/views/components/spinner
     */
    private void ShowFileSelect() {
        // Get the file names from our intent. Check if its valid is done before hand
        ArrayList<String> fileNames = (ArrayList<String>) getIntent().getSerializableExtra("FILE_NAMES");

        // Get the spinner of the file drop down
        Spinner spinner = findViewById(R.id.FS_available_files);

        // Set the available options in the spinner to the file names
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                fileNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

}
