//
//  Implementation of Board class - runs the game board
//

#include "Board.h"

/* Accessors */

/**********************************************************************
Function Name: GetGameBoardCopy
Purpose: To get a copy of 2D vector representing the game board
Parameters: None
Return Value: the game board, a 2D vector of characters
Assistance Received: None
**********************************************************************/
vector<vector<char>> Board::GetGameBoard() const {
    return m_gameBoard;
}

/**********************************************************************
Function Name: GetInnerBounds
Purpose: To get the inner bounds board restriction set on the board,
    i.e. the minimum distance a stone can be from the center stone
Parameters: None
Return Value: the inner bounds, an integer
Assistance Received: None
**********************************************************************/
int Board::GetInnerBounds() const {
    return m_currMove.innerBounds;
}

/**********************************************************************
Function Name: GetOuterBounds
Purpose: To get the outer bounds board restriction set on the board,
    i.e. the maximum distance a stone can be from the center stone
Parameters: None
Return Value: the outer bounds, an integer
Assistance Received: None
**********************************************************************/
int Board::GetOuterBounds() const {
    return m_currMove.outerBounds;
}


/**********************************************************************
Function Name: IsGameOver
Purpose: To check if the game is over, i.e. if there is a winner or
    the board is full
Parameters: None
Return Value: if the game is over, a boolean value
Assistance Received: None
**********************************************************************/
bool Board::IsGameOver() const {
    return (IsBoardFull() || IsWinner());
}

/**********************************************************************
Function Name: IsBoardFull
Purpose: To see if there is any space left on the board
Parameters: None
Return Value: if the board is full, a boolean value
Assistance Received: None
**********************************************************************/
bool Board::IsBoardFull() const {
    return m_currMove.intersectLeft <= 0;
}

/**********************************************************************
Function Name: IsWinner
Purpose: To see if the last move resulted in a win
Parameters: None
Return Value: if there is a winner, a boolean value
Assistance Received: None
**********************************************************************/
bool Board::IsWinner() const {
    return m_currMove.winInARow > 0;
}

/**********************************************************************
Function Name: GetIntersectLeft
Purpose: To get the number of spaces left on the board
Parameters: None
Return Value: The number of spaces left on the board, an integer
Assistance Received: None
**********************************************************************/
int Board::GetIntersectLeft() const {
    return m_currMove.intersectLeft;
}


/**********************************************************************
Function Name: GetWinInARow
Purpose: To get the amount times the last stone placed by the player
    has contributed to a win. E.g. 2 if the player has placed
    2 sets of the winning amount of stones in a row
Parameters: None
Return Value: The number of pairs captured, an integer
Assistance Received: None
**********************************************************************/
int Board::GetWinInARow() const {
    return m_currMove.winInARow;
}

/**********************************************************************
Function Name: GetCapturedPairs
Purpose: To get the amount of pairs captured by the last move
Parameters: None
Return Value: The number of pairs captured, an integer
Assistance Received: None
**********************************************************************/
int Board::GetCapturedPairs() const {
    return m_currMove.capturedPairs;
}

/**********************************************************************
Function Name: GetLastPosition
Purpose: To get the last move made by the player
Parameters: None
Return Value: The move made by the last player, a string
Assistance Received: None
**********************************************************************/
string Board::GetLastPosition() const {
    return m_currMove.position;
}


/* Mutators */

