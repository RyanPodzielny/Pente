//
//  Round implementation of Pente
//

#include "Round.h"

/* Accessors */

/**********************************************************************
Function Name: GetRoundBoard
Purpose: To get a copy of the round's current board state
Parameters: None
Return Value: The current round's board, a Board object
Assistance Received: None
**********************************************************************/
Board Round::GetRoundBoard() const {
    return m_board;
}

/**********************************************************************
Function Name: GetNextPlayer
Purpose: To get the next player whose ply it is to make a move
Parameters: None
Return Value: The next player whose ply it is to make a move, a copy of
    the Player object
Assistance Received: None
**********************************************************************/
Player Round::GetNextPlayer() const {
    return *m_players[NextPlayerIndex(m_currPlayerIndex)];
}


/* Mutators */

/**********************************************************************
Function Name: Play
Purpose: To play a round of Pente, the game loop for the round
Parameters: None
Return Value: A ReturnCode enum value, indicating the status of the round
Algorithm:
    1) Reset the players if we are on a serialized game
    2) Set the ply order of the players, ask for a coin toss if
        tournament scores are tied
    3) Set a board restriction based on the ply count, i.e.
        where a player can place their stone
    4) Facilitate a ply, i.e. ask the current player to make a move
    5) Output the results of what the ply did to the board,
        and what the current scores are
    6) Ask the controller if they want to serialize the game
    7) Tally up the scores for the round and output the results,
        i.e. who won the round and how many points they got
Assistance Received: None
**********************************************************************/
Codes::ReturnCode Round::Play() {
    // Total number of moves made, tracks if we need to set a bounds
    int plyCount = 0;

    if (!m_isSerializedGame) {
        // Ensure players have 0 captured pairs and colors are removed
        // And order the players based on their tournament scores
        Reset();
        SetPlyOrder();
    }
    else {
        // If we are playing a serialized game, we need to determine the ply
        // Game could have been serialized at any point, so we need to determine
        // if we need bounds or not
        plyCount = DeterminePly();
        // Show the current scores
        OutputEndPly();
    }

    BoardView::PrintBoard(m_board);
    Player* currPlayer = m_players[m_currPlayerIndex];
    for ( ; ; ){
        SetBoardRestriction(plyCount);

        // If our ply results in an endgame, break out of the loop
        if (FacilitatePly(currPlayer)) {
            break;
        }

        // Ask for serialization from controller of the game
        // As long as the controller doesn't want to serialize, continue the game
        if (AskToSerialize()) {
            return Codes::SERIALIZE;
        }

        // Get the next player whose ply it is to make a move
        m_currPlayerIndex = NextPlayerIndex(m_currPlayerIndex);
        currPlayer = m_players[m_currPlayerIndex];
        plyCount++;
    }

    // Round ends, tally up scores
    // The current player is the winner of the game, as they were the last to make a move
    // That or game ended in tie
    TallyScores();

    return Codes::ROUND_END;
}

/**********************************************************************
Function Name: SetGameState
Purpose: To set the round's state from a serialized game
Parameters:
    a_board, a constant reference to a Board object, the board state
        of the serialized game
    a_players, a constant reference to a vector of Player pointers,
        the players of the serialized game
Return Value: A ReturnCode enum value, indicating where the round's
    state was set successfully
Algorithm:
    1) Ensure the players are valid, i.e. not null, not the same player,
        and have different colors
    2) Set the round's state from the serialized game
    3) Let the object know we are playing a serialized game
Assistance Received: None
**********************************************************************/
Codes::ReturnCode Round::SetGameState(const Board& a_board, const vector<Player*>& a_players) {
    // Players cannot be null, nor can they be the same player - can't play a game with yourself
    // Must have different colors so ply's make sense, and be no greater than our number of players
    // in our current implementation
    if (a_players.size() != 2) {
        return Codes::INVALID_PLAYER;
    }
    for (int i = 0; i < a_players.size(); i++) {
        if (a_players[i] == nullptr) {
            return Codes::NULL_PLAYER;
        }
        for (int j = i + 1; j < a_players.size(); j++) {
            if (a_players[i] == a_players[j]) {
                return Codes::SAME_COLOR;
            }
        }
    }

    // Update the round's state from the serialized game
    m_board = a_board;
    m_players = a_players;

    // Let the round we are playing a serialized game
    m_isSerializedGame = true;
    m_currPlayerIndex = DEFAULT_START_INDEX;

    return Codes::SUCCESS;
}

