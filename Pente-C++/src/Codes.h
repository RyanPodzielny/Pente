//
//  Handles the return/error codes for the program, specifically for mutators
//

#pragma once
#include "stdafx.h"

class Codes {
    public:
        // Implemented as an enum to avoid namespace pollution in each class
        // This is not practical for a large scale project, but for this project
        // and its size - it's the easiest implementation to record error codes.
        enum ReturnCode {
            // General
            UNKNOWN,
            SUCCESS,

            // Board's codes
            COULD_NOT_PARSE,
            INVALID_MOVE,
            SPACE_OCCUPIED,
            INVALID_BOARD,
            FULL_BOARD,
            ALREADY_WINNER,
            INVALID_BOUNDS,
            NO_PREV_MOVES,

            // Player's codes
            INVALID_INC,

            // Round's codes
            SERIALIZE,
            ROUND_END,
            INVALID_PLAYER,
            NULL_PLAYER,
            SAME_COLOR,

            // Tournament's codes
            LOAD_ERROR,
            SAVE_ERROR
        };

        Codes() = default;

        static string GetMessage(ReturnCode a_errCode);
};

