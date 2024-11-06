//
//  The main activity where the user will be for most of the time
//

package edu.ramapo.rpodziel.pente.viewcontrol;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;

import java.util.Objects;
import java.util.Vector;

import edu.ramapo.rpodziel.pente.model.Board;
import edu.ramapo.rpodziel.pente.model.GameLog;
import edu.ramapo.rpodziel.pente.model.Player;
import edu.ramapo.rpodziel.pente.model.Round;

public class MainActivity extends Activity {

    /* Class Constants */

    public static final int TEXT_SIZE = 20;
    // Stores the round object as private field as to not have to pass it around in


    /* Class Variables */

    private static Round m_round;


    /* Control Functions */

    /**
     * Creates the main activity, setting the content view and starting a new round.
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    /*
    Algorithm:
        1) Get round from intent
        2) Start the round and log it
        3) Generate the round display
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Start up the round
        m_round = (Round) Objects.requireNonNull(getIntent().getSerializableExtra("ROUND"));
        GameLog.AddMessage("Round started!");
        m_round.Start();

        GenerateRoundDisplay();
    }

    /**
     * Do nothing, user started a game, going back could end up breaking the game.
     */
    @Override
    public void onBackPressed() { }

    /**
     * Go to the end tournament activity.
     * @param a_view The view that was clicked, the quit button.
     */
    public void Quit(View a_view) {
        // User decided to quit instead of playing another, end the tournament
        Intent intent = new Intent(this, EndTournamentActivity.class);
        intent.putExtra("ROUND", m_round);
        startActivity(intent);
    }

    /**
     * Play another round, go to the another round activity.
     */
    public void PlayAgain(View a_view) {
        // User wants to play another round, reset the board and start a new round
        Intent intent = new Intent(this, AnotherRoundActivity.class);
        intent.putExtra("ROUND", m_round);
        startActivity(intent);
    }

    /**
     * Save the game, go to the serialize activity.
     * @param a_view The view that was clicked, the save button.
     */
    public void SaveGame(View a_view) {
        // User wants to save the game, go to the save activity so they can
        Intent intent = new Intent(this, SerializeActivity.class);
        intent = intent.putExtra("ROUND", m_round);
        startActivity(intent);
    }

    /**
     * Get help from the computer, display the help and highlight the best move.
     * @param a_view The view that was clicked, the help button.
     */
    /*
    Algorithm:
        1) Get the help from the computer
        2) Set the help to what computer suggested
        3) Set the visibility of the help header so user can see it
        4) Get the tag of the best move, so we can highlight it
        5) Remove clicks from the help button so user doesn't press it again
        6) Append help to the log
     */
    public void GetHelp(View a_view) {
        // Get the help from the computer
        Player.ComputerMove help = m_round.GetCurrentPlayer().GetHelp(
                m_round.GetRoundBoard(),
                m_round.GetNextPlayer()
        );

        // Set the help to what computer suggested
        TextView helpView = findViewById(R.id.M_help_text);
        String helpText = help.formattedReason + "\n\nIndicated by the green stone!";
        helpView.setText(helpText);
        helpView.setVisibility(View.VISIBLE);

        // Set the visibility of the help header so user can see it
        TextView helpViewHeader = findViewById(R.id.M_help_header);
        helpViewHeader.setVisibility(View.VISIBLE);

        // Get the tag of the best move, so we can highlight it
        Button bestMoveButton = findViewById(R.id.M_button_container).findViewWithTag(help.position);
        bestMoveButton.setBackground(ResourcesCompat.getDrawable(
                getResources(),
                R.drawable.green_stone,
                null)
        );
        // Remove clicks from the help button so user doesn't press it again
        a_view.setClickable(false);
        // Append help to the log
        AppendCurrentLog();
    }

    /**
     * Place a stone on the board, facilitate the ply and update the display.
     * @param a_view The view that was clicked, the board button.
     */
    /*
    Algorithm:
        1) Get the tag of the button that was clicked
        2) Facilitate the ply
        3) Refresh the display
        4) If the game is over, display the winner
     */
    public void PlaceStone(View a_view) {
        // Get the tag of the button that was clicked
        Object buttonPressed = a_view.getTag();
        String position = "step";
        if (buttonPressed != null) {
            position = buttonPressed.toString();
        }

        boolean isGameOver = m_round.FacilitatePly(position);
        // Refresh the display
        GenerateRoundDisplay();
        // Game is over, display the winner
        if (isGameOver) {
            DisplayRoundEnd(m_round.GetRoundWinner());
        }
    }


    /* View Functions */