/**********************************************************************
Function Name: Reset
Purpose: To reset the round's state, i.e. start a new round
Parameters: None
Return Value: A ReturnCode enum value, indicating where the round's
    state was reset successfully
Algorithm:
    1) Reset colors and captured pairs for each player
    2) Reset the board
Assistance Received: None
**********************************************************************/
Codes::ReturnCode Round::Reset() {
    // Reset the players for our new round
    for (Player* player : m_players) {
        player->ResetCapturedPairs();
        player->SetColor(Player::DEFAULT_COLOR);
    }

    m_board = Board();

    // Let the round know we are not playing a serialized game
    m_isSerializedGame = DEFAULT_SERIALIZED;
    m_currPlayerIndex = DEFAULT_START_INDEX;

    return Codes::SUCCESS;
}


/* Public Utility Functions */

/**********************************************************************
Function Name: OutputEndPly
Purpose: To output the end of a ply, i.e. the captured pairs and
    tournament scores of each player
Parameters: None
Return Value: None
Assistance Received: None
**********************************************************************/
void Round::OutputEndPly() const {
    cout << endl;
    cout << "Captured Pairs:" << endl;
    for (Player* player : m_players) {
        cout << "\t" << player->GetNameAndColor() << ": " << player->GetCapturedPairs() << endl;
    }
    cout << "Tournament scores:" << endl;
    for (Player* player : m_players) {
        cout <<  "\t" << player->GetNameAndColor() << ": " << player->GetTournamentScore() << endl;
    }
    cout << endl;
}


/* Private Utility Functions */

/**********************************************************************
Function Name: SetPlyOrder
Purpose: To set the ply order of the players, i.e. who goes first
Parameters: None
Return Value: None
Algorithm:
    1) If the tournament scores are tied, ask the human to call a coin
        toss and compare the result. This will occur if first move as well
    2) If the human wins, they go first, otherwise the computer goes first
    3) If the tournament scores are not tied, arrange the players in
        order of who goes first by tournament score
    4) Set the colors of the players based on who goes first
    5) Let the user know who goes first
Assistance Received: None
**********************************************************************/
void Round::SetPlyOrder() {
    // Output inserted with logic/model to avoid complex output functions
    // To remove view from model, we'd need to store coin toss winner and output in controller
    // Would easily add 30 lines of code for a simple output

    // Tournament scores are tied, so we need a coin flip to determine who goes first
    if (m_human->GetTournamentScore() == m_computer->GetTournamentScore()) {
        cout << endl << "Tournament scores are tied! Performing coin flip...";
        // Ask the human to call the toss and compare the result
        // If the human wins, they go first, otherwise the computer goes first
        // Literal constants 0 and 1 are positions on the player vector
        if (m_human->CallToss()) {
            cout << "You won the coin toss! You are white and will go first.";
            m_players[0] = m_human;
            m_players[1] = m_computer;
        }
        else {
            cout << "You lost the coin toss! You are black and computer will go first.";
            m_players[0] = m_computer;
            m_players[1] = m_human;
        }
    }
    // Whoever has more tournament points goes first
    else {
        // Arrange the players in order of who goes first by tournament score
        SortScores();
        // Let the user know who goes first
        cout << m_players[0]->GetName() << " goes first as they have the highest tournament score with "
             << m_players[0]->GetTournamentScore() << " points";
    }

    // Set colors based on who goes first
    for (int i = 0; i < m_players.size(); i++) {
        m_players[i]->SetColor(COLOR_PRECEDENCE[i]);
    }

    cout << endl;
}

/**********************************************************************
Function Name: SortScores
Purpose: To sort the players in order of who goes first by tournament score
Parameters: None
Return Value: None
Algorithm:
    1) Sort the players in order of who goes first by tournament score
    2) Update the players colors accordingly based on the color precedence
Assistance Received: https://cplusplus.com/reference/algorithm/sort/
**********************************************************************/
void Round::SortScores() {
    // The player with the highest tournament score goes first, sort to get ply order
    sort(m_players.begin(), m_players.end(), [](Player* a, Player* b) {
        return a->GetTournamentScore() > b->GetTournamentScore();
    });

    // Update the players colors accordingly based on the color precedence
    for (int currPlayer = 0; currPlayer < NUM_PLAYERS; currPlayer++) {
        m_players[currPlayer]->SetColor(COLOR_PRECEDENCE[currPlayer]);
    }
}

