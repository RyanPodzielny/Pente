//
//  Tournament implementation
//

#include "Tournament.h"

/* Mutators */

/**********************************************************************
Function Name: Start
Purpose: To start the Pente tournament, having a game loop for the round
Parameters: None
Return Value: Codes::ReturnCode - the exit status of the tournament
Algorithm:
    1) Create round and our players
    2) See if controller wants to load a saved game - load if so
    3) Start the round loop until controller wants to save or exit,
        resetting the round for every new one played
    4) Output the end results of the tournament
Assistance Received: None
**********************************************************************/
Codes::ReturnCode Tournament::Start() {
    // Create round object with our players
    m_human = Human(), m_computer = Computer();
    Round round = Round(&m_human, &m_computer);

    // Record if we are resuming or starting a new game for output
    string gameState = "started";
    vector<vector<char>> savedBoard;
    // Check if we need to load a game
    if (LoadGame(savedBoard)) {
        // Reverse the board to get the correct order as we save upside down
        // Rows go from 1-19 bottom up
        reverse(savedBoard.begin(), savedBoard.end());
        Board board = Board();
        // See if we could set the board
        Codes::ReturnCode status = board.SetBoard(savedBoard);
        if (status != Codes::SUCCESS) {
            // Print the error, and notify client that we could not load the game
            cout << Codes::GetMessage(status) << endl;
            return Codes::LOAD_ERROR;
        }

        // Set the game state to the loaded game
        round.SetGameState(board, m_players);
        gameState = "resumed";
    }

    cout << endl << "Pente tournament has " << gameState << "!" << endl;

    // Our round loop - will keep playing until the player does not want to play again
    Codes::ReturnCode existStatus;
    for ( ; ; ) {
        // Start the round
        existStatus = round.Play();
        // See if controller wants to save - if so exit
        if (existStatus == Codes::SERIALIZE) {
            if (!SerializeGame(round)) {
                return Codes::SAVE_ERROR;
            }
            cout << "Game saved successfully, check the saves folder to see it!" << endl;
            // Don't want to output the end results if saving
            return Codes::SUCCESS;
        }

        // If player does not want to play again, get out of game loop
        if (!AskToPlayAgain()) { break; }

        // Reset the players captured scores and the board for a new round
        round.Reset();
    }

    // Print out who won the tournament
    OutputEndResults();

    return Codes::SUCCESS;
}

/* Private Utility Functions */

// General utility

/**********************************************************************
Function Name: AskToPlayAgain
Purpose: To ask the controller if they want to play another round
Parameters: None
Return Value: a boolean - true if the controller wants to play again, false otherwise
Assistance Received: None
**********************************************************************/
bool Tournament::AskToPlayAgain() const {
    string question = "Round has ended, would you like to play again";
    return BoolInput(question);
}

/**********************************************************************
Function Name: OutputEndResults
Purpose: To output the end results of the tournament
Parameters: None
Return Value: None
Assistance Received: None
**********************************************************************/
void Tournament::OutputEndResults() const {
    // Print out the final results
    cout << endl << "Pente tournament has ended! Here are the final results:" << endl
         << "Scores: " << endl
         << "\t" << m_human.GetName() << ": " << m_human.GetTournamentScore() << " points" << endl
         << "\t" << m_computer.GetName() << ": " << m_computer.GetTournamentScore() << " points" << endl;

    // Print out who won the tournament
    // Accessing the players directly as we only have 2 players,
    // if there were more I'd use the vector of players and sort by score
    cout << endl;
    if (m_human.GetTournamentScore() > m_computer.GetTournamentScore()) {
        cout << m_human.GetName() << " has won the tournament!" << endl;
    }
    else if (m_human.GetTournamentScore() < m_computer.GetTournamentScore()) {
        cout << m_computer.GetName() << " has won the tournament!" << endl;
    }
    else {
        cout << "The tournament is a tie!" << endl;
    }
}


// Loading