/**********************************************************************
Function Name: PlaceStone
Purpose: Facilitates the move made by player by placing a stone on the
    board. Checking if the move is valid and updates the board accordingly.
Parameters:
     a_color, a character. The color of the player making the move
     a_position, a constant string reference. The position the player
          wants to place their stone, e.g. "J10"
Return Value: The success of the move, a Codes::ReturnCode enum value
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
Assistance Received: None
**********************************************************************/
Codes::ReturnCode Board::PlaceStone(char a_color, const string& a_position) {
    // We could put the guard clauses to protect against invalid moves in its
    // own separate function to keep the logic seperated from the returns.
    // But if this function ever were to be modified, it would be unclear and
    // disjointed to have the guard clauses somewhere else - in terms of modifying
    // the logic or the guard clauses.

    // Parse the position to get the row and column index
    int row, column;
    // Need to be able to parse the position into valid row and column indices
    if (!ParsePosition(a_position, row, column)) {
        return Codes::COULD_NOT_PARSE;
    }
    // Position must be within the board
    if (!IsValidIndex(row, column)) {
        return Codes::INVALID_MOVE;
    }
    // Check if we have a restriction on where stone can be placed
    int distance = AwayFromCenter(row, column);
    if ((m_currMove.innerBounds > distance) || (distance > m_currMove.outerBounds)) {
        return Codes::INVALID_MOVE;
    }
    // Can't place stone if square is already occupied
    if (m_gameBoard[row][column] != NULL_PIECE) {
        return Codes::SPACE_OCCUPIED;
    }
    // Can't place a stone if there is a winner
    if (m_currMove.winInARow > 0) {
        return Codes::ALREADY_WINNER;
    }
    // If board is full, we cannot place a stone
    if (IsBoardFull()) {
        return Codes::FULL_BOARD;
    }


    // Place the stone on the board
    m_gameBoard[row][column] = a_color;
    // Store the sequences after placing the stone, but before capturing
    m_currMove.prevSeqs = ColorSeq(WIN_SCORE, row, column);
    m_currMove.intersectLeft--;
    m_currMove.position = a_position;

    // Check if the move resulted in a winner, or captured pairs
    m_currMove.winInARow = GetNumNInARow(WIN_SCORE, row, column);
    m_currMove.capturedPairs = CapturePairs(a_color, row, column);

    // Store the move on the stack, so we can undo if needed
    m_prevMoves.push(m_currMove);

    return Codes::SUCCESS;
}

/**********************************************************************
Function Name: UndoMove
Purpose: To undo the last move made by a player
Parameters: None
Return Value: The success of the undo, a Codes::ReturnCode enum value
Algorithm:
    1) Check if there are any moves to undo, can't undo if there are none
    2) Get the last move made by the player
    3) Undo the move by updating the board to the previous state
Assistance Received: https://cplusplus.com/reference/stack/stack/
**********************************************************************/
Codes::ReturnCode Board::UndoMove() {
    // Can't undo if there are no moves to undo
    if (m_prevMoves.empty()) {
        return Codes::NO_PREV_MOVES;
    }

    // Get the last move
    m_currMove = m_prevMoves.top();
    m_prevMoves.pop();

    // Get the row and column indices from the position. Row and column
    // Not stored in the move as we can get it from a method - we know its valid already
    int row, column;
    ParsePosition(m_currMove.position, row, column);

    // Undo the sequences - middle piece is stored at each direction so set it to null
    UpdateSeqs(m_currMove.prevSeqs, row, column);
    m_gameBoard[row][column] = NULL_PIECE;

    // Update the number of intersections left based on if there was a capture
    m_currMove.intersectLeft++;
    m_currMove.intersectLeft += m_currMove.capturedPairs * CAPTURE_NUM;

    // Reset the win and capture counts as we have undone the move
    // Can't have a win or capture there was no move made at this state
    m_currMove.winInARow = DEFAULT_SCORES;
    m_currMove.capturedPairs = DEFAULT_SCORES;

    return Codes::SUCCESS;
}

