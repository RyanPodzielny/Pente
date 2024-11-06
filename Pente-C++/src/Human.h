//
//  Human player for game Pente - inherits from Player
//

#pragma once
#include "Player.h"

class Human : public Player {
    public:
        /** Constants **/
        inline static const string DEFAULT_NAME = "Human";


        /** Constructors **/
        Human() :
            Player(DEFAULT_NAME)
            { };

        /** Public Utility Functions **/
        void MakeMove(Board &a_board, const Player& a_nextPlayer) override;
        void GetHelp(Board &a_board, const Player& a_nextPlayer);
        bool CallToss() const;
};