/**********************************************************************
Function Name: DeterminePly
Purpose: To estimate the ply of the game, i.e. how many moves have been
    made. This is used for when board is serialized, as we need to know
    if we need to set a board restriction or not.
Parameters: None
Return Value: The ply estimate of the game, an integer
Assistance Received: None
**********************************************************************/
int Round::DeterminePly() const {
    const int MIN_PLY = 3;

    // If there are any captured pairs we know we are past the first 3 plys
    for (Player* player : m_players) {
        if (player->GetCapturedPairs() > 0) {
            return MIN_PLY;
        }
    }

    // Return total pieces on board - rough estimate
    // In actuality we just need to know if it's less than 3 for setting bounds
    return (Board::BOARD_SIZE * Board::BOARD_SIZE) - m_board.GetIntersectLeft() ;
}

/**********************************************************************
Function Name: SetBoardRestriction
Purpose: To set the board restriction, i.e. where a player can place
    their stone, based on the ply count
Parameters:
    a_plyCount, an integer, the ply count of the game
Return Value: None
Algorithm:
    1) Set the board restriction based on the plyS count
    2) Output where the players can move
Assistance Received: None
**********************************************************************/
void Round::SetBoardRestriction(int a_plyCount) {
    // Output here to avoid complex output functions
    switch (a_plyCount) {
        // First move must be placed in the center of the board
        case 0:
            m_board.SetBounds(0, 0);
            cout << "First white move must be placed on the center of the board at "
                 << Board::CENTER_POSITION << "!" << endl;
            break;
            // Second move must be placed at least 3 stones away from the center of the board
        case 2:
            m_board.SetBounds(3, Board::BOARD_SIZE);
            cout << "Second white move must be placed at least 3 stones away from the center of the board at "
                 << Board::CENTER_POSITION << "!" << endl;
            break;
            // All other moves have no restrictions
        default:
            m_board.SetBounds(0, Board::BOARD_SIZE);
            break;
    }
    // Formatting
    cout << endl;
}

/**********************************************************************
Function Name: FacilitatePly
Purpose: To facilitate a ply, i.e. ask the current player to make a move
    incrementing their captured pairs if they captured if ply resulted in one
Parameters:
    a_currPlayer, a pointer to a Player object, the current player whose
        ply it is to make a move
Return Value: A boolean, true if the ply resulted in an endgame, false
Algorithm:
    1) Ask the current player to make a move
    2) Output the results of what the ply did to the board, and what the
        current scores are
    3) See if there is a winner, or if the board is full
    4) Output the scores if there are any
Assistance Received: None
**********************************************************************/
bool Round::FacilitatePly(Player* a_currPlayer) {
    cout << a_currPlayer->GetNameAndColor() << "'s turn:" << endl;
    a_currPlayer->MakeMove(m_board, GetNextPlayer());
    BoardView::PrintBoard(m_board);

    cout << a_currPlayer->GetNameAndColor() << ", placed a stone at "
         << m_board.GetLastPosition() << "!" << endl;

    a_currPlayer->IncCapturedPairs(m_board.GetCapturedPairs());

    if (m_board.GetCapturedPairs() > 0) {
        cout << a_currPlayer->GetNameAndColor() << ", captured "
             << m_board.GetCapturedPairs() << " pair(s)!" << endl;
    }

    // See if there is a winner, or if the board is full
    if(CheckRoundEnd(a_currPlayer)) {
        return true;
    }

    OutputEndPly();

    return false;
}

/**********************************************************************
Function Name: NextPlayerIndex
Purpose: To get the index of the next player, i.e. the player whose ply
    it is to make a move
Parameters:
    a_playerIndex, an integer, the index of the current player
Return Value: The index of the next player, an integer
Assistance Received: None
**********************************************************************/
int Round::NextPlayerIndex(int a_playerIndex) const {
    return (a_playerIndex + 1) % NUM_PLAYERS;
}

