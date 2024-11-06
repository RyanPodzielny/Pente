//
//  Represents a computer player in the game of Pente - inherits from Player
//

#pragma once
#include "Player.h"

class Computer : public Player {
    public:
        inline static const string DEFAULT_NAME = "Computer";

        Computer() :
            Player(DEFAULT_NAME) {};

        void MakeMove(Board &a_board, const Player& a_nextPlayer) override;
};

