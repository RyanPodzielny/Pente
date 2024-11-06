//
//  Implementation of Codes, handles printing of error messages
//

#include "Codes.h"
#include "Board.h"

/**********************************************************************
Function Name: GetMessage
Purpose: Prints out the error message associated with the code - just
    a large switch statement
Parameters:
    a_code, a Codes::ReturnCode, the code to print the message for
Return Value: A string, the error message
Assistance Received: None
**********************************************************************/
string Codes::GetMessage(Codes::ReturnCode a_code) {
    string errorMessage;
    switch (a_code) {
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
                           + to_string(Board::BOARD_SIZE) + " pieces long and wide!";
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

        // Tournament's codes
        case LOAD_ERROR:
            errorMessage = "Load error: Could not load tournament!";
            break;
        case SAVE_ERROR:
            errorMessage = "Save error: Could not save tournament!";
            break;

        // Default - should never happen
        default:
            errorMessage = "Unknown error!";
            break;
    }
    return "\n" + errorMessage + "\n";
}