/**********************************************************************
Function Name: SetBoard
Purpose: To set a in-progress game board, i.e. a board from a saved/serialized game
Parameters:
    a_gameBoard, a 2D vector of character passed by value. Represents
        game board, with each element being the color of a player
        e.g. "W" for white
Return Value: The success of setting the board, a Codes::ReturnCode enum value
Algorithm:
    1) Check if the board is valid, i.e. square and of proper size
    2) Check if there is a winner, if so we cannot set the board as game is over
    3) Ensure board is not full as cannot place a any stones on a full board
    4) Set the board to the new board, update the state to reflect the new board
Assistance Received: None
**********************************************************************/
Codes::ReturnCode Board::SetBoard(const vector<vector<char>>& a_gameBoard) {
    // Guard Clauses to prevent setting invalid board, like before
    // I am not putting the guard clauses in their own function to ensure
    // the logic is not disjointed from the return statements.

    // Check if board is the correct size
    if (a_gameBoard.size() != BOARD_SIZE) {
        return Codes::INVALID_BOARD;
    }
    // Check if board is square
    for (const vector<char>& row : a_gameBoard) {
        if (row.size() != BOARD_SIZE) {
            return Codes::INVALID_BOARD;
        }
    }

    // Set our member to the board, so we can use its methods
    // But store a copy, so we can revert if something goes wrong
    vector<vector<char>> gameBoardCopy = m_gameBoard;
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
            if (a_gameBoard[row][column] == NULL_PIECE) {
                intersectLeft++;
                // Don't check for winner if piece is null
                continue;
            }

            // Check if we have a winner on select piece
            if (GetNumNInARow(WIN_SCORE, row, column) > 0) {
                // Put game board back to original state, as we cannot set board
                m_gameBoard = gameBoardCopy;
                return Codes::ALREADY_WINNER;
            }
        }
    }
    // If board is full, we cannot place a stone
    if (intersectLeft == 0) {
        // Put game board back to original state, as we cannot set board
        m_gameBoard = gameBoardCopy;
        return Codes::FULL_BOARD;
    }


    // Update members to reflect new board
    m_currMove.intersectLeft = intersectLeft;
    // We don't know last position, so we set it to default
    m_currMove.position = DEFAULT_LAST_POSITION;
    m_currMove.winInARow = DEFAULT_SCORES;
    m_currMove.capturedPairs = DEFAULT_SCORES;

    // Clear the previous moves as we have a new board and don't know last move
    m_prevMoves = stack<struct Move>();

    return Codes::SUCCESS;
}

/**********************************************************************
Function Name: SetBounds
Purpose: To set the inner and outer bounds of the board, i.e. the
    minimum and maximum distance a stone can be from the center stone
Parameters:
    a_innerBounds, an integer. The inner bounds of the board, if 0
        there is no restriction, if size of board cannot place anywhere
    a_outerBounds, an integer. The outer bounds of the board, if 0
        cannot place stone on any piece, if board size there is no restriction
Return Value: The success of setting the bounds, a Codes::ReturnCode enum value
Assistance Received: None
**********************************************************************/
Codes::ReturnCode Board::SetBounds(int a_innerBounds, int a_outerBounds) {
    // Bounds must be within the board size, can't be negative or above the board
    if ((a_innerBounds > BOARD_SIZE || a_innerBounds < 0) ||
        (a_outerBounds > BOARD_SIZE || a_outerBounds < 0)) {
        return Codes::INVALID_BOUNDS;
    }

    m_currMove.innerBounds = a_innerBounds;
    m_currMove.outerBounds = a_outerBounds;
    return Codes::SUCCESS;
}


/* Public Utility Functions */

