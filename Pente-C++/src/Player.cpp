//
//  Player implementation
//

#include "Player.h"

/* Accessors */

/**********************************************************************
Function Name: GetNameAndColor
Purpose: To get the name and color of the player in string form
Parameters: None
Return Value: The name and color of the player, a string
Assistance Received: None
**********************************************************************/
string Player::GetNameAndColor() const {
    return m_name + " - " + CharToColor(m_color);
}

/**********************************************************************
Function Name: GetColor
Purpose: To get the stone color of the player
Parameters: None
Return Value: The stone color of the player, a character
Assistance Received: None
**********************************************************************/
char Player::GetColor() const {
    return m_color;
}

/**********************************************************************
Function Name: GetName
Purpose: To get the name of the player
Parameters: None
Return Value: The name of the player, a string
Assistance Received: None
**********************************************************************/
string Player::GetName() const {
    return m_name;
}

/**********************************************************************
Function Name: GetCapturedPairs
Purpose: To get the number of captured pairs the player has in a round
Parameters: None
Return Value: The number of captured pairs the player has, an integer
Assistance Received: None
**********************************************************************/
int Player::GetCapturedPairs() const {
    return m_capturedPairs;
}

/**********************************************************************
Function Name: GetTournamentScore
Purpose: To get the tournament score of the player
Parameters: None
Return Value: The tournament score of the player, an integer
Assistance Received: None
**********************************************************************/
int Player::GetTournamentScore() const {
    return m_tournamentScore;
}


/* Mutators */

/**********************************************************************
Function Name: SetName
Purpose: To set the name of the player
Parameters:
    a_name, a const string reference, the new name of the player
Return Value: A ReturnCode representing the success of setting the name
Assistance Received: None
**********************************************************************/
Codes::ReturnCode Player::SetName(const string& a_name) {
    m_name = a_name;
    return Codes::SUCCESS;
}

/**********************************************************************
Function Name: SetColor
Purpose: To set the stone color of the player
Parameters:
    a_color, a character, the new stone color of the player
Return Value: A ReturnCode representing the success of setting the color
Assistance Received: None
**********************************************************************/
Codes::ReturnCode Player::SetColor(char a_color) {
    m_color = a_color;
    return Codes::SUCCESS;
}

/**********************************************************************
Function Name: ResetCapturedPairs
Purpose: To reset the number of captured pairs the player has in a round,
    should be called at the start of a new round
Parameters: None
Return Value: A ReturnCode representing the success of resetting the
    captured pairs
Assistance Received: None
**********************************************************************/
Codes::ReturnCode Player::ResetCapturedPairs() {
    m_capturedPairs = 0;
    return Codes::SUCCESS;
}

/**********************************************************************
Function Name: IncCapturedPairs
Purpose: To increment the number of captured pairs the player has in a round
Parameters:
    a_pairs, an integer, the number of pairs to increment by
Return Value: A ReturnCode representing the success of incrementing the
    captured pairs
Assistance Received: None
**********************************************************************/
Codes::ReturnCode Player::IncCapturedPairs(int a_pairs) {
    // Can't have negative pairs
    if (a_pairs < 0 ) {
        return Codes::INVALID_INC;
    }
    m_capturedPairs += a_pairs;
    return Codes::SUCCESS;
}

/**********************************************************************
Function Name: IncTournamentScore
Purpose: To increment the tournament score of the player
Parameters:
    a_score, an integer, the number of points to increment by
Return Value: A ReturnCode representing the success of incrementing the
    tournament score
Assistance Received: None
**********************************************************************/
Codes::ReturnCode Player::IncTournamentScore(int a_score) {
    if (a_score < 0) {
        return Codes::INVALID_INC;
    }
    m_tournamentScore += a_score;
    return Codes::SUCCESS;
}


/* Public Utility Functions */

