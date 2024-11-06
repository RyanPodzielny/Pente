//
//  Round implementation of Pente
//

package edu.ramapo.rpodziel.pente.model;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Vector;

public class Round implements Serializable {
    /* Class Constants */

    public static final int WIN_SCORE = 5;
    public static final int STRAIGHT_STONES = 4;

    public static final boolean DEFAULT_SERIALIZED = false;
    public static final int DEFAULT_START_INDEX = 0;
    public static final int DEFAULT_PLY_COUNT = 0;

    public static final int DEFAULT_WIN_SCORE = 0;

    // Colors in order of which goes first
    public static char[] COLOR_PRECEDENCE = {'W', 'B'};
    // Number of players - should be used in lock step with COLOR_PRECEDENCE
    public static final int NUM_PLAYERS = 2;


    /* Class Variables */

    // Need to store human and computer separately as
    // we need to do a coin toss setting human to a specific color
    // This is mainly a problem with being a 2 player game
    private Human m_human;
    private Computer m_computer;
    // Stores the players in order of which goes first
    private Vector<Player> m_players;

    // Holds the result of the coin toss (heads or tails)
    private String m_coinTossResult;
    private int m_plyCount;

    // Holds the current player index
    private int m_currPlayerIndex;
    // Holds the winner of the game
    private Player m_winner;

    // Board information
    private Board m_board;
    private boolean m_isSerializedGame;
    private int m_numWinInARow;


    /* Constructors */

    public Round() {
        m_human = new Human();
        m_computer = new Computer();

        // Add the players to the vector
        m_players = new Vector<Player>();
        m_players.add(m_human);
        m_players.add(m_computer);

        m_currPlayerIndex = DEFAULT_START_INDEX;
        m_winner = null;
        m_board = new Board();
        m_isSerializedGame = DEFAULT_SERIALIZED;
        m_numWinInARow = DEFAULT_WIN_SCORE;

        m_coinTossResult = null;
        m_plyCount = DEFAULT_PLY_COUNT;
    }


    /* Accessors */

    /**
     * To get a copy of the round's current board state
     * @return The current round's board, a Board object copy
     */
    public Board GetRoundBoard() {
        return m_board.clone();
    }
    public Vector<Player> GetPlayers() {
        // Create deep copy of players
        Vector<Player> players = new Vector<Player>();
        for (Player player : m_players) {
            players.add(player.clone());
        }
        return players;
    }

    /**
     * To get the player with the highest tournament score
     * @return A Player object, the player with the highest tournament score
     */
    /*
     Assistance Received: For sorting the vector of players
        https://docs.oracle.com/javase/8/docs/api/java/util/Vector.html#sort-java.util.Comparator-
     */
    public Player GetHighestScoringPlayer() {
        // Get a copy of the players - don't want to score
        Vector<Player> players = GetPlayers();

        // Sort based on highest score
        players.sort(Comparator.comparingInt(Player::GetTournamentScore).reversed());

        boolean isTied = false;
        // Check to see if there is a tie - see if all players have the same score
        // If it is, return null to let caller know no player has the highest score
        for (int i = 0; i < players.size() - 1; i++) {
            isTied = players.get(i).GetTournamentScore() == players.get(i + 1).GetTournamentScore();
        }
        // If there is a tie, return null to let caller know no player has the highest score
        if (isTied) {
            return null;
        }

        return players.get(0);
    }

    /**
     * To get the winner of the round
     * @return A Player object, the winner of the round
     */
    public Player GetRoundWinner() {
        if (m_winner == null) {
            return null;
        }
        return m_winner.clone();
    }

    /**
     * To get the current player who's ply it is
     * @return A copy of Player object, the current player who's ply it is
     */
    public Player GetCurrentPlayer() {
        return m_players.get(m_currPlayerIndex).clone();
    }

    /**
     * To get the human player
     * @return A copy of the human player - a Player object
     */
    public Player GetHuman() {
        return m_human.clone();
    }

