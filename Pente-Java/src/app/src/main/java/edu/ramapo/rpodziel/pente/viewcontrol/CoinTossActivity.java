//
//  Coin Toss View and Control
//

package edu.ramapo.rpodziel.pente.viewcontrol;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;


import edu.ramapo.rpodziel.pente.R;
import edu.ramapo.rpodziel.pente.model.GameLog;
import edu.ramapo.rpodziel.pente.model.Round;

public class CoinTossActivity extends Activity {

    /* Control Functions */

    /**
     * Creates the coin toss activity, setting the content view.
     * @param a_savedInstanceState @param a_savedInstanceState If the activity is being re-initialized after
     *      previously being shut down then this Bundle contains the data it most
     *      recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle a_savedInstanceState) {
        super.onCreate(a_savedInstanceState);
        setContentView(R.layout.activity_coin_toss);
    }

    /**
     * Resets the round if the user wants to go back to start menu.
     */
    @Override
    public void onBackPressed() {
        Round round = (Round) getIntent().getSerializableExtra("ROUND");

        // Need to reset the round if user wants to go back to start menu
        // If we don't do this then the round gets messed up. Did not disable back
        // button as it is inconvenient  for the user
        round.Reset();
        GameLog.ClearLog();

        super.onBackPressed();
    }


    /**
     * Perform the coin toss and display the result. The big coin toss button is clicked.
     * @param a_view The view that was clicked, the coin toss button.
     */
    /*
    Algorithm:
        1) Get the round object from the intent
        2) Get the user decision based on the toggle button value
        3) Display the the result from the coin toss being preformed
        4) Remove the option to click the toggle decision
     */
    public void CoinToss(View a_view) {
        Round round = (Round) getIntent().getSerializableExtra("ROUND");

        // Get the user decision based on the toggle button value
        ToggleButton toggleButton = findViewById(R.id.CT_decision);
        String userDecision = toggleButton.getText().toString();

        // Display the the result from the coin toss being preformed
        DisplayResult(round.PerformCoinToss(userDecision), round.GetCoinTossResult());

        // Remove the option to click the toggle decision
        toggleButton.setClickable(false);
        // Remove the option to click the coin toss, if they can press again, they
        // then get a second chance to flip it, cheating the game.
        a_view.setClickable(false);
    }

    /**
     * Continue to the main activity, the continue button is clicked.
     * @param a_view The view that was clicked, the continue button.
     */
    public void Continue(View a_view) {
        Round round = (Round) getIntent().getSerializableExtra("ROUND");

        // Go to the main for the board, passing the round object
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("ROUND", round);
        startActivity(intent);
    }


    /* View Functions */

    /**
     * Display the result of the coin toss.
     * @param a_isWinner True if the user won the coin toss, false otherwise.
     * @param a_decision The decision the user made, heads or tails.
     */
    private void DisplayResult(boolean a_isWinner, String a_decision) {
        // Put the result to a string, for user readability
        String message = "Coin landed on " + a_decision + "!\n\n";
        if (a_isWinner) { message +=  "You won the coin toss! You are white and will go first!"; }
        else { message += "You lost the coin toss! You are black and computer will go first."; }

        // Set the result text view screen based on the message
        TextView result = findViewById(R.id.CT_result);
        result.setText(message);

        // Show the continue button to allow the user to go to the game
        Button continueButton = findViewById(R.id.CT_continue);
        continueButton.setVisibility(View.VISIBLE);
    }

}
