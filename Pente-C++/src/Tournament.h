//
//  Handles tournament for Pente
//

#pragma once
#include "stdafx.h"
#include "Round.h"
#include "Player.h"
#include "Human.h"
#include "Computer.h"
#include "Board.h"

class Tournament {
    public:
        /** Constants **/
        const filesystem::path SAVE_PATH = filesystem::current_path() / "saves";
        const string EXTENSION_TYPE = ".txt";

        // Parsing strings for saving/loading
        const string BOARD_SECTION = "Board:";
        const string HUMAN_SECTION = "Human:";
        const string COMPUTER_SECTION = "Computer:";
        const string CAPTURED = "Captured pairs:";
        const string SCORE = "Score:";
        const string NEXT_PLAYER_SECTION = "Next Player:";

        // Could implement accessors, but for my implementation it's not needed

        /** Constructors **/
        Tournament() :
            m_human(Human()),
            m_computer(Computer()),
            m_players()
            { };

        /** Mutators **/
        Codes::ReturnCode Start();

    private:
        /** Variables **/
        Human m_human;
        Computer m_computer;
        // Stores the ply order based on serializing
        vector<Player*> m_players;

        /** Private Utility Functions **/
        // In order of where functions are "tied" together
        // In composite to derived for each group, but not overall

        // General utility
        bool AskToPlayAgain() const;
        void OutputEndResults() const;

        // Loading a game
        bool LoadGame(vector<vector<char>>& a_board);
        void PrintAvailableFiles() const;
        bool AskToResume() const;
        filesystem::path AskResumeFile() const;

        bool ReadFile(const filesystem::path& a_filePath, vector<vector<char>>& a_board);
        bool ParseBoard(const string& a_line, vector<char>& a_row);
        bool ParsePlayer(const string& a_line, Player& a_player);
        bool ParseNextPlayer(const string& a_line);

        // Saving a game
        bool SerializeGame(const Round& a_round);
        bool SaveGame(const filesystem::path& a_filePath, const Round& a_round) const;
        filesystem::path AskSaveFile() const;
        string FormatSave(const Round& a_round) const;
        string FormatBoard(vector<vector<char>> a_board) const;
        string PlayerFormat(const string& a_sectionName, const Player& a_player) const;
        string NextPlayerFormat(const Player& a_player) const;

        // Shared utility
        filesystem::path GetFileAsPath(const string& a_fileName) const;
        bool BoolInput(const string& a_question) const;

};
