//
//  Another Round Activity if a player wants to play another round when it ends
//

package edu.ramapo.rpodziel.pente.viewcontrol;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import edu.ramapo.rpodziel.pente.R;
import edu.ramapo.rpodziel.pente.model.GameLog;
import edu.ramapo.rpodziel.pente.model.Player;
import edu.ramapo.rpodziel.pente.model.Round;

import java.util.Vector;

public class AnotherRoundActivity extends Activity {

    /* Control Functions */

    /**
     * Creates the another round activity, setting the content view and starting a new round.
     * @param a_savedInstanceState @param a_savedInstanceState If the activity is being re-initialized after
     *      previously being shut down then this Bundle contains the data it most
     *      recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle a_savedInstanceState) {
        super.onCreate(a_savedInstanceState);
        setContentView(R.layout.activity_another_round);

        // Get round from intent
        Round round = (Round) getIntent().getSerializableExtra("ROUND");

        StartNewRound(round);
    }

    /**
     * Do nothing, already reset the board. Going back would result empty board.
     */
    @Override
    public void onBackPressed() { }

    /**
     * Go to the coin toss activity.
     * @param a_view The view that was clicked, the coin toss button.
     */
    public void CoinFlip(View a_view) {
        Round round = (Round) getIntent().getSerializableExtra("ROUND");

        // Go to the coin toss activity
        Intent intent = new Intent(this, CoinTossActivity.class);
        intent.putExtra("ROUND", round);
        startActivity(intent);
    }

    /**
     * Go to the main activity - nothing needs to be done by users.
     * @param a_view The view that was clicked, the sorted button.
     */
    public void Sorted(View a_view) {
        Round round = (Round) getIntent().getSerializableExtra("ROUND");

        // Go to the main activity - nothing needs to be done by users
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("ROUND", round);
        startActivity(intent);
    }


    /* Private Utility Functions */

    /**
     * Reset the entire round (board and captured pairs for players). And clearthe game lgo.
     * @param a_round The round object to reset.
     */
    private void StartNewRound(final Round a_round) {
        // Reset the entire round (board and captured pairs for players)
        GameLog.ClearLog();

        // Display the current scores on the page
        DisplayScores(a_round.GetPlayers());

        // Display who will go next - if tied go to coin toss
        DisplayFirstPlayer(a_round.GetHighestScoringPlayer());

        // Reset previous round and set up new one
        a_round.StartAnotherGame();
    }


    /* View Functions */


    /**
     * Display the current scores on the page.
     * @param a_players The players in the round.
     */
    private void DisplayScores(final Vector<Player> a_players) {
        // TextView for displaying current scores
        TextView scoresView = findViewById(R.id.AR_tournament_scores);

        // Display current tournament scores - so user knows current state of game
        StringBuilder scoresText = new StringBuilder("Current scores\n");
        for (Player player : a_players) {
            // Format: <Name>: <Score>\n<Name>: <Score>
            scoresText.append(player.GetName())
                    .append(": ")
                    .append(player.GetTournamentScore())
                    .append("\n");
        }
        scoresView.setText(scoresText.toString());
    }

    /**
     * Display who will go next - if tied go to coin toss.
     * @param a_firstPlayer The player who will go first.
     */
    private void DisplayFirstPlayer(Player a_firstPlayer) {
        Button button = findViewById(R.id.AR_continue);

        // Holds the message of what will happen next - a coin flip or first player
        String messageText;

        // If the scores are tied, we need a coin flip
        // Set the continue button to go to the coin flip activity
        // If not tied, then just go back to main (board already reset)
        if (a_firstPlayer == null) {
            messageText = "Tournament scores are tied! A coin flip must be played!";
            button.setOnClickListener(this::CoinFlip);
        }
        else {
            messageText = "Next round will start with " + a_firstPlayer.GetName()
                    + " as they have the highest score!";
            button.setOnClickListener(this::Sorted);
        }

        // Update what is happening next
        TextView messageView = findViewById(R.id.AR_next_round);
        messageView.setText(messageText);
    }

}