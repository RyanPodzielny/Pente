package edu.ramapo.rpodziel.pente.model;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Vector;

public class Player implements Serializable, Cloneable {
    /* Class Constants */

    public static final String DEFAULT_NAME = "Player";
    public static final char DEFAULT_COLOR = '?';
    public static final String NO_COLOR = "Invalid color";
    public static final int DEFAULT_SCORE = 0;
    public static final boolean DEFAULT_INPUT = false;

    public static final String WHITE = "White";
    public static final String BLACK = "Black";
    public static final char WHITE_CHAR = 'W';
    public static final char BLACK_CHAR = 'B';

    protected static final int DEFAULT_EVAL = Integer.MIN_VALUE;

    // Used for passing around in intents and for subclasses
    // Assistance received:
    //  https://stackoverflow.com/questions/285793/what-is-a-serialversionuid-and-why-should-i-use-it
    private static final long serialVersionUID = 42L;

    // Used to get rational for the move
    protected enum MoveReason {
        UNKNOWN,
        WIN,
        CAPTURE,
        BUILD,
        BOARD_RESTRICTION
    }

    // Acts as 'struct' for the computer strategy
    // Used for the computer strategy, stores the move and the reason for the move
    // and how well the move did in the evaluation
    public static class ComputerMove implements Serializable {
        public String position;
        public int evalScore;
        public char color;

        public MoveReason reason;
        public String formattedReason;

        protected ComputerMove() {
            position = "";
            evalScore = DEFAULT_EVAL;
            color = DEFAULT_COLOR;
            reason = MoveReason.UNKNOWN;
            formattedReason = "";
        }

        /**
         * To avoid shallow copies, we need to implement our own clone function
         */
        @NonNull
        public ComputerMove clone() {
            ComputerMove copy = new ComputerMove();
            try  { copy = (ComputerMove) super.clone(); }
            catch (CloneNotSupportedException e) {
                copy.position = this.position;
                copy.evalScore = this.evalScore;
                copy.color = this.color;
                copy.reason = this.reason;
                copy.formattedReason = this.formattedReason;
            }
            return copy;
        }
    }


    /* Protected Members */

    protected String m_name;
    protected char m_color;
    // If the player requires input
    protected boolean m_requiresInput;
    protected String m_position;

    // Overall scores of the player
    protected int m_tournamentScore;
    protected int m_capturedPairs;

    // Used for the computer strategy - stores the best move to make
    protected ComputerMove m_bestMove;


    /* Constructors */

    public Player(String a_name) {
        m_name = a_name;
        m_color = DEFAULT_COLOR;

        m_position = null;
        m_requiresInput = DEFAULT_INPUT;

        m_tournamentScore = DEFAULT_SCORE;
        m_capturedPairs = DEFAULT_SCORE;

        m_bestMove = new ComputerMove();
    }

    public Player() {
        this(DEFAULT_NAME);
    }


    /* Accessors */

    /**
     *  To get the name and color of the player in string form
     * @return a string, the name and color of the player
     */
    public String GetNameAndColor() {
        return m_name + " - " + CharToColor(m_color);
    }

    /**
     * To get the stone color of the player
     * @return The stone color of the player, a character
     */
    public char GetColor() {
        return m_color;
    }

    /**
     * To get the name of the player
     * @return The name of the player, a string
     */
    public String GetName()  {
        return m_name;
    }

    /**
     * To see if the player requires input
     * @return a boolean, true if the player requires input, false otherwise
     */
    public boolean RequiresInput() {
        return m_requiresInput;
    }

    /**
     * To get the number of captured pairs the player has in a round
     * @return  The number of captured pairs the player has, an integer
     */
    public int GetCapturedPairs() {
        return m_capturedPairs;
    }

    /**
     * To get the tournament score of the player
     * @return The tournament score of the player, an integer
     */
    public int GetTournamentScore() {
        return m_tournamentScore;
    }


    /* Mutators */

    // Pure virtual function - must be implemented by derived classes
    // Not putting a default implementation as it should never be called
    public Codes.ReturnCode MakeMove(Board a_board, final Player a_nextPlayer) {
        return Codes.ReturnCode.UNKNOWN;
    }

    /**
     * Sets the input of where the player wants to place their stone
     * @param a_position a string, representing the position the player wants to place their stone
     * @return A Codes.ReturnCode, the status of setting the member
     */
    public Codes.ReturnCode SetInput(final String a_position) {
        m_position = a_position;
        return Codes.ReturnCode.SUCCESS;
    }

