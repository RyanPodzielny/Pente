//
//  Handles implementation of viewing board
//

#include "BoardView.h"

/**********************************************************************
Function Name: PrintBoard
Purpose: Prints out the board in a readable format
Parameters:
    a_board, a const reference Board object, the board to print
    a_replaceNull, a char, the character to replace null pieces with
Return Value: None
Algorithm:
    1) Have rows to be 1-19 going up
    2) Have columns to be A-S going left to right
    3) At every intersection print the stone that is on it
Assistance Received: None
**********************************************************************/
void BoardView::PrintBoard(const Board& a_board, char a_replaceNull) {
    // Spacing is for whitespace on rows
    const int SPACING = 2;
    const int BOARD_SIZE = Board::BOARD_SIZE;
    const vector<vector<char>> GAME_BOARD = a_board.GetGameBoard();

    cout << endl << "Current Board:" << endl;

    // Row
    for (int row = BOARD_SIZE - 1; row >= 0; row--) {
        // Fix spacing for single digit numbers to ensure board alignment
        cout << setw(SPACING) << row + 1 << " ";

        // Column
        for (int column = 0; column < BOARD_SIZE; column++) {
            // Replace null pieces with a_replaceNull
            if (GAME_BOARD[row][column] == Board::NULL_PIECE) {
                cout << a_replaceNull << " ";
                continue;
            }
            cout << GAME_BOARD[row][column] << " ";
        }
        cout << endl;
    }

    // Print the column footer
    cout << "   ";
    for (int i = 0; i < BOARD_SIZE; i++) {
        cout << (char)('A' + i) << " ";
    }

    cout << endl << endl;
}
