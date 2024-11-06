/***********************************************************
* Name: Ryan Podzielny                                     *
* Project: Pente C++                                       *
* Class: CMPS 366 - OPL                                    *
* Date: 10/15/2023                                         *
***********************************************************/

#include "Tournament.h"
#include "Codes.h"

/***********************************************************
Function Name: main
Purpose: Entry point/driver for the program
Parameters: None
Return Value: 0, if the program exits correctly, 1 otherwise
Assistance Received: None
***********************************************************/
int main() {
    // Seed the random number generator
    // Not "the most random" but good enough for what we need it for
    srand(time(NULL));

    // Start the tournament
    Tournament tournament = Tournament();
    Codes::ReturnCode exitStatus = tournament.Start();

    // Print out the exit status, if there is one
    if (exitStatus != Codes::SUCCESS) {
        cout << Codes::GetMessage(exitStatus) << endl;
        return 1;
    }

    return 0;
}