    /**
     * Sets the name of the player
     * @param a_name a string, the name of the player
     * @return A ReturnCode representing the success of setting the name
     */
    public Codes.ReturnCode SetName(final String a_name) {
        if (a_name == null) {
            return Codes.ReturnCode.INVALID_NAME;
        }
        m_name = a_name;
        return Codes.ReturnCode.SUCCESS;
    }

    /**
     * To set the stone color of the player
     * @param a_color a character, the new stone color of the player
     * @return A ReturnCode representing the success of setting the color
     */
    public Codes.ReturnCode SetColor(char a_color) {
        m_color = a_color;
        return Codes.ReturnCode.SUCCESS;
    }

    /**
     * To reset the number of captured pairs the player has in a round,
     *     should be called at the start of a new round
     * @return A ReturnCode representing the success of resetting the
     *     captured pairs
     */
    public Codes.ReturnCode ResetCapturedPairs() {
        m_capturedPairs = 0;
        return Codes.ReturnCode.SUCCESS;
    }

    /**
     *  To increment the number of captured pairs the player has in a round
     * @param a_pairs an integer, the number of pairs to increment by
     * @return A ReturnCode representing the success of incrementing the
     *     captured pairs
     */
    public Codes.ReturnCode IncCapturedPairs(int a_pairs) {
        // Can't have negative pairs
        if (a_pairs < 0) {
            return Codes.ReturnCode.INVALID_INC;
        }
        m_capturedPairs += a_pairs;
        return Codes.ReturnCode.SUCCESS;
    }

    /**
     * To increment the tournament score of the player
     * @param a_score an integer, the number of points to increment by
     * @return A ReturnCode representing the success of incrementing the
     *     tournament score
     */
    public Codes.ReturnCode IncTournamentScore(int a_score) {
        if (a_score < 0) {
            return Codes.ReturnCode.INVALID_INC;
        }
        m_tournamentScore += a_score;
        return Codes.ReturnCode.SUCCESS;
    }


    /* Main for Debug */
    public static void main(String[] args) { }


    /* Public Utility Functions */

    /** To avoid shallow copies, we need to implement our own clone function
     * @return a Player, a deep copy of the player
     */
    @NonNull
    public Player clone() {
        Player copy = new Player(this.m_name);
        try { copy = (Player) super.clone(); }
        catch (CloneNotSupportedException e){
            copy.m_color = this.m_color;
            copy.m_requiresInput = this.m_requiresInput;
            copy.m_position = this.m_position;
            copy.m_tournamentScore = this.m_tournamentScore;
            copy.m_capturedPairs = this.m_capturedPairs;
        }
        // Need to deep copy the best move object
        copy.m_bestMove = this.m_bestMove.clone();
        return copy;
    }

    /**
     * To convert a the character of a stone to a string representing
     *     the color in a more human readable format
     * @param a_color a character, the color of the stone
     * @return A string representing the color of the stone
     */
    public static String CharToColor(char a_color) {
        if (a_color == WHITE_CHAR) { return WHITE; }
        else if (a_color == BLACK_CHAR) { return BLACK; }
        else { return NO_COLOR; }
    }

    /**
     * To convert a string representing the color of a stone to a
     *     character
     * @param a_color a string, the color of the stone in plain english
     * @return A character representing the color of the stone
     */
    static char ColorToChar(final String a_color) {
        if (a_color.equals(WHITE)) { return WHITE_CHAR; }
        else if (a_color.equals(BLACK)) { return BLACK_CHAR; }
        else { return DEFAULT_COLOR; }
    }


    /* Protected Utility Functions */

