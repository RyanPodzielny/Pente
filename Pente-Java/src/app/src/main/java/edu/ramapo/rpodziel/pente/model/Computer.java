//
//  Implementation for Computer, handles the computer's moves
//

package edu.ramapo.rpodziel.pente.model;

public class Computer extends Player {
    /* Class Constants */

    public static final String DEFAULT_NAME = "Computer";


    /* Constructor */
    public Computer() {
        super(DEFAULT_NAME);
    }


    /* Main For Debug */
    public static void main(String[] args) { }


    /* Mutators */

    /**
     * Logs the move the computer is making and then places the
     *     stone on the board
     * @param a_board a Board object, the board to place the stone on
     * @param a_nextPlayer a Player object, the next player who's ply it is
     * @return a Codes.ReturnCode, the status of the move
     */
    @Override
    public Codes.ReturnCode MakeMove(Board a_board, final Player a_nextPlayer) {
        // Sets the best move for the computer
        BestMove(a_board, a_nextPlayer);

        Codes.ReturnCode status = a_board.PlaceStone(m_color, m_bestMove.position);
        // Log if successful
        if (status == Codes.ReturnCode.SUCCESS) {
            m_bestMove.formattedReason = "\nI'm placing a stone at " + m_bestMove.position
                    + GetReasonMessage() + "\n";
            GameLog.AddMessage(m_bestMove.formattedReason);
        }

        return status;
    }

}
