# Purpose and Description #

The GameModel package contains all of the logic for executing and progressing the state of the game as dictated by its various gameplay rules and mechanics. It will contain representations for the players, player moves (attacks, block(s)), and game states (e.g., round start, round in-play, round end, pause). Internally, the GameModel is a state machine that reacts to its input based on its current state. For example, when the GameModel is told that a player has executed a particular gesture, the model will only react if it is in a "RoundInPlay" state. Within the context of the state it will then carry out the logic to incorporate the input into the current frame/tick of the game simulation and possibly change the game state based off of that input and its interaction with the exiting internal state of the GameModel.

Since the model has to be constantly updating with real-time correspondence to the outside world, it will require an ongoing heartbeat in order to keep it aware of the amount of time elapsing over the course of a game. This simulation 'tick' or delta time frame will allow the model to keep track of and react to various time-sensitive data (e.g., round-time, time between player moves).


# Interface #

### Events ###
  * A player's health changes during the game
  * The round countdown timer changes during the game
  * The game state changes

Example interface functions:
```
void OnPlayerHealthChanged(int playerNum, int prevLifePercentage, int newLifePercentage)
void OnRoundTimeChanged(int newCountdownTimeInSecs)
void OnGameStateChanged(GameStateType stateType)
```

### Commands ###
  * Tell the GameModel to tick its simulation by some delta time
  * Start the first round of a new match or begin the next round of the current match
  * Execute a move for a player in the game
  * Execute an action for the ringmaster
  * Pause any game that is currently in-play
  * Kill the game entirely (i.e., clears it to an Idle state)

Example interface functions:
```
void Tick(double dT)
void InitiateNextMatchRound()
void ExecutePlayerAction(int playerNum, PlayerActionType actionType, ActionQualities qualities)
void ExecuteRingMasterAction(RingMasterActionType actionType, ActionQualities qualities)
bool TogglePauseGame()
void KillGame()
```

# Interactions and Dependencies #

### IOServer depends on GameModel ###
The IOServer will both listen to and execute commands on the GameModel. When various events occur in the GameModel, the IOServer will be notified (via an event listener) of the changes and then ensure that the relevant clients/peripherals are notified of those changes. Contrariwise, when clients/peripherals of the IOServer have updates that are relevant to the GameModel, the IOServer will forward those updates to the GameModel via its interface.

### GameModel depends on GestureRecognizer ###
The GestureRecognizer package will be responsible for issuing events when it detects the various moves executed by the two players and ring master over the course of a match. The GameModel, upon receiving an event from the GestureRecognizer, will then update its current state based off of the gesture that was received.

### GameModel depends on FireEmitterModel ###
As an outgoing dependancy, the GameModel is responsible for driving the FireEmitterModel though its simulation of the various moves that get executed on it over the course of a game. For example, once an attack (e.g., a jab) is executed on the GameModel while the game is in play, the GameModel will execute that attack for some length of time by changing the states of the appropriate fire emitters within the FireEmitterModel, which will emulate the "attack flame(s)" traveling towards the opposing player.

### GameModel depends on Utils ###
Similar to all other packages, the GameModel will require logging and possibly some other basic, low-level objects and functions, which will be available to it via the Utils package.