    /**
     * To get the computer player
     * @return A copy of the computer player - a Player object
     */
    public Player GetComputer() {
        return m_computer.clone();
    }

    /**
     * To get the result of the coin toss
     * @return A String, the result of the coin toss. Either null, HEADS or TAILS
     */
    public String GetCoinTossResult() {
        return m_coinTossResult;
    }

    /**
     * To get the next player who's ply it is
     * @return A copy of Player object, the next player who's ply it is
     */
    public Player GetNextPlayer() {
        return m_players.get((m_currPlayerIndex + 1) % NUM_PLAYERS).clone();
    }


    /* Mutators */

    /**
     * To start a brand new round
     * @return A Codes.ReturnCode, the status of the start
     */
    public Codes.ReturnCode Start() {
        // Just need to set where the first move can be placed, i.e. bounds
        SetBoardRestriction();
        return Codes.ReturnCode.SUCCESS;
    }

    /**
     * Facilitates the coin toss for human when tournament scores are tied
     * @param a_decision A String, the human's decision for the coin toss
     * @return a boolean, true if human won the coin toss, false otherwise
     */
    /*
    Algorithm:
        1) Get the human's decision
        2) Perform the coin toss
        3) If human won, set the human to go first
        4) If computer won, set the computer to go first
        5) Set the colors based on who goes first
        6) Return if human won the coin toss
     Assistance Received: For a random number
        https://www.geeksforgeeks.org/generating-random-numbers-in-java/
     */
    public boolean PerformCoinToss(String a_decision) {
        final String HEADS = "HEADS"; final String TAILS = "TAILS";
        a_decision = a_decision.toUpperCase();

        // If random int is 0 then heads, else tails
        m_coinTossResult = ((int) Math.round(Math.random()) == 0) ? HEADS : TAILS;
        GameLog.AddMessage("The coin landed on " + m_coinTossResult + "!");

        // Holds if human was the winner
        boolean isWinner = false;
        if (m_coinTossResult.equals(a_decision)) {
            // Human goes first
            isWinner = true;

            GameLog.AddMessage("You won the coin toss! You are white and will go first.");
            m_players.set(0, m_human);
            m_players.set(1, m_computer);
        }
        else {
            // Computer goes first
            GameLog.AddMessage("You lost the coin toss! You are black and computer will go first.");
            m_players.set(0, m_computer);
            m_players.set(1, m_human);
        }

        // Set colors based on who goes first
        for (int i = 0; i < m_players.size(); i++) {
            m_players.get(i).SetColor(COLOR_PRECEDENCE[i]);
        }

        // Format game log
        GameLog.AddMessage("");

        return isWinner;
    }

    /**
     * To set the round's state from a serialized game
     * @param a_board The board to set the round's board to
     * @param a_players The players to set the round's players to, the players of the serialized game
     * @param a_human The human to set the round's human to
     * @param a_computer The computer to set the round's computer to
     * @return A Codes.ReturnCode enum value, indicating where the round's
     *     state was set successfully
     */
    /*
    Algorithm:
        1) Ensure the players are valid, i.e. not null, not the same player,
            and have different colors
        2) Set the round's state from the serialized game
        3) Let the object know we are playing a serialized game
     */
    public Codes.ReturnCode SetGameState(final Board a_board, final Vector<Player> a_players,
                                         Human a_human, Computer a_computer) {
        // Players cannot be null, nor can they be the same player - can't play a game with yourself
        // Must have different colors so ply's make sense, and be no greater than our number of players
        // in our current implementation
        if (a_players.size() != 2) {
            return Codes.ReturnCode.INVALID_PLAYER;
        }
        for (int i = 0; i < a_players.size(); i++) {
            if (a_players.get(i) == null) {
                return Codes.ReturnCode.NULL_PLAYER;
            }
            for (int j = i + 1; j < a_players.size(); j++) {
                if (a_players.get(i) == a_players.get(j)) {
                    return Codes.ReturnCode.SAME_COLOR;
                }
            }
        }

        // Update the round's state from the serialized game
        m_board = a_board;
        m_players = a_players;
        m_human = a_human;
        m_computer = a_computer;

        // Let the round we are playing a serialized game
        m_isSerializedGame = true;
        m_currPlayerIndex = DEFAULT_START_INDEX;

        m_plyCount = DeterminePly();

        return Codes.ReturnCode.SUCCESS;
    }