    /**
     * To determine the best move for the computer to make, i.e.
     *     the strategy of the computer
     * @param a_board a final Board object, the current state of the board
     * @param a_nextPlayer a final Player object, the next player to move
     */
    /*
    Algorithm:
        1) For every position on the board, play as ourselves, and play as the
            next player
        2) Record the move made and the evaluation score of it for our move and
            the next player's move
        3) Determine the best move to make based on this score, add to a list
            if it shares a move with current the best move
        4) After all moves have been evaluated, determine the best move to make
            based on the score and the reason for the move
    Assistance Received: Inspiration of computer strategy from
        https://www.youtube.com/watch?v=SLgZhpDsrfc&t=531s (minimax algorithm)
     */
    protected void BestMove(final Board a_board, final Player a_nextPlayer) {
        // Don't touch main board
        Board boardCopy = a_board.clone();

        String currPosition;
        ComputerMove ourBest = new ComputerMove(); ComputerMove theirBest = new ComputerMove();
        ComputerMove ourMove; ComputerMove theirMove;

        Vector<ComputerMove> topMoves = new Vector<ComputerMove>();
        // For every position on the board, play as ourselves, and play as the next player
        // Evaluate the move, and store the best move
        // If there are multiple moves with the same score, add it to a vector of top moves
        for (int row = 0; row < Board.BOARD_SIZE; row++) {
            for (int column = 0; column < Board.BOARD_SIZE; column++) {
                // I did not separate out this into another function as I believe ints more readable
                // in this case. I want to show that the current player is moving for itself,
                // and then the next player is moving for itself. Though worse of for modifiability,
                // I believe it's a good trade off.
                currPosition = Board.IndicesToString(row, column);

                /* Place for us */
                if (boardCopy.PlaceStone(m_color, currPosition) != Codes.ReturnCode.SUCCESS) {
                    continue;
                }
                ourMove = EvaluateMove(boardCopy, this);
                boardCopy.UndoMove();

                /* Place for them */
                if (boardCopy.PlaceStone(a_nextPlayer.GetColor(), currPosition) != Codes.ReturnCode.SUCCESS) {
                    continue;
                }
                theirMove = EvaluateMove(boardCopy, a_nextPlayer);
                boardCopy.UndoMove();

                // Check to see if highest score
                if (ourMove.evalScore >= ourBest.evalScore) {
                    ourBest = ourMove;
                    topMoves.add(ourMove);
                }
                if (theirMove.evalScore >= theirBest.evalScore) {
                    theirBest = theirMove;
                    topMoves.add(theirMove);
                }
            }
        }

        // Determine the best move
        DetermineBest(boardCopy, ourBest, theirBest, topMoves);
    }

    /**
     * To evaluate a move made by a player, i.e. how good the move is
     * @param a_board a final Board object, the current state of the board
     * @param a_player a final Player object, the player who made the move
     * @return A object ComputerMove, containing the position, evaluation
     *     score, color, and reason for the move
     */
    /*
    Algorithm:
        1) Set the multiplier values for the evaluation, i.e. how much we care
            about winning, capturing, and building (in current implementation,
            win takes priority, then capture, then build)
        2) Based on moved, increase the evaluation score if the move is a win,
            capture, or build
        3) If move could result in a potential capture, only for our move,
            decrease the evaluation score - we do not want to place there
        4) Set the reason for the move based on the score, and return the move
    Assistance Received: Inspiration of computer strategy from
        https://www.youtube.com/watch?v=SLgZhpDsrfc&t=531s (minimax algorithm)
     */
    // Though not necessary to be in protected, if a base class wanted to change
    // how a computer evaluates a move or determines the best we can allow them to
    // do so by making them protected
    protected ComputerMove EvaluateMove(final Board a_board, final Player a_player) {
        // Adjustable constants to put more emphasis on certain moves
        // We care the most about winning, then capturing, then building
        final int WIN_MULTI = 10000;
        final int CAPTURE_MULTI = 2000;
        final int BUILD_MULTI = 5;

        // Holds the evaluation score of the move, i.e. how good it is
        int evalScore = 0;

        ComputerMove move = new ComputerMove();
        move.position = a_board.GetLastPosition();
        move.color = a_player.GetColor();

        // Get where we are on the board
        // Need a position pair to be able to directly get row and column
        Board.PositionPair rawPosition = new Board.PositionPair();
        Board.ParsePosition(move.position, rawPosition);
        int row = rawPosition.row; int column = rawPosition.column;

        /* Win */
        evalScore += WIN_MULTI * a_board.GetWinInARow();

        /* Building Blocks */
        int blockCount = 0;
        for (int n = Board.WIN_SCORE - 1; n > 1; n--) {
            blockCount += a_board.GetNumNInARow(n, row, column) - a_board.GetWinInARow();
            evalScore += BUILD_MULTI * blockCount * n * n;
        }
        // Prioritize building block instead of preventing one from forming
        // Incentive for us to gain more points by building larger blocks
        if (blockCount > 0 && m_color == a_player.GetColor()) {
            evalScore += BUILD_MULTI;
        }

        /* Avoiding Captures */
        // Only if we're the one moving - don't want to avoid captures on their turn
        if (evalScore < WIN_MULTI && m_color == a_player.GetColor()) {
            evalScore -= CAPTURE_MULTI * a_board.GetPotentialCaptures(a_player.m_color, row, column);
        }

        /* Capturing */
        evalScore += CAPTURE_MULTI * a_board.GetCapturedPairs();

        // Easy way to see if what move we made is to check the eval score multipliers
        move.evalScore = evalScore;
        // Determine the reason for the move
        if (evalScore >= WIN_MULTI) {
            move.reason = MoveReason.WIN;
        }
        else if (evalScore >= CAPTURE_MULTI) {
            move.reason = MoveReason.CAPTURE;
        }
        else if (evalScore > 0) {
            move.reason = MoveReason.BUILD;
        }
        else {
            move.reason = MoveReason.UNKNOWN;
        }
        return move;
    }

