//
//  Base class for Pente players (Human and Computer inherit from it)
//

#pragma once
#include "stdafx.h"
#include "Codes.h"
#include "Board.h"


class Player {
    public:
        /** Constants **/
        inline static const string DEFAULT_NAME = "Player";
        static const char DEFAULT_COLOR = '?';
        inline static const string NO_COLOR = "Invalid color";
        static const int DEFAULT_SCORE = 0;

        inline static const string WHITE = "White", BLACK = "Black";
        static const char WHITE_CHAR = 'W', BLACK_CHAR = 'B';


        /** Constructors **/
        explicit Player(string a_name = DEFAULT_NAME) :
            m_name(std::move(a_name)),
            m_color(DEFAULT_COLOR),
            m_tournamentScore(DEFAULT_SCORE),
            m_capturedPairs(DEFAULT_SCORE),
            m_bestMove(ComputerMove())
            { };

        /** Accessors **/
        string GetNameAndColor() const;
        char GetColor() const;
        string GetName() const;

        int GetCapturedPairs() const;
        int GetTournamentScore() const;

        /** Mutators **/
        // Pure virtual function - must be implemented by derived classes
        virtual void MakeMove(Board& a_board, const Player& a_nextPlayer) {};

        Codes::ReturnCode SetName(const string& a_name);
        Codes::ReturnCode SetColor(char a_color);

        Codes::ReturnCode ResetCapturedPairs();
        Codes::ReturnCode IncCapturedPairs(int a_pairs);
        Codes::ReturnCode IncTournamentScore(int a_score);

        /** Public Utility Functions **/
        static string CharToColor(char a_color);
        static char ColorToChar(const string& a_color);

    protected:
        /** Constants **/
        static const int DEFAULT_EVAL = INT_MIN;

        // Used to get rational for the move
        enum MoveReason {
            UNKNOWN,
            WIN,
            CAPTURE,
            BUILD,
            BOARD_RESTRICTION
        };

        // Used for the computer strategy, stores the move and the reason for the move
        // and how well the move did in the evaluation
        struct ComputerMove {
            string position;
            int evalScore;

            char color;
            Player::MoveReason reason;

            ComputerMove() :
                position(),
                evalScore(DEFAULT_EVAL),
                reason(UNKNOWN),
                color(DEFAULT_COLOR)
                { };
        };

        /** Variables **/
        string m_name;
        char m_color;

        // Overall scores of the player
        int m_tournamentScore;
        int m_capturedPairs;

        // Used for the computer strategy - stores the best move to make
        struct ComputerMove m_bestMove;

        /** Protected Utility Functions **/
        void BestMove(const Board& a_board, const Player& a_nextPlayer);
        string GetReasonMessage() const;

        // Though not necessary to be in protected, if a base class wanted to change
        // how a computer evaluates a move or determines the best we can allow them to
        // do so by making them protected
        struct ComputerMove EvaluateMove(const Board& a_board, const Player& a_player) const;
        void DetermineBest(const Board& a_board, const ComputerMove& ourBest,
                           const ComputerMove& theirBest, vector<ComputerMove>& topMoves);
};