    /**
     * To facilitate a ply, i.e. ask the current player to make a move
     *     incrementing their captured pairs if they captured if ply resulted in one
     * @param a_position A String, the position to place the stone at
     * @return A boolean, true if the ply resulted in an endgame, false otherwise
     */
    /*
    Algorithm:
        1) Ask the current player to make a move
        2) Output the results of what the ply did to the board, and what the
            current scores are
        3) See if there is a winner, or if the board is full
        4) Output the scores if there are any
     */
    public boolean FacilitatePly(String a_position) {
        // Get the player who's turn it is
        Player currPlayer = m_players.get(m_currPlayerIndex);

        // Set the input for the player
        currPlayer.SetInput(a_position);

        // Log the move
        GameLog.AddMessage(currPlayer.GetNameAndColor() + "'s turn:");
        Codes.ReturnCode status = currPlayer.MakeMove(m_board, GetNextPlayer());
        if (status != Codes.ReturnCode.SUCCESS) {
            GameLog.AddMessage(Codes.GetMessage(status));
            return false;
        }

        GameLog.AddMessage(currPlayer.GetNameAndColor() + ", placed a stone at "
                + m_board.GetLastPosition() + "!");

        // Add the captured pairs to the player
        currPlayer.IncCapturedPairs(m_board.GetCapturedPairs());

        // Log the captured pairs
        if (m_board.GetCapturedPairs() > 0) {
            GameLog.AddMessage(currPlayer.GetNameAndColor() + ", captured "
                    + m_board.GetCapturedPairs() + " pair(s)!");
        }

        // See if there is a winner, or if the board is full
        if(CheckRoundEnd(currPlayer)) {
            TallyScores();
            return true;
        }

        m_plyCount++;
        // Set the b restriction if there is one based on updated ply count
        SetBoardRestriction();
        // Update who' s turn it is
        m_currPlayerIndex = NextPlayerIndex(m_currPlayerIndex);

        // Log the end scores
        RecordEndPly();

        return false;
    }

    /**
     * To start another game from one already played, i.e. reset the round
     * @return A Codes.ReturnCode, the status of the reset
     */
    public Codes.ReturnCode StartAnotherGame() {
        // Reset the current round
        this.Reset();
        // Set the ply order
        SetPlyOrder();
        return Codes.ReturnCode.SUCCESS;
    }

    /**
     * To reset the round's state, i.e. start a new round
     * @return A Codes.ReturnCode enum value, indicating where the round's
     *     state was reset successfully
     */
    /*
    Algorithm:
        1) Reset colors and captured pairs for each player
        2) Reset the board
     */
    public Codes.ReturnCode Reset() {
        // Reset the players for our new round
        for (Player player : m_players) {
            player.ResetCapturedPairs();
            player.SetColor(Player.DEFAULT_COLOR);
        }

        m_board = new Board();

        // Let the round know we are not playing a serialized game
        m_isSerializedGame = DEFAULT_SERIALIZED;
        m_currPlayerIndex = DEFAULT_START_INDEX;

        m_plyCount = DEFAULT_PLY_COUNT;
        m_coinTossResult = null;

        m_numWinInARow = DEFAULT_WIN_SCORE;
        m_winner = null;

        return Codes.ReturnCode.SUCCESS;
    }


    /* Main for Debugging */
    public static void main(String[] args) { }


    /* Private Utility Functions */

