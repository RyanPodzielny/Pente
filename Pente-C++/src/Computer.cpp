//
//  Implementation for Computer, handles the computer's moves
//

#include "Computer.h"

/**********************************************************************
Function Name: MakeMove
Purpose: Prints out the move the computer is making and then places the
    stone on the board
Parameters:
    a_board, a Board object passed by reference, the board to place the stone on
    a_nextPlayer, a const reference Player object, the next player who's ply it is
Return Value: None
Assistance Received: None
**********************************************************************/
void Computer::MakeMove(Board &a_board, const Player& a_nextPlayer)  {
    // Sets the best move for the computer
    BestMove(a_board, a_nextPlayer);

    cout << "I'm placing a stone at " << m_bestMove.position << GetReasonMessage() << endl;

    a_board.PlaceStone(m_color, m_bestMove.position);
}