//
//  Human player for game Pente - inherits from Player
//


package edu.ramapo.rpodziel.pente.model;

public class Human extends Player {
    /* Class Constants */

    public static final String DEFAULT_NAME = "Human";

    /* Constructor */

    public Human() {
        super(DEFAULT_NAME);
        m_requiresInput = true;
    }

    /* Main For Debug */
    public static void main(String[] args) { }


    /* Mutators */

    /**
     * Facilitates the human player's move
     * @param a_board a Board object, the board to place the stone on
     * @param a_nextPlayer a Player object, the next player who's ply it is
     * @return a Codes.ReturnCode, the status of the move
     */
    @Override
    public Codes.ReturnCode MakeMove(Board a_board, final Player a_nextPlayer) {
        if (m_position == null) {
            return Codes.ReturnCode.SUCCESS;
        }

        // Place based on the set position
        return a_board.PlaceStone(m_color, m_position);
    }

}