/**********************************************************************
Function Name: CharToColor
Purpose: To convert a the character of a stone to a string representing
    the color in a more human readable format
Parameters:
    a_color, a character, the color of the stone
Return Value: A string representing the color of the stone
Assistance Received: None
**********************************************************************/
string Player::CharToColor(char a_color) {
    if (a_color == WHITE_CHAR) { return WHITE; }
    else if (a_color == BLACK_CHAR) { return BLACK; }
    else { return NO_COLOR; }
}

/**********************************************************************
Function Name: ColorToChar
Purpose: To convert a string representing the color of a stone to a
    character
Parameters:
    a_color, a const string reference, the color of the stone in plain
        english
Return Value: A character representing the color of the stone
Assistance Received: None
**********************************************************************/
char Player::ColorToChar(const string& a_color) {
    if (a_color == WHITE) { return WHITE_CHAR; }
    else if (a_color == BLACK) { return BLACK_CHAR; }
    else { return DEFAULT_COLOR; }
}


/* Protected Utility Functions */

/**********************************************************************
Function Name: BestMove
Purpose: To determine the best move for the computer to make, i.e.
    the strategy of the computer
Parameters:
    a_board, a const reference Board object, the current state of the board
    a_nextPlayer, a const reference Player object, the next player to move
Return Value: None
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
**********************************************************************/
void Player::BestMove(const Board& a_board, const Player& a_nextPlayer) {
    // Don't touch main board
    Board boardCopy = a_board;

    string currPosition;
    struct ComputerMove ourBest, theirBest;
    struct ComputerMove ourMove, theirMove;

    vector<struct ComputerMove> topMoves;
    // For every position on the board, play as ourselves, and play as the next player
    // Evaluate the move, and store the best move
    // If there are multiple moves with the same score, add it to a vector of top moves
    for (int row = 0; row < Board::BOARD_SIZE; row++) {
        for (int column = 0; column < Board::BOARD_SIZE; column++) {
            // I did not separate out this into another function as I believe ints more readable
            // in this case. I want to show that the current player is moving for itself,
            // and then the next player is moving for itself. Though worse of for modifiability,
            // I believe it's a good trade off.
            currPosition = Board::IndicesToString(row, column);

            /** Place for us **/
            if (boardCopy.PlaceStone(m_color, currPosition) != Codes::SUCCESS) {
                continue;
            }
            ourMove = EvaluateMove(boardCopy, *this);
            boardCopy.UndoMove();

            /** Place for them **/
            if (boardCopy.PlaceStone(a_nextPlayer.GetColor(), currPosition) != Codes::SUCCESS) {
                continue;
            }
            theirMove = EvaluateMove(boardCopy, a_nextPlayer);
            boardCopy.UndoMove();

            // Check to see if highest score
            if (ourMove.evalScore >= ourBest.evalScore) {
                ourBest = ourMove;
                topMoves.push_back(ourMove);
            }
            if (theirMove.evalScore >= theirBest.evalScore) {
                theirBest = theirMove;
                topMoves.push_back(theirMove);
            }
        }
    }

    // Determine the best move
    DetermineBest(boardCopy, ourBest, theirBest, topMoves);
}

/**********************************************************************
Function Name: GetReasonMessage
Purpose: To get the rationale for the move made by the computer in plain
    english
Parameters: None
Return Value: A string representing the rationale for the move
Assistance Received: None
**********************************************************************/
string Player::GetReasonMessage() const {
    string reason = " to ";
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
    return reason += "!";
}

/**********************************************************************
Function Name: EvaluateMove
Purpose: To evaluate a move made by a player, i.e. how good the move is
Parameters:
    a_board, a const reference Board object, the current state of the board
    a_player, a const reference Player object, the current players move
        we are evaluating
Return Value: A struct ComputerMove, containing the position, evaluation
    score, color, and reason for the move
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
**********************************************************************/
struct Player::ComputerMove Player::EvaluateMove(const Board& a_board, const Player& a_player) const {
    // Adjustable constants to put more emphasis on certain moves
    // We care the most about winning, then capturing, then building
    const int WIN_MULTI = 10000;
    const int CAPTURE_MULTI = 2000;
    const int BUILD_MULTI = 5;

