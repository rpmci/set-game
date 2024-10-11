# Play
You can play this game at [rpmci.github.io/set-game](https://rpmci.github.io/set-game/html/build/dist/index.html)

## What is this?
This is a version of the [card game SET](https://en.wikipedia.org/wiki/Set_(card_game)) created using Java and libGDX. This project was created by referencing the tutorials from [libgdx.com](https://libgdx.com/wiki/start/a-simple-game). 

The core of this project's code can be found under '[core/src/com/setgame/game](core/src/com/setgame/game)'. 

## What are the rules
View the rules [here](https://en.wikipedia.org/wiki/Set_(card_game))

The goal is to select 3 cards that form a set.

A set consists of three cards satisfying all of these conditions:
- They all have the same number or have three different numbers.
- They all have the same shape or have three different shapes.
- They all have the same shading or have three different shadings.
- They all have the same color or have three different colors.

If there is not a set available, add 3 cards. The game ends when there are no sets available and no cards remaining in the deck.


## Buttons
**Restart**: Reset the game

**Hint**: State if a set exists

**Add 3**: If there is not a set, add 3 additional cards

**Reveal 1**: As a hint, this highlights one card that is part of a set