    /**
     * To determine the best move to make based on the evaluation score
     * @param a_board a final Board object, the current state of the board
     * @param ourBest a ComputerMove object, the best move we made
     * @param theirBest a ComputerMove object, the best move they made
     * @param topMoves a Vector of ComputerMove objects, the moves that are
     */
    /*
    Algorithm:
        1) If we can win, we will
        2) Compare the two scores from our best and their best take the
            best move evaluated
        3) Get all top moves that are equal to the best move
        4) If there are multiple moves with the same score, choose one at random
            Helps give the computer a more "human" feel - would just place in the
            same location every time
        5) Check if there is a board restriction, if so make that the reason why
        6) Set the best score
     */
    protected void DetermineBest(final Board a_board, final ComputerMove ourBest,
                           final ComputerMove theirBest, Vector<ComputerMove> topMoves) {
        // We want to win, so if we can win, we will
        if (ourBest.reason == MoveReason.WIN) {
            m_bestMove = ourBest;
            return;
        }
        // Compare the two scores - take the best move evaluated
        if (ourBest.evalScore > theirBest.evalScore) {
            m_bestMove = ourBest;
        }
        else {
            m_bestMove = theirBest;
        }

        // Get all top moves that are equal to the best move
        // Help received: IntelliSense
        topMoves.removeIf(currMove -> currMove.evalScore != m_bestMove.evalScore);

        // If there are multiple moves with the same score, choose one at random
        // Helps give the computer a more "human" feel - would just place in the same location
        // every time
        // https://www.educative.io/answers/how-to-generate-random-numbers-in-java
        if (topMoves.size() > 1) {
            int randIndex = (int) Math.floor(Math.random() * topMoves.size());
            m_bestMove = topMoves.get(randIndex);
        }

        // Stone must be placed on center stone (0)
        if (a_board.GetOuterBounds() == 0) {
            m_bestMove.reason = MoveReason.BOARD_RESTRICTION;
        }

        // If second white move, set our specific values to a ring around center
        // We want to be close to the center in order to build blocks
        // Hardcoded as it's the easiest implementation (3 because must be 3 away)
        if (a_board.GetInnerBounds() == 3) {
            m_bestMove.reason = MoveReason.BOARD_RESTRICTION;
            final String[] VALUES = {"J7", "M10", "J13", "G10"};
            int randIndex = (int) Math.floor(Math.random() * VALUES.length);
            m_bestMove.position = VALUES[randIndex];
        }
    }

    /**
     *  To get the rationale for the move made by the computer in plain
     *     english
     * @return A string representing the rationale for the move
     */
    protected String GetReasonMessage() {
        String reason = " to ";
        // If the best move was not made by us - we're a preventing a move
        if (m_bestMove.color != m_color) {
            reason += "prevent a ";
        }
        switch (m_bestMove.reason) {
            case WIN:
                reason += "win";
                break;
            case CAPTURE:
                reason += "capture";
                break;
            case BUILD:
                reason += "build";
                break;
            case BOARD_RESTRICTION:
                reason = " because of a board restriction, no other moves available";
                break;
            default:
                reason = "ERROR: Unknown reason";
                break;
        }
        reason += "!";
        return reason;
    }

    /**
     *  Gets help from computer if the player asks for help, i.e.
     *     what computer thinks is the best move to make
     * @param a_board a final Board object, the current state of the board
     * @param a_nextPlayer a final Player object, the next player to move
     * @return A copy of ComputerMove object, the best move to make
     */
    public ComputerMove GetHelp(final Board a_board, final Player a_nextPlayer) {
        BestMove(a_board, a_nextPlayer);
        m_bestMove.formattedReason = "The computer recommends you play at "
                + m_bestMove.position + GetReasonMessage();
        GameLog.AddMessage(m_bestMove.formattedReason);

        return m_bestMove.clone();
    }

}