//
//  Handles the return/error codes for the program, specifically for mutators
//

package edu.ramapo.rpodziel.pente.model;

public class Codes {
    // Implemented as an enum to avoid namespace pollution in each class
    // This is not practical for a large scale project, but for this project
    // and its size - it's the easiest implementation to record error codes.
    public enum ReturnCode {
        // General
        UNKNOWN,
        SUCCESS,

        // Board's codes
        COULD_NOT_PARSE,
        INVALID_MOVE,
        SPACE_OCCUPIED,
        INVALID_BOARD,
        FULL_BOARD,
        ALREADY_WINNER,
        INVALID_BOUNDS,
        NO_PREV_MOVES,

        // Player's codes
        INVALID_INC,
        INVALID_NAME,

        // Round's codes
        SERIALIZE,
        ROUND_END,
        INVALID_PLAYER,
        NULL_PLAYER,
        SAME_COLOR,

        // Serialize codes
        LOAD_ERROR,
        SAVE_ERROR,
        FILE_EXISTS
    }

    Codes() {}

    /**
     * Prints out the error message associated with the code - just
     *     a large switch statement
     * @param a_errCode a Codes.ReturnCode, the code to print the message for
     * @return A string, the error message
     */
    public static String GetMessage(ReturnCode a_errCode) {
        // We can use the .name() method on the enum to get the string representation of the enum
        // but it does not give us the full message we want to display to the console.
        String errorMessage;
        switch (a_errCode) {
            // Success Should be validated at this point - added for completeness
            case SUCCESS:
                return "";

            // Board's codes
            case COULD_NOT_PARSE:
                errorMessage = "Could not parse input: Format should be <letter><number> (e.g. 'A1', 'J10')!";
                break;
            case INVALID_MOVE:
                errorMessage = "Invalid move: Move must be within the bounds of the board!";
                break;
            case SPACE_OCCUPIED:
                errorMessage = "Space occupied: Cannot place stone on an occupied space!";
                break;
            case INVALID_BOARD:
                errorMessage = "Invalid board: Board must be square and be "
                        + Board.BOARD_SIZE + " pieces long and wide!";
                break;
            case FULL_BOARD:
                errorMessage = "Full board: Cannot place stone on a full board!";
                break;
            case ALREADY_WINNER:
                errorMessage = "Already winner: Cannot place stone if there is known a winner!";
                break;
            case INVALID_BOUNDS:
                errorMessage = "Invalid bounds: Bounds must be within board size!";
                break;
            case NO_PREV_MOVES:
                errorMessage = "No previous moves: Cannot undo move if there are no previous moves!";
                break;

            // Player's codes
            case INVALID_INC:
                errorMessage = "Invalid increment: Increment must be greater than 0!";
                break;
            case INVALID_NAME:
                errorMessage = "Invalid name: Name must be a valid string!";
                break;

            // Round's codes
            case SERIALIZE:
                errorMessage = "Serializing round...";
                break;
            case ROUND_END:
                errorMessage = "Round ended!";
                break;
            case INVALID_PLAYER:
                errorMessage = "Invalid player: Player must be a valid player!";
                break;
            case NULL_PLAYER:
                errorMessage = "Null player: Player cannot be null!";
                break;

            // Serialize codes
            case LOAD_ERROR:
                errorMessage = "Load error: Could not load tournament!";
                break;
            case SAVE_ERROR:
                errorMessage = "Save error: Could not save tournament!";
                break;
            case FILE_EXISTS:
                errorMessage = "File exists: A file with this name already exists!";
                break;

            // Default - should never happen
            default:
                errorMessage = "Unknown error!";
                break;
        }
        return "\n" + errorMessage + "\n";
    }
}