    /**
     * Display the winner of the round.
     * @param a_winner The player who won the round.
     */
    /*
    Algorithm:
        1) Disable game board buttons so user can't click them
        2) Set bottom controls to invisible so user is more aware that game ended
        3) Display the winner and end message on the right side
        5) Make the right side visible
     */
    private void DisplayRoundEnd(Player a_winner) {
        // Disable game board buttons
        FrameLayout buttons = findViewById(R.id.M_button_container);
        for (int i = 0; i < buttons.getChildCount(); i++) {
            View child = buttons.getChildAt(i);
            child.setClickable(false);
        }

        // Set bottom controls to invisible
        ConstraintLayout bottomControls = findViewById(R.id.M_user_controller);
        bottomControls.setVisibility(View.INVISIBLE);

        // Display the winner
        TextView winnerView = findViewById(R.id.M_winner_text);
        String winner;
        if (a_winner == null) { winner = "Tie!"; }
        else {winner = a_winner.GetNameAndColor() + " wins!"; }
        winner += " Round is over!";
        winnerView.setText(winner);

        // Display the end message
        TextView endMessageView = findViewById(R.id.M_end_message);
        String endMessageText = "The final score details are above!\n\n" +
                "The score tally results can be found in the game log!\n";
        endMessageView.setText(endMessageText);

        // Make the right side visible
        ConstraintLayout rightSide = findViewById(R.id.M_win_container);
        rightSide.setVisibility(View.VISIBLE);
    }

    /**
     * Generate the round display, displaying the board, scores, and current player.
     */
    private void GenerateRoundDisplay() {
        // Reset the help text
        ResetHelp();
        AppendCurrentLog();
        // Displays the current player's turn
        DisplayTurn(m_round.GetCurrentPlayer());
        DisplayScores(m_round.GetPlayers());
        GenerateBoard();
    }

    /**
     * Reset the help text to invisible if it was visible..
     */
    private void ResetHelp() {
        // Reset the help text
        TextView helpView = findViewById(R.id.M_help_text);
        helpView.setVisibility(View.INVISIBLE);
        TextView helpViewHeader = findViewById(R.id.M_help_header);
        helpViewHeader.setVisibility(View.INVISIBLE);
    }

    /**
     * Append the current log to the game log text view. Scrolling to the bottom so we can see the latest log.
     */
    /*
     Assistance Received:
        https://stackoverflow.com/questions/3506696/auto-scrolling-textview-in-android-to-bring-text-into-view
     */
    private void AppendCurrentLog() {
        // Holds the how long it takes for scroll to come into affect
        final int SCROLL_DELAY = 100;

        // Append the log to the text view - which holds current messages
        TextView logView = findViewById(R.id.M_gamelog_text);
        String logText = GameLog.FormatLog();
        logView.append(logText);

        // Make the scroll to the bottom so we can see the latest log
        ScrollView scrollView = findViewById(R.id.M_gamelog_scroller);
        scrollView.postDelayed(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN),SCROLL_DELAY);