    /**
     * To sort the players in order of who goes first by tournament score
     */
    /*
    Algorithm:
        1) Sort the players in order of who goes first by tournament score
        2) Update the players colors accordingly based on the color precedence
    Assistance Received: For sorting the vector of players
        https://docs.oracle.com/javase/8/docs/api/java/util/Vector.html#sort-java.util.Comparator-
     */
    private void SetPlyOrder() {
        // Whoever has more tournament points goes first
        // Arrange the players in order of who goes first by tournament score
        // The player with the highest tournament score goes first, sort to get ply order
        m_players.sort(Comparator.comparingInt(Player::GetTournamentScore).reversed());

        // Update the players colors accordingly based on the color precedence
        for (int currPlayer = 0; currPlayer < NUM_PLAYERS; currPlayer++) {
            m_players.get(currPlayer).SetColor(COLOR_PRECEDENCE[currPlayer]);
        }
        // Let the user know who goes first
        GameLog.AddMessage(m_players.get(0).GetName() + " goes first as they have the highest tournament score with "
                + m_players.get(0).GetTournamentScore() + " points");

        // Set colors based on who goes first
        for (int i = 0; i < m_players.size(); i++) {
            m_players.get(i).SetColor(COLOR_PRECEDENCE[i]);
        }
    }

    /**
     * To estimate the ply of the game, i.e. how many moves have been
     *     made. This is used for when board is serialized, as we need to know
     *     if we need to set a board restriction or not.
     * @return The ply estimate of the game, an integer
     */
    private int DeterminePly() {
        final int MIN_PLY = 3;

        // If there are any captured pairs we know we are past the first 3 plys
        for (Player player : m_players) {
            if (player.GetCapturedPairs() > 0) {
                return MIN_PLY;
            }
        }

        // Return total pieces on board - rough estimate
        // In actuality we just need to know if it's less than 3 for setting bounds
        return (Board.BOARD_SIZE * Board.BOARD_SIZE) - m_board.GetIntersectLeft() ;
    }

    /**
     * To set the board restriction, i.e. where a player can place
     *     their stone, based on the ply count
     */
    /*
    Algorithm:
        1) Set the board restriction based on the plyS count
        2) Output where the players can move
     */
    private void SetBoardRestriction() {
        // Formatting
        GameLog.AddMessage("");

        // Output here to avoid complex output functions
        switch (m_plyCount) {
            // First move must be placed in the center of the board
            case 0:
                m_board.SetBounds(0, 0);
                GameLog.AddMessage("First white move must be placed on the center of the board at "
                        + Board.CENTER_POSITION + "!");
                break;
            // Second move must be placed at least 3 stones away from the center of the board
            case 2:
                m_board.SetBounds(3, Board.BOARD_SIZE);
                GameLog.AddMessage("Second white move must be placed at least 3 stones away from the center of the board at "
                        + Board.CENTER_POSITION + "!");
                break;
            // All other moves have no restrictions
            default:
                m_board.SetBounds(0, Board.BOARD_SIZE);
                break;
        }
        // Formatting
        GameLog.AddMessage("");

    }

    /**
     * To get the index of the next player, i.e. the player whose ply
     *     it is to make a move
     * @param a_playerIndex an integer, the index of the current player
     * @return The index of the next player, an integer
     */
    private int NextPlayerIndex(int a_playerIndex) {
        return (a_playerIndex + 1) % NUM_PLAYERS;
    }