/**********************************************************************
Function Name: LoadGame
Purpose: To load a game from the saves folder
Parameters:
    a_board, a 2D vector of chars passed by reference, representing
        the game board to load the game into
Return Value: a boolean - true if loaded a game, false otherwise
Algorithm:
    1) Check if the saves folder exists and is not empty, can't
        load a game if we have no games to load
    2) Print out all the files in the saves folder, so the controller
        can choose which one to load
    3) Ask the controller if they want to resume from a game,
        asking for a file name if they do
    4) Read the save file
Assistance Received: https://en.cppreference.com/w/cpp/header/filesystem
**********************************************************************/
bool Tournament::LoadGame(vector<vector<char>>& a_board) {
    // Guard clause
    // If folder exists and it is not empty
    if (!filesystem::exists(SAVE_PATH) || filesystem::is_empty(SAVE_PATH)) {
        return false;
    }

    // Print out all the files in the saves folder, so the controller can choose which one to load
    PrintAvailableFiles();

    // Controller did not want to resume from a game
    // So put players in the vector and let caller know we did not load a game
    if (!AskToResume()) {
        m_players.push_back(&m_human);
        m_players.push_back(&m_computer);
        return false;
    }

    // Ask for file name
    filesystem::path filePath = AskResumeFile();

    // File exits
    return ReadFile(filePath, a_board);
}

/**********************************************************************
Function Name: AskToResume
Purpose: To ask the controller if they want to resume from a game
Parameters: None
Return Value: a boolean - true if the controller wants to resume from a game, false otherwise
Assistance Received: None
**********************************************************************/
bool Tournament::AskToResume() const {
    string question = "Would you like to resume from one of these games";
    return BoolInput(question);
}

/**********************************************************************
Function Name: AskResumeFile
Purpose: To ask the controller which file they want to resume from
Parameters: None
Return Value: a filesystem::path, the file path of the file to resume from
Assistance Received: None
**********************************************************************/
filesystem::path Tournament::AskResumeFile() const {
    // Get the file name from the player
    cout << "Please enter the name of the file you would like to resume from:" << endl << "> ";
    string fileName;
    getline(cin, fileName);

    // Make it as a path - and update to a correct extension if controller did not enter it
    filesystem::path filePath = GetFileAsPath(fileName);
    // Validate user input is correct, if not ask again
    while (!filesystem::exists(filePath)) {
        cout << "File name does not exist! Please enter a different file name:" << endl << "> ";
        getline(cin, fileName);
        filePath =  GetFileAsPath(fileName);
    }

    return filePath;
}

/**********************************************************************
Function Name: PrintAvailableFiles
Purpose: To print out all the files in the saves folder
Parameters: None
Return Value: None
Assistance Received: https://en.cppreference.com/w/cpp/filesystem/directory_iterator
**********************************************************************/
void Tournament::PrintAvailableFiles() const {
    // Let controller know what save files there are
    cout << "Save files found:" << endl;
    for (const auto& entry : filesystem::directory_iterator(SAVE_PATH)) {
        if (entry.path().extension() == EXTENSION_TYPE) {
            cout << "\"" << entry.path().filename().string() << "\" ";
        }
    }
    cout << endl << endl;
}

/**********************************************************************
Function Name: ReadFile
Purpose: To read the save file and parse the data into the game
Parameters:
    a_filePath, a filesystem::path object, the file path of the file to read
    a_board, a 2D vector of chars passed by reference, representing
        the game board to load the game into
Return Value: a boolean - true if could load a game, false otherwise
Algorithm:
    1) Open the file if it can
    2) Read the file line by line
    3) Determine what section we are at and parse the data accordingly
Assistance Received: None
**********************************************************************/
bool Tournament::ReadFile(const filesystem::path& a_filePath, vector<vector<char>>& a_board) {
    // Return values if we could parse the board
    bool couldParse = false;

    ifstream inputFile(a_filePath);
    if (!inputFile.is_open()) {
        return couldParse;
    }

    // Line stores the current line we are parsing in file
    // Section stores the section we are at, i.e. Board, Human, Computer, Next Player
    string line, section;
    // Beginning of file is first section of board
    getline(inputFile, section);

    while(getline(inputFile, line) && !inputFile.eof()) {
        // New line indicates that next line is new a section to parse
        if(line.empty()) {
            // Gets the section and continues to next line
            getline(inputFile, section);
        }

        // Parse our board section
        if (section.find(BOARD_SECTION) != string::npos) {
            vector<char> row;
            couldParse = ParseBoard(line, row);
            // Add row to board
            a_board.push_back(row);
        }
        // Parse our player sections
        else if (section.find(HUMAN_SECTION) != string::npos) {
            couldParse = ParsePlayer(line, m_human);
        }
        else if (section.find(COMPUTER_SECTION) != string::npos) {
            couldParse = ParsePlayer(line, m_computer);
        }
        // Parse our next player section
        else if (section.find(NEXT_PLAYER_SECTION) != string::npos) {
            couldParse = ParseNextPlayer(section);
        }
        else {
            couldParse = false;
        }
    }
    inputFile.close();

    return couldParse;
}