        // Clear so not rewritten all the time
        GameLog.ClearLog();
    }

    /**
     * Display the current player's turn on the view.
     * @param a_currentPlayer The player who's turn it is.
     */
    private void DisplayTurn(Player a_currentPlayer) {
        // Display the current player's turn
        TextView turn = findViewById(R.id.currentmove);
        String turnText = a_currentPlayer.GetNameAndColor() + "'s Turn";
        turn.setTextSize(TEXT_SIZE);
        turn.setText(turnText);
    }

    /**
     * Display the scores of the players in the round. So the user knows the current state of the game.
     * @param a_players
     */
    /*
    Algorithm:
        1) Get the current scores for each player
        2) Display the scores
     */
    private void DisplayScores(Vector<Player> a_players) {
        TextView scores = findViewById(R.id.M_scores);

        // Current scores strings for each player
        StringBuilder scoresText = new StringBuilder("Captured Pairs:\n");
        for (Player player : a_players) {
            scoresText.append('\t')
                    .append(player.GetNameAndColor())
                    .append(": ")
                    .append(player.GetCapturedPairs())
                    .append("\n");
        }

        // Tournament scores strings for each player
        scoresText.append("\nTournament Score:\n");
        for (Player player : a_players) {
            scoresText.append('\t')
                    .append(player.GetNameAndColor())
                    .append(": ")
                    .append(player.GetTournamentScore())
                    .append("\n");
        }

        // Display the scores
        scores.setTextSize(TEXT_SIZE);
        scores.setText(scoresText.toString());
    }

    /**
     * Generate the board display, displaying the board, headers, and buttons.
     */
    /*
    Algorithm:
        1) Generate button size based on the density
        2) Set the board to a certain size - helps with not being squished or stretched
        3) Generate the headers and buttons
    Assistance Received:
        Alan Salanzo on board layout
     */
    private void GenerateBoard() {
        // Generate button size based on the density
        // 40 selected as it makes it nice and visible without taking too much space
        final int BUTTON_SIZE = (int) (40 * this.getResources().getDisplayMetrics().density);
        // Add 25 to account for the headers
        final int BOARD_VIEW_SIZE = Board.BOARD_SIZE * BUTTON_SIZE + 25;

        // Get the overall container
        ConstraintLayout boardView = findViewById(R.id.M_board_container);

        // Set the board to a certain size - helps with not being squished or stretched
        boardView.setMinWidth(BOARD_VIEW_SIZE);
        boardView.setMinHeight(BOARD_VIEW_SIZE);

        // Generate the headers and buttons
        GenerateRowHeader(BUTTON_SIZE);
        GenerateColumnHeader(BUTTON_SIZE);
        GenerateButtons(BUTTON_SIZE);
    }

    /**
     * Generates the column headers (A-S)
     * @param a_buttonSize The size of the buttons in pixels
     */
    /*
    Assistance Received:
        Alan Salzano on board layout
     */
    private void GenerateColumnHeader(int a_buttonSize) {
        LinearLayout topHeader = findViewById(R.id.M_row_header);
        // Remove so we don't have duplicate views
        topHeader.removeAllViews();

        // Generate the column headers (A-S)
        for (char column = 0; column < Board.BOARD_SIZE; column++) {
            TextView letter = new TextView(this);
            letter.setText(String.valueOf((char) (column + Board.COLUMN_OFFSET)));

            letter.setLayoutParams(new LinearLayout.LayoutParams(
                    a_buttonSize,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));

            // Center the text
            letter.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            topHeader.addView(letter);
        }
    }

    /**
     * Generates the row headers (19-1)
     * @param a_buttonSize The size of the buttons in pixels
     */
    /*
    Assistance Received:
        Alan Salzano on board layout
     */
    private void GenerateRowHeader(int a_buttonSize) {
        LinearLayout columnHeader = findViewById(R.id.M_column_header);
        // Remove so we don't have duplicate views
        columnHeader.removeAllViews();

        // Generate the row headers (19-1)
        for (int row = Board.BOARD_SIZE; row > 0; row--) {
            TextView number = new TextView(this);
            number.setText(String.valueOf(row));

            number.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    a_buttonSize
            ));

            // Center the text
            number.setGravity(Gravity.CENTER_VERTICAL);
            columnHeader.addView(number);
        }
    }

    /**
     * Generates the buttons for the board, each button is a
     *      position on the board for the user to click.
     * @param a_buttonSize The size of the buttons in pixels
     */
    /*
    Algorithm:
        1) Get the button container
        2) Remove so we don't have duplicate views multiple times
        3) For every position on the board set the button to a certain image
        4)      If the stone is black, set the button to a black stone
        5)      If the stone is white, set the button to a white stone
        6)      If the stone is a bounds restriction, set the button to a red stone
        7)      If the stone is empty, set the button to a empty stone
    Assistance Received:
        Alan Salzano on board layout
     */
    private void GenerateButtons(int a_buttonSize) {
        FrameLayout buttons = findViewById(R.id.M_button_container);
        // Remove so we don't have duplicate views
        buttons.removeAllViews();

        Button stepButton = findViewById(R.id.M_step);
        Button helpButton = findViewById(R.id.M_help);
        // If the current player requires input, disable the step button, but enable the help
        boolean requiresInput = m_round.GetCurrentPlayer().RequiresInput();
        stepButton.setClickable(!requiresInput);
        helpButton.setClickable(requiresInput);

        // Generator buttons for the board
        Vector<Vector<Character>> gameBoard = m_round.GetRoundBoard().GetGameBoard();
        for (int row = Board.BOARD_SIZE - Board.ROW_OFFSET; row >= 0; row--) {
            for (int column = 0; column < Board.BOARD_SIZE; column++) {
                Button button = new Button(this);
                // Set the tag to the position of the button
                button.setTag(Board.IndicesToString(row, column));

                // Default set to false as we don't want to click on the board
                // Only if you can place and requires input do we want to enable the button
                button.setClickable(false);

                // See if there is a bounds restriction
                int distance = Board.AwayFromCenter(row, column);
                char stone = gameBoard.get(row).get(column);

                // Opted not to seperate the bounds restriction and requires input into a seperate
                // function to make everything more readable. Increases clutter but readability is better.

                // Structure of if else block:
                // Black stone - black button
                // White stone - white button
                // Bounds restriction - red button
                // Requires input - clickable button
                if (stone == Player.BLACK_CHAR) {
                    button.setBackground(ResourcesCompat.getDrawable(
                            getResources(),
                            R.drawable.black_stone,
                            null)
                    );
                }
                else if (stone == Player.WHITE_CHAR) {
                    button.setBackground(ResourcesCompat.getDrawable(
                            getResources(),
                            R.drawable.white_stone,
                            null)
                    );
                }
                else if ((m_round.GetRoundBoard().GetInnerBounds() > distance)
                            || (distance > m_round.GetRoundBoard().GetOuterBounds())) {
                    button.setBackground(ResourcesCompat.getDrawable(
                            getResources(),
                            R.drawable.bounds_restrict,
                            null)
                    );
                }
                else if (requiresInput){
                    button.setClickable(true);
                    // Set the click listen to the PlaceStone method in the controller
                    button.setOnClickListener(this::PlaceStone);
                }

                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                        a_buttonSize,
                        a_buttonSize
                );
                // Add margins to space propely
                params.topMargin = (Board.BOARD_SIZE - Board.ROW_OFFSET - row) * a_buttonSize;
                params.leftMargin = column * a_buttonSize;

                button.setLayoutParams(params);
                buttons.addView(button);
            }
        }
    }

}