    /**
     * To check if the round has ended, i.e. if there is a winner or
     *     if the board is full
     * @param a_currPlayer a reference to a Player object, the current player whose
     *         ply it is to make a move
     * @return A boolean, true if the round has ended, false otherwise
     */
    /*
    Algorithm:
        1) See if player placed five in a row (will be tallied in another function)
        2) See if player captured 5 or more pairs
        3) See if board is full, i.e. a tie
        4) Set the winner of the round if there is one
     */
    private boolean CheckRoundEnd(Player a_currPlayer) {
        // If there is a winner, or the board is full, end the round
        boolean endRound = false;
        // Holds the message to output at the end of the round
        String endMessage = "";

        // See if player placed five in a row (will be tallied in another function)
        int win = m_board.GetWinInARow();
        if (win > 0) {
            m_numWinInARow = win;
            m_winner = a_currPlayer;
            endMessage = a_currPlayer.GetNameAndColor() + " has won the round by placing " +
                    WIN_SCORE + " stones in a row!";
            endRound = true;
        }
        // See if player captured 5 or more pairs
        if (a_currPlayer.GetCapturedPairs() >= WIN_SCORE) {
            m_winner = a_currPlayer;
            endMessage = a_currPlayer.GetNameAndColor() + " has won the game by capturing " +
                    a_currPlayer.GetCapturedPairs() + " pairs!";
            endRound = true;
        }

        // See if board is full, i.e. a tie
        if (m_board.IsBoardFull()) {
            m_winner = null;
            endMessage = "The board is full! The round ends in a tie!";
            endRound = true;
        }

        // Log round end message
        if (endRound) {
            GameLog.AddMessage(endMessage);
        }

        return endRound;
    }

    /**
     * To tally up the scores for the round and output the results,
     *     i.e. who won the round and how many points they got
     */
    /*
    Algorithm:
        1) Determine if tie or win
        2) Add 5 in a row scores to the winner
        3) Add 1 point for each pair captured to each player
        4) Add 1 point for each set of 4 uninterrupted stones to each player
        5) Output the end scores
     */
    private void TallyScores() {
        GameLog.AddMessage("\n" + GameLog.SECTION_FORMAT + "Score Details:");

        // Add 5 in a row scores
        if (m_numWinInARow > 0 && m_winner != null) {
            // Winner receives 5 points for every 5 in a row they got in each direction
            m_winner.IncTournamentScore(m_numWinInARow * WIN_SCORE);
            GameLog.AddMessage("\t- Added " + WIN_SCORE + " points " + m_numWinInARow + " time(s) to "
                    + m_winner.GetNameAndColor() + ", for placing " + WIN_SCORE
                    + " stones in a row, winning the round!\n");
        }

        // Take all players and add their points to their tournament score
        int pairsCaptured; int fourInARow;
        for (Player player : m_players) {
            // Add 1 point for each pair captured
            pairsCaptured = player.GetCapturedPairs();
            player.IncTournamentScore(player.GetCapturedPairs());
            if (pairsCaptured > 0) {
                GameLog.AddMessage("\t- Added " + player.GetCapturedPairs() + " point(s) to " + player.GetNameAndColor()
                        + ", for capturing " + player.GetCapturedPairs() + " pair(s)!\n");
            }

            // Add 1 point for each set of 4 uninterrupted stones
            fourInARow = m_board.GetUninterStones(STRAIGHT_STONES, player.GetColor());
            // If player has no uninterrupted stones, continue (literal constant 0, for readability)
            player.IncTournamentScore(fourInARow);
            if (fourInARow > 0) {
                GameLog.AddMessage("\t- Added " + fourInARow + " point(s) to " + player.GetNameAndColor()
                        + ", for having " + fourInARow + " set(s) of " + STRAIGHT_STONES
                        + " uninterrupted stones at the end of the round!\n");
            }
        }

        // Log the end scores
        GameLog.AddMessage("\nEnd scores:");
        RecordEndPly();
    }

    /**
     * To record the end of the ply in the game log, i.e. the scores of the players
     */
    private void RecordEndPly() {
        GameLog.AddMessage("Captured Pairs:");
        for (Player player : m_players) {
            GameLog.AddMessage("\t" + player.GetNameAndColor() + ": " + player.GetCapturedPairs());
        }
        GameLog.AddMessage("Tournament scores:");
        for (Player player : m_players) {
            GameLog.AddMessage("\t" + player.GetNameAndColor() + ": " + player.GetTournamentScore());
        }
    }

}