/**********************************************************************
Function Name: ParseBoard
Purpose: To parse the board section of the save file
Parameters:
    a_line, a string object, the line to parse
    a_row, a vector of chars passed by reference, representing the row
        of the board to parse
Return Value: a boolean - true if could parse the board, false otherwise
Assistance Received: None
**********************************************************************/
bool Tournament::ParseBoard(const string& a_line, vector<char>& a_row) {
    // Go through the line and add the stones to the row
    for (char stone : a_line) {
        // If we have a space, the board is invalid
        if (isspace(stone)) {
            return false;
        }
        a_row.push_back(stone);
    }
    return true;
}

/**********************************************************************
Function Name: ParsePlayer
Purpose: To parse the player section of the save file
Parameters:
    a_line, a string object, the line to parse
    a_player, a Player object passed by reference, representing the player
        to parse
Return Value: a boolean - true if could parse the player, false otherwise
Algorithm:
    1) Search for "Captured pairs" and extract value
    2) Search for "Score" and extract the value
    3) Add the values to the player
Assistance Received: https://regex101.com
**********************************************************************/
bool Tournament::ParsePlayer(const string& a_line, Player& a_player) {
    int capturedPairs, tournamentScore;

    // Stores the regexes for the lines that could be parsed
    regex capturedPairsRegex(CAPTURED + R"(\s(\d+))");
    regex tournamentScoreRegex(SCORE + R"(\s(\d+))");
    smatch matches;

    // Code duplication here, but simple enough where it does not need
    // to be moved to a function. One could argue that it would be best practice,
    // but I'm not the one to do so.

    // Search for "Captured pairs" and extract value
    if (regex_search(a_line, matches, capturedPairsRegex)) {
        capturedPairs = stoi(matches[1].str());
        a_player.IncCapturedPairs(capturedPairs);
        return true;
    }
    // Search for "Score" and extract the value
    if (regex_search(a_line, matches, tournamentScoreRegex)) {
        tournamentScore = stoi(matches[1].str());
        a_player.IncTournamentScore(tournamentScore);
        return true;
    }

    return false;
}


