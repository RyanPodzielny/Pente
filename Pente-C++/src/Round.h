#pragma once

#include <utility>

#include "stdafx.h"
#include "Player.h"
#include "Human.h"
#include "Computer.h"
#include "Board.h"
#include "BoardView.h"

class Round {
    public:
        /** Constants **/
        static const int NUM_PLAYERS = 2;

        static const int WIN_SCORE = 5;
        static const int STRAIGHT_STONES = 4;

        static const bool DEFAULT_SERIALIZED = false;
        static const int DEFAULT_START_INDEX = 0;

        static const int DEFAULT_WIN_SCORE = 0;

        // Colors in order of which goes first
        constexpr static const char COLOR_PRECEDENCE[NUM_PLAYERS] = {'W', 'B'};

        /** Constructors **/
        // Force the user to pass in the human and computer
        Round(Human* a_human, Computer* a_computer) :
            m_human(a_human),
            m_computer(a_computer),
            m_players({m_human, m_computer}),
            m_currPlayerIndex(DEFAULT_START_INDEX),
            m_winner(nullptr),
            m_board(Board()),
            m_isSerializedGame(DEFAULT_SERIALIZED),
            m_numWinInARow(DEFAULT_WIN_SCORE)
            { };

        /** Accessors **/
        Board GetRoundBoard() const;
        Player GetNextPlayer() const;

        /** Mutators **/
        Codes::ReturnCode Play();
        Codes::ReturnCode SetGameState(const Board& a_board, const vector<Player*>& a_players);
        Codes::ReturnCode Reset();

        /** Public Utility Functions **/
        void OutputEndPly() const;

    private:
        /** Variables **/
        // Need to store human and computer separately as
        // we need to do a coin toss setting human to a specific color
        // This is mainly a problem with being a 2 player game
        Human* m_human;
        Computer* m_computer;
        // Stores the players in order of which goes first
        vector<Player*> m_players;
        // Holds the current player index
        int m_currPlayerIndex;
        // Holds the winner of the game
        Player* m_winner;

        // Board information
        Board m_board;
        bool m_isSerializedGame;
        int m_numWinInARow;

        /** Private Utility Functions **/
        void SetPlyOrder();
        void SortScores();

        int DeterminePly() const;
        void SetBoardRestriction(int a_plyCount);
        bool FacilitatePly(Player *a_currPlayer);
        int NextPlayerIndex(int a_playerIndex) const ;

        bool CheckRoundEnd(Player* a_currPlayer);
        bool AskToSerialize() const;
        void TallyScores();
};