/**********************************************************************
Function Name: GetNumNInARow
Purpose: To get the number of sequences of n stones in a row on a specific
    intersection on the board. E.g. 2 if there are 2 sequences of 5 in a
    row in two directions on the intersection.
Parameters:
    a_n, an integer. The number of stones out from the intersection to check for
    a_row, an integer. The row index of the intersection to check
    a_column, an integer. The column index of the intersection to check
Return Value: The number of sequences of n stones in a row, an integer
Algorithm:
    1) Check if we can check for n in a row, i.e. n > 1. A sequence of 1 is just a
        stone on the board
    2) Add the number of stone sequences in each direction together based on their
        cardinal planes: horizontal, vertical and the two diagonals
    3) Divide by n to account for double counting in the cardinal planes
Assistance Received: None
**********************************************************************/
int Board::GetNumNInARow(int a_n, int a_row, int a_column) const {
    // Can't check for less than 2 in a row - causes problems with double counting
    const int LOWER_LIMIT = 2;

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

/**********************************************************************
Function Name: GetUninterStones
Purpose: To get the uninterrupted amount of stones on the entire board from
    a certain stone color and a select number.
Parameters:
    a_n, an integer. The number of stones in a row to check for
    a_color, a character. The color of the stones to check for
Return Value: The number of uninterrupted stones on the board, an integer
Algorithm:
    1) Check if a_n is within the bounds of the board and not 0,
        can't check for 0 stones in a row
    2) Loop through every intersection on the board where the stone color
        matches
    3) Get the number of stones in a row in each cardinal place, e.g. horizontal,
        vertical and the two diagonals at a_n. We cannot share the same
        direction if a stone sequence is greater than a_n. We can share
        other directions, but not the same one
Assistance Received: None
**********************************************************************/
int Board::GetUninterStones(int a_n, char a_color) const {
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
            if (m_gameBoard[row][column] != a_color) {
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

/**********************************************************************
Function Name: GetPotentialCaptures
Purpose: To get the number of potential captures at a specific intersection
    on the board that can happen in the next ply of the game. E.g.
    OBBW -> 1 potential capture.
Parameters:
    a_color, a character. The color of the stones to check for
    a_row, an integer. The row index of the intersection to check
    a_column, an integer. The column index of the intersection to check
Return Value: The number of potential captures on a specific intersection, an integer
Algorithm:
    1) Create a pattern to match for potential captures, i.e. a sequence of
        stones that can be captured in the next ply
    2) Get the sequences of stones in each cardinal direction
    3) Check if the sequences match the pattern, if so we have a potential capture
Assistance Received: https://regex101.com
**********************************************************************/
int Board::GetPotentialCaptures(char a_color, int a_row, int a_column) const {
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
    const string REGEX_HEADER = "[^" + string(1, NULL_PIECE) + "^" + string(1, a_color) + "]";

    // Then create the capture sequence: NULL_PIECE{CAPTURE_NUM}
    string capSeq(1, NULL_PIECE);
    for (int i = 0; i < CAPTURE_NUM; i++) {
        capSeq += a_color;
    }
    // Get the reverse of it, to get the other combination
    string revCapSeq = capSeq;
    reverse(revCapSeq.begin(), revCapSeq.end());
    // Add the header to the capture sequence, and the reverse capture sequence
    capSeq = capSeq + REGEX_HEADER;
    revCapSeq = REGEX_HEADER + revCapSeq;
    // Put it into a patter
    regex pattern = regex(capSeq + "|" + revCapSeq);

    // Now we need to get the cardinal directions, e.g. horizontal, vertical and the two diagonals
    const int PLANES = NUM_DIRECTIONS / 2;
    // Our sequence length is the capture number + 1, as we need to check 3 intersections away
    // in each direction
    const int SEQ_LENGTH = CAPTURE_NUM + 1;
    vector<string> directionSeqs = ColorSeq(SEQ_LENGTH, a_row, a_column);
    string firstSeq, secondSeq;
    for(int direction = 0; direction < PLANES; direction++) {
        // Remove the same middle stone from the sequence, as we don't want to double count
        firstSeq = directionSeqs[direction];
        firstSeq.erase(0, 1);
        // Get the second sequence, which we need to reverse as we want to check the other side
        secondSeq = directionSeqs[direction + PLANES];
        reverse(secondSeq.begin(), secondSeq.end());

        // If hit a match, that means we have 1 potential capture in next ply
        if (regex_search(secondSeq + firstSeq, pattern)) {
            count++;
        }
    }
    return count;
}


/**********************************************************************
Function Name: ParsePosition
Purpose: Convert the string/move input to a valid row and column
    index for the game board. Takes in position, e.g. "J10" and parses
    it to information the board can use, e.g. row = 9, column = 9
Parameters:
    a_position, a constant string reference. It refers to the position the
        player wants to place their stone, e.g. "J10"
    a_row, an integer passed by reference. It is the row index of the
        move. Sets the value of the row index based on a_position.
    a_column, an integer passed by reference. It is the column index of the
        move. Sets the value of the column index based on a_position.
Return Value: The success of the placing parsing the move, a boolean value
Algorithm:
    1) Check if position meets length requirements to be valid
    2) If row is not a number, we cannot parse
    3) If column is not a letter, we cannot parse
Assistance Received: None
**********************************************************************/
bool Board::ParsePosition(const string& a_position, int& a_row, int& a_column) {
    // Starting index on the move/position format
    const int ROW_START_INDEX = 1;
    const int COLUMN_START_INDEX = 0;
    // Position string must be of certain length to be valid
    const int MIN_LENGTH = 2;
    const int MAX_LENGTH = 3;

    // Guard clauses for invalid parse
    // Check if position is valid length
    if (a_position.length() < MIN_LENGTH || a_position.length() > MAX_LENGTH) {
        return false;
    }
    // Clause protects stoi from throwing error, row must be a number
    // Use substring as could more than one digit
    for (char element: a_position.substr(ROW_START_INDEX)) {
        if (!isdigit(element)) {
            return false;
        }
    }
    // Check if first character is a letter as column must be a letter
    if (!isalpha(a_position[COLUMN_START_INDEX])) {
        return false;
    }

    // Convert the row and column to their respective indices based on
    // their offsets. Row is int + 1 and column is char, so we need to convert.
    a_row = stoi(a_position.substr(ROW_START_INDEX)) - ROW_OFFSET;
    a_column = toupper(a_position[COLUMN_START_INDEX]) - COLUMN_OFFSET;

    // Parse was successful
    return true;
}

/**********************************************************************
Function Name: IndicesToString
Purpose: Convert row and columns to their corresponding position
    string, e.g. row = 9, column = 9 -> "J10"
Parameters:
    a_row, an integer. The row index of the move
    a_column, an integer. The column index of the move
Return Value: The position as a string
Assistance Received: None
**********************************************************************/
string Board::IndicesToString(int a_row, int a_column) {
    // Offset the row and column to their corresponding position
    int row = a_row + ROW_OFFSET;
    char column = (char)(a_column + COLUMN_OFFSET);

    // Convert the row to a string and concatenate with the column
    string position = column + to_string(row);
    return position;
}

/**********************************************************************
Function Name: IsValidIndex
Purpose: To check if a row and column index is valid for the game board,
    i.e. within the bounds of the board
Parameters:
    a_row, an integer. The row index of the move
    a_column, an integer. The column index of the move
Return Value: If the row and column index is valid, a boolean value
Assistance Received: None
**********************************************************************/
bool Board::IsValidIndex(int a_row, int a_column) {
    return (a_row >= 0 && a_row < BOARD_SIZE) && (a_column >= 0 && a_column < BOARD_SIZE);
}

/**********************************************************************
Function Name: AwayFromCenter
Purpose: To check how far a row and column index is from the center stone
Parameters:
    a_row, an integer. The row index of the move
    a_column, an integer. The column index of the move
Return Value: The distance from the center stone Chebyshev distance, an integer
Assistance Received: https://en.wikipedia.org/wiki/Chebyshev_distance
**********************************************************************/
int Board::AwayFromCenter(int a_row, int a_column) {
    // Chebyshev distance given by: max(abs(x1 - x2), abs(y1 - y2))
    const int CENTER_INDEX = BOARD_SIZE / 2;
    return max(abs(a_row - CENTER_INDEX), abs(a_column - CENTER_INDEX));
}


/* Private Utility Functions */

/**********************************************************************
Function Name: CapturePairs
Purpose: To capture pairs of stones on the board based on the last move
    made by the player. E.g. if the last move was WBBW, we capture the
    pair of BB, and set new stone sequence to WOOW.
Parameters:
    a_color, a character. The color of the player making the move
    a_row, an integer. The row index of the move
    a_column, an integer. The column index of the move
Return Value: The number of pairs captured, an integer
Algorithm:
    1) Get the sequences of stones in each direction, not cardinal
        as we don't need to check for captures in the same direction
    2) See if the sequence is the proper length and forms a pair
    3) If so, capture the pair by setting the sequence to null stones
Assistance Received: None
**********************************************************************/
int Board::CapturePairs(char a_color, int a_row, int a_column) {
    // Holds the number of pairs captured
    int capturedPairs = 0;

    // Length of sequence to check for capture
    // We need to add 2 to the capture number as we need to check the first and last stone
    const int SEQ_LENGTH = CAPTURE_NUM + 2;

    vector<string> directionSeqs = ColorSeq(SEQ_LENGTH, a_row, a_column);

    // Store seq by reference as we need to update the board
    for (string& seq : directionSeqs) {
        // Guard clauses to prevent invalid captures
        // If not the proper length, we cannot have a capture
        if (seq.length() != SEQ_LENGTH) {
            continue;
        }
        // If the first and last stone are not the same color, we cannot have a capture
        char firstStone = seq[0], lastStone = seq[seq.length()-1];
        if (firstStone != lastStone) {
            continue;
        }

        // Get the middle of the sequence
        string captureSeq = seq.substr(1, seq.length() - 2);
        // If the sequence contains all the same stones and the first stone is not the same color as the player
        if (CountSameStones(captureSeq) == CAPTURE_NUM && captureSeq[0] != a_color) {
            capturedPairs++;
            seq = firstStone + string(captureSeq.length(), NULL_PIECE) + lastStone;
            // Update the number of intersections left based on the number of captures
            m_currMove.intersectLeft += CAPTURE_NUM;
        }
    }
    // Update the board based on new sequences
    UpdateSeqs(directionSeqs, a_row, a_column);

    return capturedPairs;
}

/**********************************************************************
Function Name: CardinalCount
Purpose: To get count of the same colors stones in a sequence at each
    cardinal direction, i.e. horizontal, vertical and the two diagonals
Parameters:
    a_n, an integer. The number of stones out from the intersection to check for
    a_row, an integer. The row index of the intersection to check
    a_column, an integer. The column index of the intersection to check
Return Value: The number of stones in a row at each cardinal direction,
    a vector of integers
Algorithm:
    1) Get the sequences of stones in each direction
    2) Count the number of stones in a row in each direction
    3) Combine the opposite directions, e.g. horizontal and vertical
Assistance Received: None
**********************************************************************/
vector<int> Board::CardinalCount(int a_n, int a_row, int a_column) const {
    // Represents cardinal directions
    const int PLANES = NUM_DIRECTIONS / 2;
    vector<int> counts(PLANES, 0);

    vector<string> seqs = ColorSeq(a_n, a_row, a_column);

    // Need to combine the 8 directions into 4, i.e. combine the opposite directions
    int firstSeqCount, secondSeqCount;
    for(int direction = 0; direction < PLANES; direction++) {
        // Add cardinal and ordinal directions together
        // We return the number of times we have this sequence
        // We do not check if there is more than n - not our problem
        firstSeqCount = CountSameStones(seqs[direction]);
        secondSeqCount = CountSameStones(seqs[direction + PLANES]);
        counts[direction] = firstSeqCount + secondSeqCount;
    }

    return counts;
}

/**********************************************************************
Function Name: CountSameStones
Purpose: To count the number of uninterrupted stones in a row in a sequence
Parameters:
    a_seq, a constant string reference. The sequence of stones to check
Return Value: The number of stones in a row in a sequence, an integer
Assistance Received: None
**********************************************************************/
int Board::CountSameStones(const string& a_seq) {
    // All sequences should be starting from closest to center stone
    // Holds the number of stones in a row
    int count = 0;
    // Check if all elements in the vector are the same
    for (int i = 0; i < a_seq.size(); i++) {
        // Stop when we hit a stone that isn't the same - or if null piece
        // Counts the middle piece color - we don't care what color it is
        if (a_seq[i] != a_seq[0] || a_seq[i] == NULL_PIECE) {
            break;
        }
        // Increment count if we have the same stone
        count++;
    }
    return count;
}

/**********************************************************************
Function Name: ColorSeq
Purpose: To get the sequence of stones in each direction from a specific
    intersection on the board.
Parameters:
    a_n, an integer. The number of stones out from the intersection to check for
    a_row, an integer. The row index of the intersection to check
    a_column, an integer. The column index of the intersection to check
Return Value: The sequence of stones in each direction, a vector of strings
Algorithm:
    1) For each direction get the offset based on the delta of the direction
    2) If offset is not valid, we cannot get the sequence
    3) Get the sequence of stones in each direction
Assistance Received: None
**********************************************************************/
vector<string> Board::ColorSeq(int a_n, int a_row, int a_column) const {
    // Can use a pointer to the stones on the board and put into a vector -
    // but to make it easier to maintain and read, it's better to use a
    // non-destructive approach. Even if it makes it harder to set the
    // stones to null when capturing
    vector<string> colorSeq(NUM_DIRECTIONS, "");

    int currRow = a_row, currColumn = a_column;
    string currSeq;
    // Sequence starts from center stone and goes outwards
    for (int direction = 0; direction < NUM_DIRECTIONS; direction++) {
        currSeq = "";
        // Will start on the current row and column
        for (int step = 0; step < a_n; step++) {
            currRow = a_row, currColumn = a_column;
            // Check if we are at a valid index for our row and column
            if (!OffsetIndices(currRow, currColumn, direction, step)) {
                break;
            }
            // Add the current stone at the index to the sequence
            currSeq += m_gameBoard[currRow][currColumn];
        }
        colorSeq[direction] = currSeq;
    }

    return colorSeq;
}

/**********************************************************************
Function Name: UpdateSeqs
Purpose: To update the board based on the sequences of stones in each
    direction from a specific intersection on the board.
Parameters:
    a_seq, a constant vector of strings reference. The sequence of stones
        in each direction to update the board with
    a_row, an integer. The row index of the intersection to update
    a_column, an integer. The column index of the intersection to update
Return Value: The success of updating the sequences, a boolean value
Algorithm:
    1) Check if the size of the sequence vector is the proper length
    2) For every direction in the sequence, update the board based on it
Assistance Received: None
**********************************************************************/
bool Board::UpdateSeqs(const vector<string>& a_seq, int a_row, int a_column) {
    // Something went horribly wrong - sequences must include all directions
    if (a_seq.size() != NUM_DIRECTIONS) {
        return false;
    }

    int currRow, currColumn;
    for (int direction = 0; direction < NUM_DIRECTIONS; direction++) {
        for (int step = 0; step < a_seq[direction].length(); step++) {
            currRow = a_row, currColumn = a_column;
            // Check if we are at a valid index for our row and column
            // If it's not, something is wrong with the sequence
            if (!OffsetIndices(currRow, currColumn, direction, step)){
                return false;
            }
            // Update the board based the current stone
            m_gameBoard[currRow][currColumn] = a_seq[direction][step];
        }
    }
    return true;
}

/**********************************************************************
Function Name: OffsetIndices
Purpose: To offset the row and column indices based on the direction
    and step. E.g. if direction is 0 and step is 2, we offset the row
    by 2 and column by 0.
Parameters:
    a_row, an integer passed by reference. The row index of the intersection
        to offset
    a_column, an integer passed by reference. The column index of the intersection
        to offset
    a_direction, an integer. The direction to offset the indices
    a_step, an integer. The number of steps to offset the indices
Return Value: If the offset indices are valid, a boolean value
Assistance Received: None
**********************************************************************/
bool Board::OffsetIndices(int& a_row, int& a_column, int a_direction, int a_step) {
    a_row = a_row + (ROW_DELTA[a_direction] * a_step);
    a_column = a_column + (COLUMN_DELTA[a_direction] * a_step);

    return IsValidIndex(a_row, a_column);
}


/**********************************************************************
Function Name: InitGameBoard
Purpose: To initialize the game board to a 2D vector of null pieces
Parameters: None
Return Value: The initialized game board, a 2D vector of characters
Assistance Received: None
**********************************************************************/
vector<vector<char>> Board::InitGameBoard() {
    return vector<vector<char>>(BOARD_SIZE, vector<char>(BOARD_SIZE, NULL_PIECE));
}