/**********************************************************************
Function Name: ParseNextPlayer
Purpose: To parse the next player section of the save file
Parameters:
    a_line, a string pass by const reference, the line to parse
Return Value: a boolean - true if could parse the next player, false otherwise
Algorithm:
    1) Search for the player name and the color
    2) Add the players to the vector based on who is next
Assistance Received:  https://regex101.com
**********************************************************************/
bool Tournament::ParseNextPlayer(const string& a_line) {
    // Parsing values for the players - hard coded as current serialization files
    // only has room for 2 players
    const string HUMAN = "Human", COMPUTER = "Computer";
    const string WHITE = "White", BLACK = "Black";
    // Regex: \s(Human|Computer)\s-\s(White|Black)*
    const string REGEX_STRING = "\\s(" + HUMAN + "|" + COMPUTER + ")\\s-\\s(" + WHITE + "|" + BLACK + ")*";
    // Not an elegant way to make the regex, but hardcoded values are needed for now

    // Stores the regexes for the lines that could be parsed
    regex nextPlayerRegex(NEXT_PLAYER_SECTION + REGEX_STRING);
    // Matches found from the regex
    smatch matches;

    // No matches found, parsing failed
    if (!regex_search(a_line, matches, nextPlayerRegex)) {
        return false;
    }

    // Literals here because we only have 2 matches, 1 for name and 1 for color
    string nextPlayerName = matches[1].str();
    string nextPlayerColor = matches[2].str();

    string otherPlayerColor;
    otherPlayerColor = (nextPlayerColor == WHITE) ? BLACK : WHITE;

    // There is code duplication here, it could be moved to a function, but
    // since we only have 2 players, it becomes more complicated to do so.
    // This is the easiest way.

    // Human will be the first to go - computer second
    if (nextPlayerName == HUMAN) {
        m_players.push_back(&m_human);
        m_human.SetColor(Player::ColorToChar(nextPlayerColor));

        m_players.push_back(&m_computer);
        m_computer.SetColor(Player::ColorToChar(otherPlayerColor));
    }
    // Computer will be the first to go - human second
    if (nextPlayerName == COMPUTER) {
        m_players.push_back(&m_computer);
        m_computer.SetColor(Player::ColorToChar(nextPlayerColor));

        m_players.push_back(&m_human);
        m_human.SetColor(Player::ColorToChar(otherPlayerColor));
    }

    // We were able to parse the next player
    return true;
}


// Saving

/**********************************************************************
Function Name: SerializeGame
Purpose: To serialize the round to a file
Parameters:
    a_round, a const reference to a Round object, the round to save
Return Value: a boolean - true if could serialize the game, false otherwise
Algorithm:
    1) Check if saves folder exists, can't save a game if we have no place to save it,
        create if it does not exist
    2) Ask for file name
    3) Create and open file to save game state in
    4) Write our data to the file to save
Assistance Received: https://en.cppreference.com/w/cpp/filesystem
**********************************************************************/
bool Tournament::SerializeGame(const Round& a_round) {
    // Check if saves folder exists, can't save a game if we have no place to save it
    if (!filesystem::exists(SAVE_PATH)) {
        // Specifically using output here because user should be notified
        // if we need to create or folder or if we couldn't
        if (filesystem::create_directory(SAVE_PATH)) {
            cout << "Successfully created save folder!" << endl;
        }
        else {
            cerr << "Error creating save folder! Could not save game!" << endl;
            return false;
        }
    }

    // Ask for file name
    filesystem::path filePath = AskSaveFile();

    // Create and open file to save game state in - input validated in AskFileName();
    return SaveGame(filePath, a_round);
}

/**********************************************************************
Function Name: SaveGame
Purpose: To save the game state to a file
Parameters:
    a_filePath, a filesystem::path object, the file path of the file to save
    a_round, a const reference to a Round object, the round to save
Return Value: a boolean - true if could save the game, false otherwise
Assistance Received: None
**********************************************************************/
bool Tournament::SaveGame(const filesystem::path& a_filePath, const Round& a_round) const {
    // Write our data to the file to save
    ofstream outputFile(a_filePath);
    if (!outputFile.is_open()) {
        return false;
    }
    outputFile << FormatSave(a_round);
    outputFile.close();

    return true;
}

/**********************************************************************
Function Name: AskSaveFile
Purpose: To ask the controller what file name they want to save the game as
Parameters: None
Return Value: a filesystem::path, the file path of the file to save
Assistance Received: None
**********************************************************************/
filesystem::path Tournament::AskSaveFile() const {
    string fileName;
    cout << "Please enter a file name:" << endl << "> ";
    getline(cin, fileName);

    // Validate input, make sure we don't overwrite something
    filesystem::path filePath = GetFileAsPath(fileName);
    while(filesystem::exists(filePath)) {
        cout << "File name already exists! Please enter a different file name:" << endl << "> ";
        getline(cin, fileName);
        filePath = GetFileAsPath(fileName);
    }
    return filePath;
}