/**********************************************************************
Function Name: CheckRoundEnd
Purpose: To check if the round has ended, i.e. if there is a winner or
    if the board is full
Parameters:
    a_currPlayer, a pointer to a Player object, the current player whose
        ply it is to make a move
Return Value: A boolean, true if the round has ended, false otherwise
Algorithm:
    1) See if player placed five in a row (will be tallied in another function)
    2) See if player captured 5 or more pairs
    3) See if board is full, i.e. a tie
    4) Set the winner of the round if there is one
Assistance Received: None
**********************************************************************/
bool Round::CheckRoundEnd(Player* a_currPlayer) {
    // If there is a winner, or the board is full, end the round
    bool endRound = false;
    // Holds the message to output at the end of the round
    string endMessage;

    // See if player placed five in a row (will be tallied in another function)
    int win = m_board.GetWinInARow();
    if (win > 0) {
        m_numWinInARow = win;
        m_winner = a_currPlayer;
        endMessage = a_currPlayer->GetNameAndColor() + " has won the round by placing " +
                     to_string(WIN_SCORE) + " stones in a row!";
        endRound = true;
    }
    // See if player captured 5 or more pairs
    if (a_currPlayer->GetCapturedPairs() >= WIN_SCORE) {
        m_winner = a_currPlayer;
        endMessage = a_currPlayer->GetNameAndColor() + " has won the game by capturing " +
                     to_string(a_currPlayer->GetCapturedPairs()) + " pairs!";
        endRound = true;
    }

    // See if board is full, i.e. a tie
    if (m_board.IsBoardFull()) {
        m_winner = nullptr;
        endMessage = "The board is full! The round ends in a tie!";
        endRound = true;
    }

    // Output round end in a nice format
    if (endRound) {
        cout << endl << endMessage << endl;
    }

    return endRound;
}

/**********************************************************************
Function Name: AskToSerialize
Purpose: To ask the controller if they want to serialize the game
Parameters: None
Return Value: A boolean, true if the controller wants to serialize the
    game, false otherwise
Assistance Received: None
**********************************************************************/
bool Round::AskToSerialize() const {
    // Allow user to input characters as well as full word
    const string YES = "YES", NO = "NO";
    const string Y = "Y", N = "N";
    string serialize;
    // Validate input
    do {
        cout << "Would you like to serialize/save and end the game (yes/no)?" << endl << "> ";
        getline(cin, serialize);
        for (char& c : serialize) { c = (char) toupper(c); }
    } while (serialize != YES && serialize != Y && serialize != NO && serialize != N);

    return (serialize == YES || serialize == Y);
}

/**********************************************************************
Function Name: TallyScores
Purpose: To tally up the scores for the round and output the results,
    i.e. who won the round and how many points they got
Parameters: None
Return Value: None
Algorithm:
    1) Determine if tie or win
    2) Add 5 in a row scores to the winner
    3) Add 1 point for each pair captured to each player
    4) Add 1 point for each set of 4 uninterrupted stones to each player
    5) Output the end scores
Assistance Received: None
**********************************************************************/
void Round::TallyScores() {
    // Once again we could remove the output from the model, but it becomes hard to read
    cout << endl << "Score Details:" << endl;

    // Add 5 in a row scores
    if (m_numWinInARow > 0 && m_winner != nullptr) {
        // Winner receives 5 points for every 5 in a row they got in each direction
        m_winner->IncTournamentScore(m_numWinInARow * WIN_SCORE);
        cout << "Added " << WIN_SCORE << " points " << m_numWinInARow << " time(s) to "
             << m_winner->GetNameAndColor() << ", for placing " << WIN_SCORE
             << " stones in a row, winning the round!" << endl;
    }

    // Take all players and add their points to their tournament score
    // Outputting the points added to each player
    int pairsCaptured, fourInARow;
    for (Player* player : m_players) {
        // Add 1 point for each pair captured
        pairsCaptured = player->GetCapturedPairs();
        player->IncTournamentScore(player->GetCapturedPairs());
        if (pairsCaptured > 0) {
            cout << "Added " << player->GetCapturedPairs() << " point(s) to " << player->GetNameAndColor()
                 << ", for capturing " << player->GetCapturedPairs() << " pair(s)!" << endl;
        }

        // Add 1 point for each set of 4 uninterrupted stones
        fourInARow = m_board.GetUninterStones(STRAIGHT_STONES, player->GetColor());
        // If player has no uninterrupted stones, continue (literal constant 0, for readability)
        player->IncTournamentScore(fourInARow);
        if (fourInARow > 0) {
            cout << "Added " << fourInARow << " point(s) to " << player->GetNameAndColor()
                 << ", for having " << fourInARow << " set(s) of " << STRAIGHT_STONES
                 << " uninterrupted stones at the end of the round!" << endl;
        }
    }

    // Output the end scores
    cout << endl << "End scores:";
    OutputEndPly();
}