//
//  Activity for serializing the game
//

package edu.ramapo.rpodziel.pente.viewcontrol;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import edu.ramapo.rpodziel.pente.R;
import edu.ramapo.rpodziel.pente.model.Codes;
import edu.ramapo.rpodziel.pente.model.GameLog;
import edu.ramapo.rpodziel.pente.model.Round;
import edu.ramapo.rpodziel.pente.model.Serialize;

public class SerializeActivity extends Activity {

    /* Control Functions */

    /**
     * Creates the serialize activity, setting the content view.
     * @param a_savedInstanceState @param a_savedInstanceState If the activity is being re-initialized after
     *      previously being shut down then this Bundle contains the data it most
     *      recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle a_savedInstanceState) {
        super.onCreate(a_savedInstanceState);
        setContentView(R.layout.activity_serialize);
    }

    /**
     * The save game button on the view, saves the game and goes back to the main activity.
     * @param a_view The view that was clicked, the save game button.
     */
    /*
    Algorithm:
        1) Get the round object from the intent
        2) Grab the file name from the text field
        3) If user did not enter a file name, we can't save - do nothing wait for input again
        4) Write the save based on the file name
        5) If we couldn't save - display the error message
        6) Go back to main activity
     */
    public void SaveGame(View a_view) {
        Round round = (Round) getIntent().getSerializableExtra("ROUND");

        // Grab the file name from the text field
        EditText input = findViewById(R.id.S_filename);
        String fileName = input.getText().toString();

        // If user did not enter a file name, we can't save - do nothing wait for input again
        if (fileName.isEmpty()) { return; }

        // Write the save based on the file name
        Codes.ReturnCode status = Serialize.WriteSave(this, round, fileName);
        // If we couldn't save - display the error message
        if (status != Codes.ReturnCode.SUCCESS) {
            TextView error = findViewById(R.id.S_serialize_error);
            error.setText(Codes.GetMessage(status));
            return;
        }

        // Go back to main activity
        navigateUpTo(new Intent(getBaseContext(), LaunchActivity.class));
    }

    /**
     * The quit without saving button on the view, goes back to the main activity.
     * @param a_view The view that was clicked, the quit without saving button.
     */
    public void QuitNoSave(View a_view) {
        // User did not want to save, go back to main activity
        GameLog.ClearLog();
        navigateUpTo(new Intent(getBaseContext(), LaunchActivity.class));
    }

    /**
     * The cancel button on the view, goes back to the main activity.
     * @param a_view The view that was clicked, the cancel button.
     */
    public void Cancel(View a_view) {
        // User did not want to save, go back to main activity
        finish();
    }

}