/**********************************************************************
Function Name: FormatSave
Purpose: To format the game state to save based on the save sections
Parameters:
    a_round, a const reference to a Round object, the round to save
Return Value: a string, the formatted game state to save
Assistance Received: None
**********************************************************************/
string Tournament::FormatSave(const Round& a_round) const {
    // Format our data
    return FormatBoard(a_round.GetRoundBoard().GetGameBoard()) + "\n" +
           PlayerFormat(HUMAN_SECTION, m_human) + "\n" +
           PlayerFormat(COMPUTER_SECTION, m_computer) + "\n" +
           NextPlayerFormat(a_round.GetNextPlayer());
}

/**********************************************************************
Function Name: FormatBoard
Purpose: To format the board to save
Parameters:
    a_board, a 2D vector of chars, the board to save
Return Value: a string, the formatted board to save
Assistance Received: None
**********************************************************************/
string Tournament::FormatBoard(vector<vector<char>> a_board) const {
    // a_board can be a reference to save time in copying, but if we used
    // the board for something else we don't want to change its state

    string boardData = BOARD_SECTION + "\n";
    // Reverse the order of the rows to save properly
    reverse(a_board.begin(), a_board.end());
    // Go through board and save all stones
    for (const vector<char>& row : a_board) {
        for (char col : row) {
            boardData += col;
        }
        boardData += "\n";
    }
    return boardData;
}

/**********************************************************************
Function Name: PlayerFormat
Purpose: To format the player to save
Parameters:
    a_sectionName, a const reference to a string, the section name of the player
    a_player, a const reference to a Player object, the player to save
Return Value: a string, the formatted player to save
Assistance Received: None
**********************************************************************/
string Tournament::PlayerFormat(const string& a_sectionName, const Player& a_player) const {
    // Save format: Human:\nCaptured pairs:0\nScore: 0
    string playerData = a_sectionName + "\n";
    playerData += CAPTURED + " " + to_string(a_player.GetCapturedPairs()) + "\n";
    playerData += SCORE + " " + to_string(a_player.GetTournamentScore()) + "\n";
    return playerData;
}

/**********************************************************************
Function Name: NextPlayerFormat
Purpose: To format the next player to save
Parameters:
    a_player, a const reference to a Player object, the next player to save
Return Value: a string, the formatted next player to save
Assistance Received: None
**********************************************************************/
string Tournament::NextPlayerFormat(const Player& a_player) const {
    // Save format: Next Player: Human - White
    string nextPlayerData = NEXT_PLAYER_SECTION + " " + a_player.GetName() + " - ";
    nextPlayerData += Player::CharToColor(a_player.GetColor());
    return nextPlayerData;
}

// Shared utility

/**********************************************************************
Function Name: GetFileAsPath
Purpose: To get the file path from a file name
Parameters:
    a_fileName, a const reference to a string, the file name to get the path of
Return Value: a filesystem::path, the file path of the file name with proper extension
Assistance Received: https://en.cppreference.com/w/cpp/filesystem/path/replace_extension
**********************************************************************/
filesystem::path Tournament::GetFileAsPath(const string& a_fileName) const {
    // Adds extension if it does not exist
    filesystem::path filePath = SAVE_PATH / a_fileName;
    if (filePath.extension() != EXTENSION_TYPE) {
        filePath += EXTENSION_TYPE;
    }

    return filePath;
}

/**********************************************************************
Function Name: BoolInput
Purpose: To get a boolean input from the controller, i.e. yes or no
Parameters:
    a_question, a const reference to a string, the question to ask the controller
Return Value: a boolean - true if the controller answered yes, false otherwise
Assistance Received: None
**********************************************************************/
bool Tournament::BoolInput(const string& a_question) const {
    // Include constants for yes and no - as well as y and n
    const string YES = "YES", NO = "NO";
    const string Y = "Y", N = "N";

    string input;
    // Loop until we get a valid input
    do {
        cout << a_question << " (yes/no)?" << endl << "> ";
        getline(cin, input);
        for (char& c : input) { c = (char) toupper(c); }
    } while (input != YES && input != Y && input != NO && input != N);

    // Can either be yes or y - so we check for both
    return (input == YES || input == Y);
}