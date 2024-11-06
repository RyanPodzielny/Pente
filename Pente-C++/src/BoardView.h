//
//  Prints out the board in a readable format
//

#pragma once
#include "stdafx.h"
#include "Board.h"

class BoardView {
    public:
        // Give the board a different look, specifically to make it more readable
        static const char REPLACE_NULL = '-';

        BoardView() = default;

        static void PrintBoard(const Board& a_board, char a_replaceNull = REPLACE_NULL);
};