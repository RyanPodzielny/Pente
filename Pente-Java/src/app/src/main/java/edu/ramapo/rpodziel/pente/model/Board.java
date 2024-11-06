//
//  Implementation of Board class - runs the game board
//

package edu.ramapo.rpodziel.pente.model;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Stack;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Board implements Serializable, Cloneable {
    /* Class Constants */

    public static final int BOARD_SIZE = 19;
    public static final String CENTER_POSITION = "J10";
    public static final char NULL_PIECE = 'O';

    // Offsets for getting proper indices
    public static final char COLUMN_OFFSET = 'A';
    public static final char ROW_OFFSET = 1;

    // Represents the direction of where to go on the board:
    // 0, 1: up, 1, 1: up right, 1, 0: right, 1, -1: down right
    // 0, -1: down, -1, -1: down left, -1, 0: left, -1, 1: up left
    public static final int[] COLUMN_DELTA =
        {0, 1, 1, 1, 0, -1, -1, -1};
    public static final int[] ROW_DELTA =
        {1, 1, 0, -1, -1, -1, 0, 1};
    // Must be the same length as COLUMN_DELTA and ROW_DELTA - represents the number of directions
    // No better way to do this in Java as we need to use both deltas in lock step
    public static final int NUM_DIRECTIONS = 8;

    public static final int DEFAULT_SCORES = 0;

    public static final String DEFAULT_LAST_POSITION = "";

    public static final int WIN_SCORE = 5;
    public static final int CAPTURE_NUM = 2;


    // Move 'struct' to store all information about a move
    // Really used to store information about the last move, and be able to undo it
    // Very helpful for the computer strategy
    private static class Move implements Serializable{
        // Position placed by last move
        public String position;

        // Piece restriction, used to restrict where a player can place a piece
        public int innerBounds;
        public int outerBounds;

        // Number of PositionPairs captured in a single turn - updates every move
        public int capturedPairs;
        public int winInARow;

        public int intersectLeft;
        public Vector<String> prevSeqs;

        public Move() {
            position = DEFAULT_LAST_POSITION;
            innerBounds = BOARD_SIZE - BOARD_SIZE;
            outerBounds = BOARD_SIZE;
            capturedPairs = DEFAULT_SCORES;
            winInARow = DEFAULT_SCORES;
            intersectLeft = BOARD_SIZE * BOARD_SIZE;
            prevSeqs = new Vector<String>();
            prevSeqs.setSize(NUM_DIRECTIONS);
        }

        /**
         * Clones a deep copy of the Move object
         * @return A deep copy of the Move object
         */
        @NonNull
        @Override
        public Move clone() {
            Move copy = new Move();
            try { copy = (Move) super.clone(); }
            catch (CloneNotSupportedException e) {
                copy.position = this.position;
                copy.innerBounds = this.innerBounds;
                copy.outerBounds = this.outerBounds;
                copy.capturedPairs = this.capturedPairs;
                copy.winInARow = this.winInARow;
                copy.intersectLeft = this.intersectLeft;
            }
            // Create a deep copy of the vector of the previous sequences
            copy.prevSeqs = (Vector<String>)this.prevSeqs.clone();

            return copy;
        }
    }

    // A 'struct' to represent a position on the board
    // Used to be able to easily parse a position into a row and column index
    // Since java does not have C++'s pass by reference, we need to use a class to do so
    public static class PositionPair implements Serializable {
        public int row;
        public int column;
        public PositionPair() {}
    }


    /* Private members */

    // 2D vector representing square game board depending on m_boardSize,
    // each element is the color symbol of a player, e.g. "W" for white
    private Vector<Vector<Character>> m_gameBoard;

    // The stack of all moves made by players,
    private Stack<Move> m_prevMoves;

    // Current move made by player
    private Move m_currMove;


    /* Constructors */
    public Board() {
        InitGameBoard();
        m_prevMoves = new Stack<Move>();
        m_currMove = new Move();
    }


    /* Accessors */

    /**
     * Get the GameBoard as a 2D vector of characters
     * @return A 2D vector of characters representing the game board
     */
    public Vector<Vector<Character>> GetGameBoard() {
        // Make a copy of the game board
        Vector<Vector<Character>> gameBoardCopy = new Vector<Vector<Character>>();
        for (int i = 0; i < BOARD_SIZE; ++i) {
            gameBoardCopy.add(new Vector<Character>());
            for (int j = 0; j < BOARD_SIZE; ++j) {
                gameBoardCopy.get(i).add(m_gameBoard.get(i).get(j));
            }
        }
        return gameBoardCopy;
    }

    /**
     * Get the inner bounds of the board, the minimum distance a stone can be from the center stone
     * @return an integer representing the inner bounds of the board
     */
    public int GetInnerBounds() {
        return m_currMove.innerBounds;
    }

    /**
     * Get the outer bounds of the board, the maximum distance a stone can be from the center stone
     * @return an integer representing the outer bounds of the board
     */
    public int GetOuterBounds() {
        return m_currMove.outerBounds;
    }

    /**
     * To check if the game is over, i.e. if there is a winner or the board is full
     * @return true if the game is over, false otherwise
     */
    public boolean IsGameOver()  {
        return (IsBoardFull() || IsWinner());
    }

    /**
     * To check if the board is full, i.e. no more intersections left
     * @return true if the board is full, false otherwise
     */
    public boolean IsBoardFull() {
        return m_currMove.intersectLeft <= 0;
    }
    /** To check if there is a winner, i.e. if there is a sequence of 5 in a row
     * @return true if there is a winner, false otherwise
     */
    public boolean IsWinner()  {
        return m_currMove.winInARow > 0;
    }

    /**
     * To get the number of spaces left on the board
     * @return the number of spaces left on the board, an integer
     */
    public int GetIntersectLeft() {
        return m_currMove.intersectLeft;
    }

    /**
     * To get the amount times the last stone placed by the player
     *     has contributed to a win. E.g. 2 if the player has placed
     *     2 sets of the winning amount of stones in a row
     * @return the number of times times the player wins
     */
    public int GetWinInARow()  {
        return m_currMove.winInARow;
    }

    /**
     * To get the amount of pairs captured by the last move
     * @return the number of pairs captured, an integer
     */
    public int GetCapturedPairs() {
        return m_currMove.capturedPairs;
    }

    /**
     * To get the last position placed by the player
     * @return the last position placed by the player, a string
     */
    public String GetLastPosition() {
        return m_currMove.position;
    }

    /* Mutators */

    /**
     * Facilitates the move made by player by placing a stone on the
     *     board. Checking if the move is valid and updates the board accordingly.
     * @param a_color a character. The color of the player making the move
     * @param a_position a constant string reference. The position the player
     *           wants to place their stone, e.g. "J10"
     * @return The success of the move, a Codes.ReturnCode enum value
     */
    /*
    Algorithm:
        1) Parse our position to get usable indices, if we can't parse
         we cannot place a stone
        2) Ensure that the position is within the board
        3) Check if there are any restrictions on where the stone can be placed
        4) Ensure there are no stones already placed on the intersection
        5) If there already winner or board is full, we cannot place a stone
          as game is over
        6) Update the board based on the move, checking for wins, captures, etc.
        7) Store the move for later use, if we need to undo at a later time
     */
    public Codes.ReturnCode PlaceStone(char a_color, final String a_position) {
        // We could put the guard clauses to protect against invalid moves in its
        // own separate function to keep the logic separated from the returns.
        // But if this function ever were to be modified, it would be unclear and
        // disjointed to have the guard clauses somewhere else - in terms of modifying
        // the logic or the guard clauses.

        // Parse the position to get the row and column index
        PositionPair rawPosition = new PositionPair();
        // Need to be able to parse the position into valid row and column indices
        if (!ParsePosition(a_position, rawPosition)) {
            return Codes.ReturnCode.COULD_NOT_PARSE;
        }
        int row = rawPosition.row; int column = rawPosition.column;

        // Position must be within the board
        if (!IsValidIndex(row, column)) {
            return Codes.ReturnCode.INVALID_MOVE;
        }
        // Check if we have a restriction on where stone can be placed
        int distance = AwayFromCenter(row, column);
        if ((m_currMove.innerBounds > distance) || (distance > m_currMove.outerBounds)) {
            return Codes.ReturnCode.INVALID_MOVE;
        }
        // Can't place stone if square is already occupied
        if (m_gameBoard.get(row).get(column) != NULL_PIECE) {
            return Codes.ReturnCode.SPACE_OCCUPIED;
        }
        // Can't place a stone if there is a winner
        if (m_currMove.winInARow > 0) {
            return Codes.ReturnCode.ALREADY_WINNER;
        }
        // If board is full, we cannot place a stone
        if (IsBoardFull()) {
            return Codes.ReturnCode.FULL_BOARD;
        }


        // Place the stone on the board
        m_gameBoard.get(row).set(column, a_color);
        // Store the sequences after placing the stone, but before capturing
        m_currMove.prevSeqs = ColorSeq(WIN_SCORE, row, column);
        m_currMove.intersectLeft--;
        m_currMove.position = a_position;

        // Check if the move resulted in a winner, or captured PositionPairs
        m_currMove.winInARow = GetNumNInARow(WIN_SCORE, row, column);
        m_currMove.capturedPairs = CapturePairs(a_color, row, column);

        // Store the move on the stack, so we can undo if needed
        m_prevMoves.push(m_currMove);

        return Codes.ReturnCode.SUCCESS;
    }

    /**
     * To undo the last move made by a player
     * @return The success of the undo, a Codes.ReturnCode enum value
     */
    /*
    Algorithm:
        1) Check if there are any moves to undo, can't undo if there are none
        2) Get the last move made by the player
        3) Undo the move by updating the board to the previous state
     */
    public Codes.ReturnCode UndoMove() {
        // Can't undo if there are no moves to undo
        if (m_prevMoves.empty()) {
            return Codes.ReturnCode.NO_PREV_MOVES;
        }

        // Get the last move
        m_currMove = m_prevMoves.peek();
        m_prevMoves.pop();

        // Get the row and column indices from the position. Row and column
        // Not stored in the move as we can get it from a method - we know its valid already
        PositionPair rawPosition = new PositionPair();
        ParsePosition(m_currMove.position, rawPosition);
        int row = rawPosition.row; int column = rawPosition.column;

        // Undo the sequences - middle piece is stored at each direction so set it to null
        UpdateSeqs(m_currMove.prevSeqs, row, column);
        m_gameBoard.get(row).set(column, NULL_PIECE);

        // Update the number of intersections left based on if there was a capture
        m_currMove.intersectLeft++;
        m_currMove.intersectLeft += m_currMove.capturedPairs * CAPTURE_NUM;

        // Reset the win and capture counts as we have undone the move
        // Can't have a win or capture there was no move made at this state
        m_currMove.winInARow = DEFAULT_SCORES;
        m_currMove.capturedPairs = DEFAULT_SCORES;

        return Codes.ReturnCode.SUCCESS;
    }

    /** To set a in-progress game board, i.e. a board from a saved/serialized game
     * @param a_gameBoard a 2D vector of character passed by value. Represents
     *         game board, with each element being the color of a player
     *         e.g. "W" for white
     * @return The success of setting the board, a Codes.ReturnCode enum value
     */
    /*
    Algorithm:
        1) Check if the board is valid, i.e. square and of proper size
        2) Check if there is a winner, if so we cannot set the board as game is over
        3) Ensure board is not full as cannot place a any stones on a full board
        4) Set the board to the new board, update the state to reflect the new board
     */
    public Codes.ReturnCode SetBoard(final Vector<Vector<Character>> a_gameBoard) {
        // Guard Clauses to prevent setting invalid board, like before
        // I am not putting the guard clauses in their own function to ensure
        // the logic is not disjointed from the return statements.

        // Check if board is the correct size
        if (a_gameBoard.size() != BOARD_SIZE) {
            return Codes.ReturnCode.INVALID_BOARD;
        }
        // Check if board is square
        for (final Vector<Character> row : a_gameBoard) {
            if (row.size() != BOARD_SIZE) {
                return Codes.ReturnCode.INVALID_BOARD;
            }
        }

        // Set our member to the board, so we can use its methods
        // But store a copy, so we can revert if something goes wrong
        Vector<Vector<Character>> gameBoardCopy = GetGameBoard();
        m_gameBoard = a_gameBoard;
        // Stores the number of intersections left on the board
        int intersectLeft = 0;
        // We nest here as we need to check 2 conditions, if there are pieces left
        // and if there is a winner. We need to check every position on the board here
        // as we don't know where the last move was made. It is also more efficient to
        // not loop through entire board twice - only need one check.
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int column = 0; column < BOARD_SIZE; column++) {
                // Check if there are any pieces left
                if (a_gameBoard.get(row).get(column) == NULL_PIECE) {
                    intersectLeft++;
                    // Don't check for winner if piece is null
                    continue;
                }

                // Check if we have a winner on select piece
                if (GetNumNInARow(WIN_SCORE, row, column) > 0) {
                    // Put game board back to original state, as we cannot set board
                    m_gameBoard = gameBoardCopy;
                    return Codes.ReturnCode.ALREADY_WINNER;
                }
            }
        }
        // If board is full, we cannot place a stone
        if (intersectLeft == 0) {
            // Put game board back to original state, as we cannot set board
            m_gameBoard = gameBoardCopy;
            return Codes.ReturnCode.FULL_BOARD;
        }


        // Update members to reflect new board
        m_currMove.intersectLeft = intersectLeft;
        // We don't know last position, so we set it to default
        m_currMove.position = DEFAULT_LAST_POSITION;
        m_currMove.winInARow = DEFAULT_SCORES;
        m_currMove.capturedPairs = DEFAULT_SCORES;

        // Clear the previous moves as we have a new board and don't know last move
        m_prevMoves = new Stack<Move>();

        return Codes.ReturnCode.SUCCESS;
    }

    /**
     * To set the inner and outer bounds of the board, i.e. the
     *     minimum and maximum distance a stone can be from the center stone
     * @param a_innerBounds an integer. The inner bounds of the board, if 0
     *         there is no restriction, if size of board cannot place anywhere
     * @param a_outerBounds an integer. The outer bounds of the board, if 0
     *         cannot place stone on any piece, if board size there is no restriction
     * @return The success of setting the bounds, a Codes.ReturnCode enum value
     */
    public Codes.ReturnCode SetBounds(int a_innerBounds, int a_outerBounds) {
        // Bounds must be within the board size, can't be negative or above the board
        if ((a_innerBounds > BOARD_SIZE || a_innerBounds < 0) ||
                (a_outerBounds > BOARD_SIZE || a_outerBounds < 0)) {
            return Codes.ReturnCode.INVALID_BOUNDS;
        }

        m_currMove.innerBounds = a_innerBounds;
        m_currMove.outerBounds = a_outerBounds;
        return Codes.ReturnCode.SUCCESS;
    }

    /* Main for Debug */
    public static void main(String[] args) {}

    /* Public Utility Functions */

    /**
     * To clone the board, i.e. make a deep copy of the board
     * @return A deep copy of the board object
     */
    @NonNull
    @Override
    public Board clone() {
        Board copy = new Board();
        try { copy = (Board) super.clone(); }
        // Ignoring error as we currently all current private members need to be
        // deep copied regardless of using super.clone()
        catch (CloneNotSupportedException ignored) {}

        // Deep copy all of our object members
        copy.m_gameBoard = GetGameBoard();
        copy.m_currMove = this.m_currMove.clone();
        copy.m_prevMoves = (Stack<Move>)this.m_prevMoves.clone();

        return copy;
    }

    /**
     * To get the number of sequences of n stones in a row on a specific
     *     intersection on the board. E.g. 2 if there are 2 sequences of 5 in a
     *     row in two directions on the intersection.
     * @param a_n an integer. The number of stones out from the intersection to check for
     * @param a_row an integer. The row index of the intersection to check
     * @param a_column an integer. The column index of the intersection to check
     * @return The number of sequences of n stones in a row, an integer
     */
    /*
    Algorithm:
        1) Check if we can check for n in a row, i.e. n > 1. A sequence of 1 is just a
            stone on the board
        2) Add the number of stone sequences in each direction together based on their
            cardinal planes: horizontal, vertical and the two diagonals
        3) Divide by n to account for double counting in the cardinal planes
      */
    public int GetNumNInARow(int a_n, int a_row, int a_column) {
        // Can't check for less than 2 in a row - causes problems with double counting
        final int LOWER_LIMIT = 2;

        // Holds the count of the number of times N in a row appears
        int NInARow = 0;

        if (a_n < LOWER_LIMIT) {
            return NInARow;
        }

        // Get the counts based on the cardinal planes, e.g a count of 10 means
        // when a_n = 5, the stone sequence at that direction had 10 stones in a row
        // The count also will "share" the middle stone at the intersection, so a
        // count of 10 at a_n = 5 really means there is 9 in a row, sharing the middle
        for(int count : CardinalCount(a_n, a_row, a_column)) {
            // If a count = n * 2, then there are 2 sequences of stones of n
            // in a row in that direction when sharing the middle stone.
            // If it's not double, then there is only one sequence so subtract 1 to
            // account for the shared middle stone.
            if (count > 0 && ((count / 2) % a_n) != 0) {
                count--;
            }

            // We use integer division to get the number of sequences in a direction
            // It prevents counting stones in a row that are not in a sequence of n
            NInARow += count / a_n;
        }

        return NInARow;
    }

    /**
     * To get the uninterrupted amount of stones on the entire board from
     *     a certain stone color and a select number.
     * @param a_n an integer. The number of stones in a row to check for
     * @param a_color a character. The color of the stones to check for
     * @return The number of uninterrupted stones on the board, an integer
     */
    /*
    Algorithm:
        1) Check if a_n is within the bounds of the board and not 0,
            can't check for 0 stones in a row
        2) Loop through every intersection on the board where the stone color
            matches
        3) Get the number of stones in a row in each cardinal place, e.g. horizontal,
            vertical and the two diagonals at a_n. We cannot share the same
            direction if a stone sequence is greater than a_n. We can share
            other directions, but not the same one
     */
    public int GetUninterStones(int a_n, char a_color) {
        // Holds the total number of structures/interrupted stones at a_n
        int total = 0;

        // Can't check for 0 stones in a row, and we check a_n + 1, so we need to
        // be within the bounds of the board
        if (a_n < 1 || a_n > BOARD_SIZE - 1) {
            return total;
        }

        // For every intersection
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int column = 0; column < BOARD_SIZE; column++){
                // Don't check on different color stones or null pieces
                if (m_gameBoard.get(row).get(column) != a_color) {
                    continue;
                }

                // Get the number of stones in a row in each cardinal place for a_n + 1
                // at specific intersection, as we cannot double count in the same direction
                for (int count : CardinalCount(a_n + 1, row, column)) {
                    // Remove the double count on the shared middle stone
                    count--;
                    // Only add if the count is exactly a_n, i.e. no double count
                    if (count == a_n) {
                        total++;
                    }
                }
            }
        }

        // We double count the sequence n times, so we need to divide by n
        return total / a_n;
    }

    /**
     * To get the number of potential captures at a specific intersection
     *     on the board that can happen in the next ply of the game. E.g.
     *     OBBW -> 1 potential capture.
     * @param a_color a character. The color of the stones to check for
     * @param a_row an integer. The row index of the intersection to check
     * @param a_column an integer. The column index of the intersection to check
     * @return The number of potential captures on a specific intersection, an integer
     */
    /*
    Algorithm:
        1) Create a pattern to match for potential captures, i.e. a sequence of
            stones that can be captured in the next ply
        2) Get the sequences of stones in each cardinal direction
        3) Check if the sequences match the pattern, if so we have a potential capture
    Assistance Received:
        https://regex101.com
        https://www.w3schools.com/java/java_regex.asp
     */
    public int GetPotentialCaptures(char a_color, int a_row, int a_column) {
        // Hold the amount of potential captures
        int count = 0;

        // Need to match of this regex, where the stone sequence can be on either side:
        // [^NULL_PIECE^a_color]{CAPTURE_NUM}|{CAPTURE_NUM}[^NULL_PIECE^a_color]
        // E.g. WBBO -> 1 potential capture OBBW -> 1 potential capture
        // The ends cannot both be the same color, one must be null and the other
        // must be a different color than our current one

        // Purposefully not putting in a separate function as it is only used here and is
        // much more readable here.

        // We will first create the header: [^NULL_PIECE^a_color]
        final String REGEX_HEADER = "[^" + NULL_PIECE + "^" + a_color + "]";

        // Then create the capture sequence: NULL_PIECE{CAPTURE_NUM}
        StringBuilder capSeq = new StringBuilder(String.valueOf(NULL_PIECE));
        for (int i = 0; i < CAPTURE_NUM; i++) {
            capSeq.append(a_color);
        }
        // Get the reverse of it, to get the other combination
        StringBuilder revCapSeq = new StringBuilder(capSeq.toString()).reverse();
        // Add the header to the capture sequence, and the reverse capture sequence
        capSeq.append(REGEX_HEADER);
        revCapSeq = new StringBuilder(REGEX_HEADER + revCapSeq);

        // Put it into a pattern
        Pattern pattern = Pattern.compile(capSeq + "|" + revCapSeq);
        Matcher matcher;
        
        // Now we need to get the cardinal directions, e.g. horizontal, vertical and the two diagonals
        final int PLANES = NUM_DIRECTIONS / 2;
        // Our sequence length is the capture number + 1, as we need to check 3 intersections away
        // in each direction
        final int SEQ_LENGTH = CAPTURE_NUM + 1;

        Vector<String> directionSeqs = ColorSeq(SEQ_LENGTH, a_row, a_column);
        StringBuilder firstSeq; StringBuilder secondSeq;

        for(int direction = 0; direction < PLANES; direction++) {
            // Remove the same middle stone from the sequence, as we don't want to double count
            firstSeq = new StringBuilder(directionSeqs.get(direction)).delete(0,1);
            // Get the second sequence, which we need to reverse as we want to check the other side
            // Delete first stone, as we haven't "placed it"
            secondSeq = new StringBuilder(directionSeqs.get(direction + PLANES)).delete(0,1).reverse();

            // If hit a match, that means we have 1 potential capture in next ply
            // Values on left + stone to place + values on right, is the sequence
            matcher = pattern.matcher(secondSeq.toString() + a_color + firstSeq.toString());
            if (matcher.find()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Convert the string/move input to a valid row and column
     *     index for the game board. Takes in position, e.g. "J10" and parses
     *     it to information the board can use, e.g. row = 9, column = 9
     * @param a_position a constant string reference. It refers to the position the
     *      player wants to place their stone, e.g. "J10"
     * @param a_rawPosition a PositionPair object passed by reference. The raw position
     * @return The success of the placing parsing the move, a boolean value
     */
    /*
    Algorithm:
        1) Check if position meets length requirements to be valid
        2) If row is not a number, we cannot parse
        3) If column is not a letter, we cannot parse
     */
    public static boolean ParsePosition(final String a_position, PositionPair a_rawPosition) {
        // Starting index on the move/position format
        final int ROW_START_INDEX = 1;
        final int COLUMN_START_INDEX = 0;
        // Position string must be of certain length to be valid
        final int MIN_LENGTH = 2;
        final int MAX_LENGTH = 3;

        // Guard clauses for invalid parse
        // Check if position is valid length
        if (a_position.length() < MIN_LENGTH || a_position.length() > MAX_LENGTH) {
            return false;
        }
        // Clause protects casting from throwing error, row must be a number
        // Use substring as could more than one digit
        char element;
        for (int i = ROW_START_INDEX; i < a_position.length(); i++) {
            element = a_position.charAt(i);
            if (!Character.isDigit(element)) {
                return false;
            }
        }

        // Check if first character is a letter as column must be a letter
        if (!Character.isLetter(a_position.charAt(COLUMN_START_INDEX))) {
            return false;
        }

        // Convert the row and column to their respective indices based on
        // their offsets. Row is int + 1 and column is char, so we need to convert.
        a_rawPosition.row = Integer.parseInt(a_position.substring(ROW_START_INDEX)) - ROW_OFFSET;
        a_rawPosition.column = Character.toUpperCase(a_position.charAt(COLUMN_START_INDEX)) - COLUMN_OFFSET;

        // Parse was successful
        return true;
    }

    /**
     * Convert row and columns to their corresponding position
     *     string, e.g. row = 9, column = 9 -> "J10"
     * @param a_row an integer. The row index of the move
     * @param a_column an integer. The column index of the move
     * @return The position as a string
     */
    public static String IndicesToString(int a_row, int a_column) {
        // Offset the row and column to their corresponding position
        int row = a_row + ROW_OFFSET;
        char column = (char)(a_column + COLUMN_OFFSET);

        // Convert the row to a string and concatenate with the column
        return column + Integer.toString(row);
    }

    /**
     * To check if a row and column index is valid for the game board,
     *     i.e. within the bounds of the board
     * @param a_row an integer. The row index of the move
     * @param a_column an integer. The column index of the move
     * @return If the row and column index is valid, a boolean value
     */
    public static boolean IsValidIndex(int a_row, int a_column) {
        return (a_row >= 0 && a_row < BOARD_SIZE) && (a_column >= 0 && a_column < BOARD_SIZE);
    }

    /**
     * To check how far a row and column index is from the center stone
     * @param a_row an integer. The row index of the move
     * @param a_column an integer. The column index of the move
     * @return The distance from the center stone Chebyshev distance, an integer
     */
    /*
    Assistance Received: https://en.wikipedia.org/wiki/Chebyshev_distance
     */
    public static int AwayFromCenter(int a_row, int a_column) {
        // Chebyshev distance given by: max(abs(x1 - x2), abs(y1 - y2))
        final int CENTER_INDEX = BOARD_SIZE / 2;
        return Math.max(Math.abs(a_row - CENTER_INDEX), Math.abs(a_column - CENTER_INDEX));
    }


    /* Private Utility Functions */

    /**
     * To capture pairs of stones on the board based on the last move
     *     made by the player. E.g. if the last move was WBBW, we capture the
     *     pair of BB, and set new stone sequence to WOOW.
     * @param a_color a character. The color of the player making the move
     * @param a_row an integer. The row index of the move
     * @param a_column an integer. The column index of the move
     * @return The number of pairs captured, an integer
     */
    /*
    Algorithm:
        1) Get the sequences of stones in each direction, not cardinal
            as we don't need to check for captures in the same direction
        2) See if the sequence is the proper length and forms a pair
        3) If so, capture the pair by setting the sequence to null stones
     */
    private int CapturePairs(char a_color, int a_row, int a_column) {
        // Holds the number of PositionPairs captured
        int capturedPairs = 0;

        // Length of sequence to check for capture
        // We need to add 2 to the capture number as we need to check the first and last stone
        final int SEQ_LENGTH = CAPTURE_NUM + 2;

        Vector<String> directionSeqs = ColorSeq(SEQ_LENGTH, a_row, a_column);

        String currSeq;
        for (int i = 0; i < directionSeqs.size(); i++) {
            currSeq = directionSeqs.get(i);
            // Guard clauses to prevent invalid captures
            // If not the proper length, we cannot have a capture
            if (currSeq.length() != SEQ_LENGTH) {
                continue;
            }

            // If the first and last stone are not the same color, we cannot have a capture
            char firstStone = currSeq.charAt(0); char lastStone = currSeq.charAt(currSeq.length() - 1);
            if (firstStone != lastStone) {
                continue;
            }

            // Get the middle of the sequence
            String captureSeq = currSeq.substring(1, currSeq.length() - 1);
            // If the sequence contains all the same stones and the first stone is not the same color as the player
            if (CountSameStones(captureSeq) == CAPTURE_NUM && captureSeq.charAt(0) != a_color) {
                capturedPairs++;

                // Set a string to the length of the capture sequence (no better way to do this in Java)
                StringBuilder nullSeq = new StringBuilder(CAPTURE_NUM);
                for (int j = 0; j < CAPTURE_NUM; j++) { nullSeq.append(NULL_PIECE); }

                directionSeqs.set(i, firstStone + nullSeq.toString() + lastStone);

                // Update the number of intersections left based on the number of captures
                m_currMove.intersectLeft += CAPTURE_NUM;
            }
        }
        // Update the board based on new sequences
        UpdateSeqs(directionSeqs, a_row, a_column);

        return capturedPairs;
    }

    /**
     * To get count of the same colors stones in a sequence at each
     *     cardinal direction, i.e. horizontal, vertical and the two diagonals
     * @param a_n an integer. The number of stones out from the intersection to check for
     * @param a_row an integer. The row index of the intersection to check
     * @param a_column an integer. The column index of the intersection to check
     * @return The number of stones in a row at each cardinal direction,
     *     a vector of integers
     */
    /*
    Algorithm:
        1) Get the sequences of stones in each direction
        2) Count the number of stones in a row in each direction
        3) Combine the opposite directions, e.g. horizontal and vertical
     */
    private Vector<Integer> CardinalCount(int a_n, int a_row, int a_column)  {
        // Represents cardinal directions
        final int PLANES = NUM_DIRECTIONS / 2;
        Vector<Integer> counts = new Vector<>();
        counts.setSize(PLANES);

        Vector<String> seqs = ColorSeq(a_n, a_row, a_column);

        // Need to combine the 8 directions into 4, i.e. combine the opposite directions
        int firstSeqCount, secondSeqCount;
        for(int direction = 0; direction < PLANES; direction++) {
            // Add cardinal and ordinal directions together
            // We return the number of times we have this sequence
            // We do not check if there is more than n - not our problem
            firstSeqCount = CountSameStones(seqs.get(direction));
            secondSeqCount = CountSameStones(seqs.get(direction + PLANES));
            counts.set(direction, firstSeqCount + secondSeqCount);
        }

        return counts;
    }

    /**
     * To count the number of uninterrupted stones in a row in a sequence
     * @param a_seq a final string reference. The sequence of stones to check
     * @return
     */
    private static int CountSameStones(final String a_seq) {
        // All sequences should be starting from closest to center stone
        // Holds the number of stones in a row
        int count = 0;
        // Check if all elements in the vector are the same
        for (int i = 0; i < a_seq.length(); i++) {
            // Stop when we hit a stone that isn't the same - or if null piece
            // Counts the middle piece color - we don't care what color it is
            if (a_seq.charAt(i) != a_seq.charAt(0) || a_seq.charAt(i) == NULL_PIECE) {
                break;
            }
            // Increment count if we have the same stone
            count++;
        }
        return count;
    }

    /**
     * To get the sequence of stones in each direction from a specific
     *     intersection on the board.
     * @param a_n an integer. The number of stones out from the intersection to check for
     * @param a_row an integer. The row index of the intersection to check
     * @param a_column an integer. The column index of the intersection to check
     * @return The sequence of stones in each direction, a vector of strings
     */
    /*
    Algorithm:
        1) For each direction get the offset based on the delta of the direction
        2) If offset is not valid, we cannot get the sequence
        3) Get the sequence of stones in each direction
     */
    private Vector<String> ColorSeq(int a_n, int a_row, int a_column)  {
        Vector<String> colorSeq = new Vector<String>();
        colorSeq.setSize(NUM_DIRECTIONS);

        PositionPair rawPosition = new PositionPair();
        StringBuilder currSeq;
        // Sequence starts from center stone and goes outwards
        for (int direction = 0; direction < NUM_DIRECTIONS; direction++) {
            currSeq = new StringBuilder();
            // Will start on the current row and column
            for (int step = 0; step < a_n; step++) {
                rawPosition.row = a_row; rawPosition.column = a_column;
                // Check if we are at a valid index for our row and column
                if (!OffsetIndices(rawPosition, direction, step)) {
                    break;
                }
                // Add the current stone at the index to the sequence
                currSeq.append(m_gameBoard.get(rawPosition.row).get(rawPosition.column));
            }
            colorSeq.set(direction, currSeq.toString());
        }

        return colorSeq;
    }

    /**
     * To update the board based on the sequences of stones in each
     *     direction from a specific intersection on the board.
     * @param a_seq a final vector of strings reference. The sequence of stones
     *      in each direction to update the board with
     * @param a_row an integer. The row index of the intersection to update
     * @param a_column an integer. The column index of the intersection to update
     * @return The success of updating the sequences, a boolean value
     */
    /*
    Algorithm:
        1) Check if the size of the sequence vector is the proper length
        2) For every direction in the sequence, update the board based on it
     */
    private boolean UpdateSeqs(final Vector<String> a_seq, int a_row, int a_column) {
        // Something went horribly wrong - sequences must include all directions
        if (a_seq.size() != NUM_DIRECTIONS) {
            return false;
        }

        PositionPair rawPosition = new PositionPair();
        for (int direction = 0; direction < NUM_DIRECTIONS; direction++) {
            for (int step = 0; step < a_seq.get(direction).length(); step++) {
                rawPosition.row = a_row; rawPosition.column = a_column;
                // Check if we are at a valid index for our row and column
                // If it's not, something is wrong with the sequence
                if (!OffsetIndices(rawPosition, direction, step)){
                    return false;
                }
                // Update the board based the current stone
                m_gameBoard.get(rawPosition.row).set(rawPosition.column, a_seq.get(direction).charAt(step));
            }
        }
        return true;
    }

    /**
     * To offset the row and column indices based on the direction
     *     and step. E.g. if direction is 0 and step is 2, we offset the row
     *     by 2 and column by 0.
     * @param a_rawPosition a PositionPair object passed by reference. The raw position
     * @param a_direction an integer. The direction to offset the indices
     * @param a_step an integer. The number of steps to offset the indices
     * @return If the offset indices are valid, a boolean value
     */
    private static boolean OffsetIndices(PositionPair a_rawPosition, int a_direction, int a_step) {
        a_rawPosition.row += (ROW_DELTA[a_direction] * a_step);
        a_rawPosition.column += (COLUMN_DELTA[a_direction] * a_step);

        return IsValidIndex(a_rawPosition.row , a_rawPosition.column);
    }

    /**
     * To initialize the game board to a 2D vector of null pieces
     */
    private void InitGameBoard() {
        m_gameBoard = new Vector<Vector<Character>>();
        for (int row = 0; row < BOARD_SIZE; row++) {
            m_gameBoard.add(new Vector<Character>());
            for (int column = 0; column < BOARD_SIZE; column++) {
                m_gameBoard.get(row).add(NULL_PIECE);
            }
        }
    }

}