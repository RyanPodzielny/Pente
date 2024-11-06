# README

Recreating the board game [Pente](https://en.wikipedia.org/wiki/Pente) in both a command-line and Android applet appoarch! The command-line is strictly C++ using OOP while the Java/Android is event-driven and can be used on any android device.

### Game Features

Below is the addtional features added, ranging from an AI player to a save and quit feature.

- Two players, one is human one is AI
  - AI plays the best available move for that ply
  - AI will give a reason to why it moved the way it did
  - Human can recieve help from AI when requested, also including in plain text the reason to do so
- A tournament system to record scores from games played within rounds
- Save and quit
  - Records board, who's turn it is, round and tournament scores
  - Can save multiple games at once
- A coinflip to see who goes first
- A clear win message if the human quits and ends the tournament

### How to Start

For the commad-line it's pretty simple, just compile and rune the exectable. To note: it is written in C++.

For the Android, and to play on an stellar UI, you'll need an Android devide or emulator in order to get it started. I suggest running from Android Studio.
