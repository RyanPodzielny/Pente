//
//  Board class: handles game-board logic for Pente
//

#pragma once
#include "stdafx.h"
#include "Codes.h"

class Board {
    public:
        /** Constants **/
        static const int BOARD_SIZE = 19;
        inline static const string CENTER_POSITION = "J10";
        static constexpr const char NULL_PIECE = 'O';

        // Offsets for getting proper indices
        static const char COLUMN_OFFSET = 'A';
        static const int ROW_OFFSET = 1;

        // Should be used in lock step fashion with deltas
        static const int NUM_DIRECTIONS = 8;
        // Represents the direction of where to go on the board:
        // 0, 1: up, 1, 1: up right, 1, 0: right, 1, -1: down right
        // 0, -1: down, -1, -1: down left, -1, 0: left, -1, 1: up left
        static constexpr const int COLUMN_DELTA[NUM_DIRECTIONS] =
            {0, 1, 1, 1, 0, -1, -1, -1};
        static constexpr const int ROW_DELTA[NUM_DIRECTIONS] =
            {1, 1, 0, -1, -1, -1, 0, 1};
        // Opted against using <pair> as it gets very cluttered
        // And could at times be hard to read

        static const int DEFAULT_SCORES = 0;

        inline static const string DEFAULT_LAST_POSITION;

        static const int WIN_SCORE = 5;
        static const int CAPTURE_NUM = 2;

        /** Constructors **/
        Board() : m_gameBoard(InitGameBoard()),
                  m_prevMoves(),
                  m_currMove(Move())
                  {};

        /** Accessors **/
        vector<vector<char>> GetGameBoard() const;
        int GetInnerBounds() const;
        int GetOuterBounds() const;

        bool IsGameOver() const;
        bool IsBoardFull() const;
        bool IsWinner() const;
        int GetIntersectLeft() const;

        int GetWinInARow() const;
        int GetCapturedPairs() const;
        string GetLastPosition() const;

        /** Mutators **/
        Codes::ReturnCode PlaceStone(char a_color, const string& a_position);
        Codes::ReturnCode UndoMove();
        Codes::ReturnCode SetBoard(const vector<vector<char>>& a_gameBoard);
        Codes::ReturnCode SetBounds(int a_innerBounds, int a_outerBounds);

        /** Public Utility Functions **/
        int GetNumNInARow(int a_n, int a_row, int a_column) const;
        int GetUninterStones(int a_n, char a_color) const;
        int GetPotentialCaptures(char a_color, int a_row, int a_column) const;

        static bool ParsePosition(const string& a_position, int& a_row, int& a_column);
        static string IndicesToString(int a_row, int a_column);
        static bool IsValidIndex(int a_row, int a_column);
        static int AwayFromCenter(int a_row, int a_column);

    private:
        // Move struct to store all information about a move
        // Really used to store information about the last move, and be able to undo it
        // Very helpful for the computer strategy
        struct Move {
            // Position placed by last move
            string position;

            // Piece restriction, used to restrict where a player can place a piece
            int innerBounds;
            int outerBounds;

            // Number of pairs captured in a single turn - updates every move
            int capturedPairs;
            int winInARow;

            int intersectLeft;
            vector<string> prevSeqs;

            Move(): position(DEFAULT_LAST_POSITION),
                    innerBounds(BOARD_SIZE - BOARD_SIZE),
                    outerBounds(BOARD_SIZE),
                    capturedPairs(DEFAULT_SCORES),
                    winInARow(DEFAULT_SCORES),
                    intersectLeft(BOARD_SIZE * BOARD_SIZE),
                    prevSeqs(vector<string>(NUM_DIRECTIONS))
                    {}
        };

        /** Variables **/
        // 2D vector representing square game board depending on m_boardSize,
        // each element is the color symbol of a player, e.g. "W" for white
        vector<vector<char>> m_gameBoard;

        // The stack of all moves made by players,
        stack<struct Move> m_prevMoves;

        // Current move made by player
        struct Move m_currMove;

        /** Private Utility Functions **/
        int CapturePairs(char a_color, int a_row, int a_column);
        vector<int> CardinalCount(int a_n, int a_row, int a_column) const;

        static int CountSameStones(const string& a_seq);
        vector<string> ColorSeq(int a_n, int a_row, int a_column) const;
        bool UpdateSeqs(const vector<string>& a_seq, int a_row, int a_column);
        static bool OffsetIndices(int& a_row, int& a_column, int a_direction, int a_step);

        static vector<vector<char>> InitGameBoard();
};