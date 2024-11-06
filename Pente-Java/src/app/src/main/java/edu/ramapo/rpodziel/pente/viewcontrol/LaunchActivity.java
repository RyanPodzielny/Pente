//
//  The launch activity - first activity the user sees. Essentially the home screen.
//

package edu.ramapo.rpodziel.pente.viewcontrol;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

import edu.ramapo.rpodziel.pente.R;
import edu.ramapo.rpodziel.pente.model.Round;
import edu.ramapo.rpodziel.pente.model.Serialize;

// Help received for all activity classes:
// Intents: https://developer.android.com/guide/components/intents-filters
// Click Listeners: https://developer.android.com/reference/android/view/View.OnClickListener
// Visibility: https://developer.android.com/reference/android/opengl/Visibility
// MVC: https://www.geeksforgeeks.org/mvc-model-view-controller-architecture-pattern-in-android-with-example/
// A few questions or issues were handled by the editor itself, suggesting syntax warnings
// I've tried to mention where to the best of my ability
public class LaunchActivity extends Activity {

    /* Private Members */

    // Holds the round object for the entire game
    private Round m_round;


    /* Control Functions */

    /**
     * Creates the launch activity, and round object.
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up the round
        m_round = new Round();
        setContentView(R.layout.activity_launch);
    }

    /**
     * Do nothing. User cannot escape from the launch. And if we quit else
     *      where in the game, we want to ensure user can go back to that state
     */
    @Override
    public void onBackPressed() { }

    /**
     * User wants a new game, we need to head to the coin toss to select first player
     * @param a_view The view that was clicked, the new game button.
     */
    public void NewGame(View a_view)  {
        // User wants a new game, we need to head to the coin toss to select first player
        Intent intent = new Intent(this, CoinTossActivity.class);
        intent.putExtra("ROUND", m_round);
        startActivity(intent);
    }

    /**
     * User wants to resume a game, we need to head to the file select activity
     * @param a_view The view that was clicked, the resume game button.
     */
    public void ResumeGame(View a_view) {
        // Get the file names from Serialize
        ArrayList<String> fileNames = Serialize.ReadFileNames(this);

        // If we could not retrieve the files, that is our saves file is empty, let the user
        // know so they can either import a save or start a new game.
        if (fileNames.isEmpty()) {
            DisplayNoFiles();
            return;
        }

        // Go to FileSelectActivity when resuming
        Intent intent = new Intent(this, FileSelectActivity.class);
        // Passing the fileNames to make it easier for the file select
        intent.putExtra("FILE_NAMES", fileNames);
        intent.putExtra("ROUND", m_round);
        startActivity(intent);
    }


    /* View Functions */

    /**
     * No files to resume from, let the user know with an updated view
     */
    private void DisplayNoFiles() {
        TextView error = findViewById(R.id.L_no_files);
        String message = "No files to resume from!";
        error.setText(message);
    }
}