//
//  End Tournament Activity - for view and control
//


package edu.ramapo.rpodziel.pente.viewcontrol;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.Vector;

import edu.ramapo.rpodziel.pente.R;
import edu.ramapo.rpodziel.pente.model.GameLog;
import edu.ramapo.rpodziel.pente.model.Player;
import edu.ramapo.rpodziel.pente.model.Round;

public class EndTournamentActivity extends Activity {

    /* Control Functions */

    /**
     * Creates the end tournament activity, setting the content view.
     * @param a_savedInstanceState @param a_savedInstanceState If the activity is being re-initialized after
     *      previously being shut down then this Bundle contains the data it most
     *      recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle a_savedInstanceState) {
        super.onCreate(a_savedInstanceState);
        setContentView(R.layout.activity_end_tournament);

        Round round = (Round) getIntent().getSerializableExtra("ROUND");

        // Show who won the tournament
        DisplayWinner(round.GetHighestScoringPlayer());

        // Show the final scores
        DisplayFinalScores(round.GetPlayers());
    }

    /**
     * Do nothing, user ended game. Going back will result in a message up tournament.
     */
    @Override
    public void onBackPressed() {}

    /**
     * User is finished with the tournament, go back to the launch activity.
     * @param view The view that was clicked, the end tournament button.
     */
    public void EndGame(View view) {
        // User is finished with the tournament, go back to the launch activity
        GameLog.ClearLog();
        navigateUpTo(new Intent(getBaseContext(), LaunchActivity.class));
    }


    /* View Functions */

    /**
     * Display the winner of the tournament.
     * @param a_winner The player who won the tournament.
     */
    private void DisplayWinner(Player a_winner) {
        TextView winnerView = findViewById(R.id.ET_winner);

        // Show who won. If winner is null, then we ended in a tie
        String winnerMessage;
        if (a_winner == null) { winnerMessage = "Tournament ended in a tie!"; }
        else { winnerMessage = a_winner.GetName() + " wins the tournament!"; }

        // Update the view
        winnerView.setText(winnerMessage);
    }

    /**
     * Display the final scores of the tournament.
     * @param a_players The players in the tournament.
     */
    /*
    Algorithm:
        1) Add all the scores from the player to a string to display
        2) Update the view
     */
    private void DisplayFinalScores(Vector<Player> a_players) {
        TextView scores = findViewById(R.id.ET_tournament_scores);

        // Add all the scores from the player to a string to display
        StringBuilder scoresText = new StringBuilder("Final scores\n\n");
        for (Player player : a_players) {
            scoresText.append(player.GetName())
                    .append(": ")
                    .append(player.GetTournamentScore())
                    .append("\n");
        }

        // Update the view
        scores.setText(scoresText.toString());
    }
}