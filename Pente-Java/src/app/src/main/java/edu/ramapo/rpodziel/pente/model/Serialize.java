package edu.ramapo.rpodziel.pente.model;

import android.content.Context;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Serialize {
    /* Class Constants */

    // Saves are stored in the app's local storage
    public static String SAVE_PATH = "saves/";

    // Parsing strings for saving/loading
    public static final String BOARD_SECTION = "Board:";
    public static final String HUMAN_SECTION = "Human:";
    public static final String COMPUTER_SECTION = "Computer:";
    public static final String CAPTURED = "Captured pairs:";
    public static final String SCORE = "Score:";
    public static final String NEXT_PLAYER_SECTION = "Next Player:";


    /* Public Utility Functions */

    /**
     * Reads the file names from the local storage only if they end with .txt
     * @param a_context a Context object, the context of the application
     * @return an ArrayList<String>, the list of file names. An empty list if
     *        the directory does not exist or there are no files. Otherwise,
     *        the list of file names.
     */
    /*
    Algorithm:
        1. Get the file names
        2. If the directory does not exist, return empty list
        3. Add all files that end with .txt
        4. Return the list of file names
    Assistance Received:
        https://stackoverflow.com/questions/12421814/how-can-i-read-a-text-file-in-android
     */
    public static ArrayList<String> ReadFileNames(Context a_context) {
        // Get the file names
        File saves = new File(a_context.getApplicationContext().getFilesDir(), SAVE_PATH);

        ArrayList<String> fileNames = new ArrayList<>();
        // If the directory does not exist, return empty list
        if (!saves.exists()) {
            return fileNames;
        }
        // Add all files that end with .txt
        for (File file : Objects.requireNonNull(saves.listFiles())) {
            Log.d("File", file.getName());
            if (file.getName().endsWith(".txt")) {
                fileNames.add(file.getName());
            }
        }

        return fileNames;
    }

    /**
     * To read the save file and parse the data into the game
     * @param a_context a Context object, the context of the application
     * @param a_round a Round object, the round to set the game state to
     * @param a_fileName a String, the name of the file to load
     * @return a Codes.ReturnCode, the status of the load. A SAVE_ERROR if the file
     *        could not be read. A LOAD_ERROR if the file could not be parsed.
     *        Or a SUCCESS if the file was read and parsed successfully.
     * @throws IOException if the file could not be read
     */
    /*
    Algorithm:
        1) Open the file if it can
        2) Read the file line by line
        3) Determine what section we are at and parse the data accordingly
    Assistance Received:
        https://stackoverflow.com/questions/14376807/how-to-read-write-string-from-a-file-in-android
     */
    public static Codes.ReturnCode ReadSave(Context a_context, Round a_round,  String a_fileName) throws IOException {
        // Create new objects here to avoid parameter passing and breaking encapsulation
        Computer computer = new Computer();
        Human human = new Human();
        Vector<Player> players = new Vector<Player>();
        Vector<Vector<Character>> gameBoard = new Vector<Vector<Character>>();

        // Get the file from the local storage
        // Need to convert to buffered reader to read line by line
        File path = a_context.getApplicationContext().getFilesDir();
        FileInputStream input = new FileInputStream(new File(path, SAVE_PATH + a_fileName));
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));

        // See if we could parse the file
        boolean couldParse = true;

        // Line stores the current line we are parsing in file
        // Section stores the section we are at, i.e. Board, Human, Computer, Next Player
        String line; String section;
        // Beginning of file is first section of board
        section = reader.readLine();

        while((line = reader.readLine()) != null && couldParse) {
            // New line indicates that next line is new a section to parse
            if(line.isEmpty()) {
                // Gets the section and continues to next line
                section = reader.readLine();
                // Special case for next player
                if (!section.contains(NEXT_PLAYER_SECTION)) {
                    continue;
                }
            }

            // Parse our board section
            if (section.contains(BOARD_SECTION)) {
                Vector<Character> row = new Vector<Character>();
                couldParse = ParseBoard(line, row);
                // Add row to board
                gameBoard.add(row);
            }
            // Parse our player sections
            else if (section.contains(HUMAN_SECTION)) {
                couldParse = ParsePlayer(line, human);
            }
            else if (section.contains(COMPUTER_SECTION)) {
                couldParse = ParsePlayer(line, computer);
            }
            // Parse our next player section
            else if (section.contains(NEXT_PLAYER_SECTION)) {
                couldParse = ParseNextPlayer(section, players, human, computer);
            }
            else {
                couldParse = false;
            }
        }
        reader.close();

        // If we could not parse the file, return error
        if (!couldParse) { return Codes.ReturnCode.LOAD_ERROR; }

        // Rows go from 19-1 bottom up, so we need to reverse.
        Collections.reverse(gameBoard);
        Board board = new Board();

        // See if we could set the board
        Codes.ReturnCode status = board.SetBoard(gameBoard);
        if (status != Codes.ReturnCode.SUCCESS) {
            return Codes.ReturnCode.LOAD_ERROR;
        }

        // Set the game state to the loaded game
        status = a_round.SetGameState(board, players, human, computer);
        if (status != Codes.ReturnCode.SUCCESS) {
            return Codes.ReturnCode.LOAD_ERROR;
        }

        return Codes.ReturnCode.SUCCESS;
    }

    /**
     * To write the save file and save the game state
     * @param a_context a Context object, the context of the application
     * @param a_round a Round object, the round to save
     * @param a_fileName a String, the name of the file to save
     * @return a Codes.ReturnCode, the status of the save. A SAVE_ERROR if the file
     *        could not be written. A FILE_EXISTS if the file already exists.
     *        Or a SUCCESS if the file was written successfully.
     */
    /*
    Algorithm:
        1) Get the file path
        2) Create the saves directory if it does not exist
        3) Check if the file already exists
        4) Write the save to the file
    Assistance Received:
        https://stackoverflow.com/questions/14376807/how-to-read-write-string-from-a-file-in-android
     */
    public static Codes.ReturnCode WriteSave(Context a_context, Round a_round, String a_fileName) {
        File path = a_context.getApplicationContext().getFilesDir();

        a_fileName = AddExtension(a_fileName);

        File saves = new File(path, SAVE_PATH);
        if (!saves.exists() && !saves.mkdirs()) {
            return Codes.ReturnCode.SAVE_ERROR;
        }

        // Check if the file already exists
        File file = new File(path, SAVE_PATH + a_fileName);

        // Don't want to overwrite a save
        if (file.exists()) { return Codes.ReturnCode.FILE_EXISTS; }

        try {
            FileOutputStream writer = new FileOutputStream(new File(path, SAVE_PATH + a_fileName));
            String content = FormatSave(a_round);
            writer.write(content.getBytes());
            writer.close();
        } catch (IOException e) {
            return Codes.ReturnCode.SAVE_ERROR;
        }

        return Codes.ReturnCode.SUCCESS;
    }

    /* Private Utility Functions */

    // For reading

    /**
     * To parse the board section of the save file
     * @param a_line a string object, the line to parse
     * @param a_row a vector of chars passed by reference, representing the row
     *         of the board to parse
     * @return a boolean - true if could parse the board, false otherwise
     */
    private static boolean ParseBoard(final String a_line, Vector<Character> a_row) {
        // Go through the line and add the stones to the row
        for (char stone : a_line.toCharArray()) {
            // If we have a space, the board is invalid
            if (Character.isWhitespace(stone)) {
                return false;
            }
            a_row.add(stone);
        }
        return true;
    }

    /**
     * To parse the player section of the save file
     * @param a_line a string object, the line to parse
     * @param a_player a Player object passed by reference, representing the player
     *         to parse
     * @return  a boolean - true if could parse the player, false otherwise
     */
    /*
    Algorithm:
        1) Search for "Captured pairs" and extract value
        2) Search for "Score" and extract the value
        3) Add the values to the player
    Assistance Received:
        https://regex101.com
     */
    private static boolean ParsePlayer(final String a_line, Player a_player) {
        int capturedPairs; int tournamentScore;

        // Stores the regexes for the lines that could be parsed
        Pattern capturedPairsRegex = Pattern.compile(CAPTURED + "\\s(\\d+)");
        Pattern tournamentScoreRegex = Pattern.compile(SCORE + "\\s(\\d+)");
        Matcher matches;

        // Code duplication here, but simple enough where it does not need
        // to be moved to a function. One could argue that it would be best practice,
        // but I'm not the one to do so.

        // Search for "Captured pairs" and extract value
        matches = capturedPairsRegex.matcher(a_line);
        if (matches.find()) {
            capturedPairs = Integer.parseInt(Objects.requireNonNull(matches.group(1)));
            a_player.IncCapturedPairs(capturedPairs);
            return true;
        }
        // Search for "Score" and extract the value
        matches = tournamentScoreRegex.matcher(a_line);
        if (matches.find()) {
            tournamentScore = Integer.parseInt(Objects.requireNonNull(matches.group(1)));
            a_player.IncTournamentScore(tournamentScore);
            return true;
        }

        return false;
    }

    /**
     * To parse the next player section of the save file
     * @param a_line a string, the line to parse
     * @param a_players a vector of Player objects passed by reference, representing the players
     * @param a_human a human player object, the human player
     * @param a_computer a computer player object, the computer player
     * @return a boolean - true if could parse the next player, false otherwise
     */
    /*
    Algorithm:
        1) Search for the player name and the color
        2) Add the players to the vector based on who is next
    Assistance Received:  https://regex101.com
     */
    private static boolean ParseNextPlayer(final String a_line, Vector<Player> a_players, Human a_human, Computer a_computer) {
        // Parsing values for the players - hard coded as current serialization files
        // only has room for 2 players
        final String HUMAN = "Human"; final String COMPUTER = "Computer";
        final String WHITE = "White"; final String BLACK = "Black";
        // Regex: \s(Human|Computer)\s-\s(White|Black)*
        final Pattern REGEX_STRING = Pattern.compile(
                NEXT_PLAYER_SECTION + "\\s(" + HUMAN + "|" + COMPUTER +
                        ")\\s-\\s(" + WHITE + "|" + BLACK + ")*"
        );
        // Not an elegant way to make the regex, but needed for now.

        // Matches found from the regex
        Matcher matches = REGEX_STRING.matcher(a_line);

        // No matches found, parsing failed. We need 2 matches, 1 for name and 1 for color.
        if (!matches.find() || matches.groupCount() != 2) {
            return false;
        }

        // Literals here because we only have 2 matches, 1 for name and 1 for color
        String nextPlayerName = matches.group(1);
        String nextPlayerColor = matches.group(2);

        String otherPlayerColor;
        otherPlayerColor = (Objects.equals(nextPlayerColor, WHITE)) ? BLACK : WHITE;
        // There is code duplication here, it could be moved to a function, but
        // since we only have 2 players, it becomes more complicated to do so.
        // This is the easiest way.

        // Human will be the first to go - computer second
        if (nextPlayerName.equals(HUMAN)) {
            a_players.add(a_human);
            a_human.SetColor(Player.ColorToChar(nextPlayerColor));

            a_players.add(a_computer);
            a_computer.SetColor(Player.ColorToChar(otherPlayerColor));
        }
        // Computer will be the first to go - human second
        if (nextPlayerName.equals(COMPUTER)) {
            a_players.add(a_computer);
            a_computer.SetColor(Player.ColorToChar(nextPlayerColor));

            a_players.add(a_human);
            a_human.SetColor(Player.ColorToChar(otherPlayerColor));
        }

        // We were able to parse the next player
        return true;
    }

    // For saving

    /**
     * To save the game state to a file
     * @param a_round a Round object, the round to save
     * @return a String, the formatted save
     */
    private static String FormatSave(final Round a_round) {
        // Format our data
        return FormatBoard(a_round.GetRoundBoard().GetGameBoard()) + "\n" +
                PlayerFormat(HUMAN_SECTION, a_round.GetHuman()) + "\n" +
                PlayerFormat(COMPUTER_SECTION, a_round.GetComputer()) + "\n" +
                // We get current player as its event based
                // We save on the player who is going next
                NextPlayerFormat(a_round.GetCurrentPlayer());
    }

    /**
     * To format the board section of the save file
     * @param a_board a vector of vector of chars, the board to format
     * @return a String, the formatted board
     */
    private static String FormatBoard(Vector<Vector<Character>> a_board)  {
        // Need to reverse it to save it properly
        Collections.reverse(a_board);

        StringBuilder boardData = new StringBuilder(BOARD_SECTION + "\n");
        // Go through board and save all stones
        for (final Vector<Character> row : a_board) {
            for (char col : row) {
                boardData.append(col);
            }
            boardData.append("\n");
        }
        return boardData.toString();
    }

    /**
     * To format the player section of the save file
     * @param a_sectionName a String, the name of the section
     * @param a_player a Player object, the player to format
     * @return a String, the formatted player
     */
    private static String PlayerFormat(final String a_sectionName, final Player a_player) {
        // Save format: Human:\nCaptured pairs:0\nScore: 0
        String playerData = a_sectionName + "\n";
        playerData += CAPTURED + " " + a_player.GetCapturedPairs() + "\n";
        playerData += SCORE + " " + a_player.GetTournamentScore() + "\n";
        return playerData;
    }

    /**
     * To format the next player section of the save file
     * @param a_player a Player object, the player to format
     * @return a String, the formatted next player
     */
    private static String NextPlayerFormat(final Player a_player) {
        // Save format: Next Player: Human - White
        String nextPlayerData = NEXT_PLAYER_SECTION + " " + a_player.GetName() + " - ";
        nextPlayerData += Player.CharToColor(a_player.GetColor());
        return nextPlayerData;
    }

    /**
     * To add the .txt extension to the file name if it does not have it
     * @param a_fileName a String, the file name to add the extension to
     * @return a String, the file name with the extension if it did not have it
     */
    private static String AddExtension(final String a_fileName) {
        if (!a_fileName.endsWith(".txt")) { return a_fileName + ".txt"; }
        return a_fileName;
    }

}
