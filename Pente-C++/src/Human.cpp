//
//  Implementation for human player
//

#include "Human.h"

/**********************************************************************
Function Name: MakeMove
Purpose: Facilitates the human player's move
Parameters:
    a_board, a Board object passed by reference, the board to place the stone on
    a_nextPlayer, a const reference Player object, the next player who's ply it is
Return Value: None
Algorithm:
    1) Get the move from the user
    2) Place the stone on the board
Assistance Received: None
**********************************************************************/
void Human::MakeMove(Board &a_board, const Player& a_nextPlayer) {
    const string HELP_MODE = "HELP";

    // Holds location of where player wants to place stone
    string location;
    Codes::ReturnCode boardErrorCode;

    // Loop until and ask for input we get a valid move, or help mode
    for ( ; ; ){

        cout << m_name << ", if you would like to get help from the computer, type '"
             << HELP_MODE << "', if not please enter your move:" << endl << "> ";
        getline(cin, location);

        // Convert to uppercase for comparison
        for (char& c : location) { c = (char) toupper(c); }

        // Check if the user wants help
        if (location == HELP_MODE) {
            GetHelp(a_board, a_nextPlayer);
            // Don't continue the loop, we want to ask for input again
            continue;
        }

        boardErrorCode = a_board.PlaceStone(m_color, location);

        // If player places a stone on a bad intersection, print out the error message
        // Ask for input again (input validation)
        if (boardErrorCode != Codes::SUCCESS) {
            cout << Codes::GetMessage(boardErrorCode) << endl;
            continue;
        }

        break;
    }
}

/**********************************************************************
Function Name: GetHelp
Purpose: Gets help from computer if the player asks for help, i.e.
    what computer thinks is the best move
Parameters:
    a_board, a Board object passed by reference, the board to place the stone on
    a_nextPlayer, a const reference Player object, the next player who's ply it is
Return Value: None
Assistance Received: None
**********************************************************************/
void Human::GetHelp(Board &a_board, const Player& a_nextPlayer) {
    BestMove(a_board, a_nextPlayer);

    // Formatting
    cout << endl;
    cout << "The computer recommends you play at " << m_bestMove.position <<  GetReasonMessage() << endl;
}

/**********************************************************************
Function Name: CallToss
Purpose: Facilitates the coin toss for human when tournament scores are tied
Parameters: None
Return Value: A bool, true if the human player won the coin toss, false if not
Assistance Received: None
**********************************************************************/
bool Human::CallToss() const {
    // We perform and ask the coin flip here because if we want to change
    // the input method the two methods are in different classes. Having it in one
    // central location makes it easier to change the input method
    const string HEADS = "HEADS", TAILS = "TAILS";

    // Literal constant: 2 is the number of sides on a coin
    int coinFlip = rand() % 2;
    // Literal constants: 0, tails/'T' is 1
    string result = (coinFlip == 0) ? HEADS : TAILS;

    // Validate input
    string callToss;
    do {
        cout << endl << "Heads or tails?" << endl << "> ";
        getline(cin, callToss);
        // Make the input uppercase for comparison
        for (char& c : callToss) { c = (char) toupper(c); }
    } while (callToss != HEADS && callToss != TAILS);

    cout << endl << "The coin landed on " << result << "!" << endl;
    return (callToss == result);
}