    // Holds the evaluation score of the move, i.e. how good it is
    int evalScore = 0;

    struct ComputerMove move;
    move.position = a_board.GetLastPosition();
    move.color = a_player.GetColor();

    // Get where we are on the board
    int row, column;
    Board::ParsePosition(move.position, row, column);

    /** Win **/
    evalScore += WIN_MULTI * a_board.GetWinInARow();

    /** Building Blocks **/
    int blockCount = 0;
    for (int n = Board::WIN_SCORE - 1; n > 1; n--) {
        blockCount += a_board.GetNumNInARow(n, row, column) - a_board.GetWinInARow();
        evalScore += BUILD_MULTI * blockCount * n * n;
    }
    // Prioritize building block instead of preventing one from forming
    // Incentive for us to gain more points by building larger blocks
    if (blockCount > 0 && m_color == a_player.GetColor()) {
        evalScore += BUILD_MULTI;
    }

    /** Avoiding Captures **/
    // Only if we're the one moving - don't want to avoid captures on their turn
    if (evalScore < WIN_MULTI && m_color == a_player.GetColor()) {
        evalScore -= CAPTURE_MULTI * a_board.GetPotentialCaptures(a_player.m_color, row, column);
    }

    /** Capturing **/
    evalScore += CAPTURE_MULTI * a_board.GetCapturedPairs();

    // Easy way to see if what move we made is to check the eval score multipliers
    move.evalScore = evalScore;
    // Determine the reason for the move
    if (evalScore >= WIN_MULTI) {
        move.reason = WIN;
    }
    else if (evalScore >= CAPTURE_MULTI) {
        move.reason = CAPTURE;
    }
    else if (evalScore > 0) {
        move.reason = BUILD;
    }
    else {
        move.reason = UNKNOWN;
    }
    return move;
}

/**********************************************************************
Function Name: DetermineBest
Purpose: To set the best move based on the best moves evaluated
Parameters:
    a_board, a const reference Board object, the current state of the board
    ourBest, a const reference struct ComputerMove, the best move we made
    theirBest, a const reference struct ComputerMove, the best move the
        next player made
    topMoves, a reference vector of struct ComputerMove, these are the
        moves that were the best a certain time
Return Value: None
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
Assistance Received: None
**********************************************************************/
void Player::DetermineBest(const Board& a_board, const ComputerMove& ourBest,
                           const ComputerMove& theirBest, vector<ComputerMove>& topMoves) {
    // We want to win, so if we can win, we will
    if (ourBest.reason == WIN) {
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
    for (auto it = topMoves.begin(); it != topMoves.end(); ) {
        if (it->evalScore != m_bestMove.evalScore) {
            it = topMoves.erase(it);
        }
        else {
            it++;
        }
    }

    // If there are multiple moves with the same score, choose one at random
    // Helps give the computer a more "human" feel - would just place in the same location
    // every time
    if (topMoves.size() > 1) {
        int randIndex = rand() % topMoves.size();
        m_bestMove = topMoves[randIndex];
    }

    // Stone must be placed on center stone (0)
    if (a_board.GetOuterBounds() == 0) {
        m_bestMove.reason = BOARD_RESTRICTION;
    }

    // If second white move, set our specific values to a ring around center
    // We want to be close to the center in order to build blocks
    // Hardcoded as it's the easiest implementation (3 because must be 3 away)
    if (a_board.GetInnerBounds() == 3) {
        m_bestMove.reason = BOARD_RESTRICTION;
        const string VALUES[4] = {"J7", "M10", "J13", "G10"};
        int randIndex = rand() % 4;
        m_bestMove.position = VALUES[randIndex];
    }
}