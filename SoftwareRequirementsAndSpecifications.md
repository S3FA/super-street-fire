# Requirements #

  * There are two sequential sets ("rails") of flame effects hardware plus an outer ring:
    * Each rail has eight flame effects (for a total of 16 flame effects)
    * The outer ring is another 16 flames
    * The number of flame effects on each set will always be consistent but that number is flexible and should therefore be adjustable.
    * Each flame effect also has two associated colour sprays (each corresponding to a player)
    * Flames and colour sprays need to be turned on/off (0-100 intensity) by software
    * Intensity will be mapped to appropriate values for PWM or whatever
    * All flame and colour control will be done wired (RS485)
    * Design should account for arbitrary number of flames for inner and outer
    * RS485-based serial communication. Hardware may also be queried for data

  * Rules of the game
    * There are two players
    * There is a ringmaster who can influence / control flames at certain points in the game (e.g. pre/post game to rile up the crowd)
    * Each match consists of a maximum of 3 rounds (not including a possible tie-breaker/sudden-death round), number of rounds should be adjustable
    * Need an easy way to choose doing 1 round or best of 3
    * Each round is 60 seconds long (round time limit should be adjustable)
    * Blocking cancels an incoming attack, if chip damage is turned on then the attack still does a minor (small percentage of the attack?) amount of damage to the blocking player; if chip damage is turned off then the attack is cancelled without doing any damage to the blocking player
    * Players can deliver simultaneous attacks to each other. Ties may be possible. Could break tie based on first attack landed, flip a coin, ... (this is liable to change)
    * INSERT OTHER RULES TO AVOID PLAYERS SPAMMING ATTACKS/BLOCKS


  * Gesture Recognition
    * Gestures must be identified with at least 80% accuracy (false negatives are preferred over false positives for the remaining 20%)
    * Gesture recognition should be more robust for core moves (hook / jab / uppercut)
    * Gesture recognition must suit all real-time requirements
    * Gestures must be made in the direction of the opposing player in order for them to register ? (Ignore for now)
    * Must be able to distinguish between ringmaster and player gestures
    * MORE STUFF HERE?

  * Game states i.e., what is the flow of the game...
    * Idle State (no game play, waiting for a game to start)
    * Ringmaster State (Where the ringmaster is free to perform amazing feats of awesomeness and trigger round/match beginnings)
    * Round Beginning State (Initiated by ringmaster. Start up the timers, special flame effects?, 3-2-1 FIGHT etc.)
    * Round In-Play State (Players are attacking/blocking, trying to defeat the other)
    * Round Ended State (A player won the round, special flame effects? flashing timer? etc.) Effectively a paused state until ringmaster starts a new Round Beginning State
    * Settle Tie State (The players have tied across 3 rounds, this state settles a game, where players play similar to the In-Play state, but the round doesn't end until one of the players is officially KOed - this round cannot end until a player delivers a non-reciprocated-KO-attack that KOs the other player)
    * Match-Over State (A player has officially won the match, victory flames?)
    * Paused Game State (The game is paused... what does this mean for flame effects that were on before the pause? - all flames go out)

  * Able to end a round OR end a match via GUI

  * Chip damage can be turned on or off

  * There are two general types of player actions: attacks and blocks
  * There are also ringmaster actions (turn on all flames, patterns, ...)
  * Combos are possible (modify actions caused by gestures based on previous actions in a particular timeframe, or the current game state)
  * When a round is being played, flames represent either a block action or an attack action
  * Types of Attacks:
    * Jab (one-handed punch motion) damage?
    * Hook (one-handed sweeping punch) damage?
    * Hadouken (two-handed special attack) damage?

  * Types of Blocks:
    * Two-handed block


  * Each player is wearing two gloves (one on each hand), which capture inertial measurements (velocity, gravitational forces, orientation)
  * Each player is wearing a headset that records brain activity via Electroencephalography - numbers between 1 and 100 for "attention" and "meditation" values, 0 for no signal
  * The gloves and headset for each player will wirelessly transmit data
  * Results of attacks have lots of factors that should be configurable:
    * Time between attacks
    * Damage multiplier
    * Speed multiplier
    * "combo mode" (previous attacks/moves may factor in to current attack, whether other player is currently being hit, ...).

  * There may be other 'peripherals' (score boards/life bars, lights, timer, compressed air hits players when hit, ...)
  * The game must run in real-time (i.e., updates should occur at or above 60 Hz)

  * A (polished, user-friendly) Graphical User Interface (GUI) is required:
    * The interface must be able to operate on an Android tablet/device (mobile?)  and will communicate wirelessly via WiFi
    * The GUI should display information pertaining to the current state of the simulation and game state (this requires elucidation)
    * The GUI should refresh information as soon as possible, in real-time (i.e., updates occur at or above 60 Hz)
    * The GUI should be easy to use and understandable to a non-developer
    * Only one network GUI can be actively supported at any given time (?)

  * All communication must be encrypted

# Specifications #

## High-level Software Modules ##

Think Model-View-Controller paradigm:
  * The model is the Game Module
  * The views are...
    * The Server Module for sending/receiving information which is relayed from/to the Game Module and where events are further broadcasted via the various protocols/packages used for communicating with remote peripherals and GUIs
    * Any developer-specific GUIs on the server machine (used for debugging/introspective purposes)
  * The controller is the combination of glove and headset hardware and embedded software along with the communications to send the data from that hardware to the server and then parse it and understand it via the Gesture Recognition Module

### Server (Network Receiver/Sender) Module ###
  * Receives incoming packages from peripherals and interprets the packages into application level objects that can be used by other modules (e.g., glove data is parsed/'unbinarized' and turned into a 'GloveData' object so that it can be passed to the gesture recognition module)
  * Sends packages to peripherals from the ongoing simulation when certain game events occur (e.g., Player 1's life changes from 100% to 80% which raises a life changed event from the game module, the sever module is a listener for such events, it takes the event and builds the appropriate outgoing packages for sending to peripherals such as the score board client).
  * Sends GUI protocol packages over the network via TCP/IP (port TBA)  when events occur in the simulation
  * Receives GUI protocol packages from networked GUI, which can change the state of the simulation (we need to enumerate all of the actions that can take place in the protocol...)
  * This module (or the network communication portion of it, at the very least), should live on a separate thread and all communication should take place asynchronously

### Game (Model and Logic) Module ###
  * Tracks information and holds data structures for representing players (e.g., life percentages, names, character type)
  * Tracks state of the game, each state tracks its corresponding game information (e.g., "RoundInPlayState" would track which round it is, how much time is left in that round, how many rounds have been won/lost by what players)
  * Contains data structures for representing blocks, attacks, damage of attacks, routines/functions for executing attacks and dealing with player damage
  * Contains code to signify when certain game events occur and execute the appropriate event callbacks on all listeners to the game model
  * Coordinates blocks and attacks with the flame effect simulation module to ensure the correct flame effects are turning on/off based on the gestures being registered while the game is being played
  * Provides an event interface with the following callbacks/handlers:
    * void OnPlayerHealthChanged(int playerNum, int prevLifePercentage, int newLifePercentage) - When a player's health changes during the game
    * void OnRoundTimeChanged(int newCountdownTimeInSecs) - When the round countdown timer changes during the game
    * void OnGameStateChanged(GameStateType stateType) - When the game state changes
    * void OnPlayerMoveExecuted(GameMoveType moveType, move quality specifiers ?) - When a player executes a particular move in the game
  * Provides a command interface to other modules with the following commands:
    * void InitiateNextMatchRound() - Used to start the first round of a new match or begin the next round of the current match
    * void ExecutePlayerMove(int playerNum, GameMoveType moveType, ... move quality specifiers ?) - Executes a move for a player in the game
    * bool TogglePauseGame() - Pauses the game that is currently in-play
    * void KillGame() - Kills the game entirely (i.e., clears it to an Idle state)


### Fire Emitter Module ###
  * Holds the state machine for each of the flame effects, simulated in software so that events arise properly that will turn on/off the hardware flame effects corresponding to attacks and blocks that are raised by the Game Module
  * The state machine details whether the effect is on (percentage) and, when the effect is turned on, whether it is 'owned' by player 1 and/or player 2 (for knowing what colour spray(s) should be active)
  * Models the hardware state
  * Provides an event interface with the following callbacks/handlers:
    * void OnFireEmitterChanged(FireEmitter emitter) - Whenever any part of a fire emitter's state changes this event will be called
  * Provides a command interface to other modules with the following commands:
    * SetEmitterFlame(FireEmitterID id, float amount) - Sets the flame output for a given emitter, the amount is a number in [0,1] representing a percentage with 0 as no flame and 1 as full flame
    * AddEmitterColour(FireEmitterID id, ColourType colourType) - Adds the colour output for a particular flame emitter, ignores duplicate additions of the same colour for the same emitter
    * RemoveEmitterColour(FireEmitterID id, ColourType colourType) - Removes the colour output for a particular flame emitter, ignores the call if the emitter already has the given colour removed from it

### Gesture Recognition Core Module ###
  * Should be a _very_ simple interface:
    * Input is an ongoing stream of data from both gloves of a given player (i.e., 2x glove data and associated player number)
    * Output is a in-game move/action (e.g., an attack or a block type object) and an associated player number for the player who executed that move/action, the output should only occur _once_ at the end of a particular, discrete action and not repeatedly as the action takes place

### Gesture Recognition Training Data File Reader/Writer Module ###
  * Responsible for serializing and de-serializing the training data for both the recording and training modules
  * Responsible for serializing and de-serializing the weights/description of the AI network that is developed to recognize gestures and will therefore be used by the training module and the core module

### Gesture Recognition Recording Module ###
  * Provides a simple interface (text or GUI based?) for recording the various moves/actions in the game from active glove hardware (record button, stop button, display of data that was recorded?)
  * Records data from the finalized glove hardware and saves plain-text training files from _lots_ of different people performing each of the actions/moves in the game

## Gesture Recognition Training Module ##
  * Aggregates the data accumulated from the Recording Module via the Reader/Writer Module and turns it into a properly weighted network (Bayesian, Neural, Other)?
  * Serializes the 'compiled' network as a plain-text file using the Reader/Writer Module so that it may be read by the Gesture Recognition Core Module when the game software is executed

### GUI Protocol Module ###
  * Since the Server Module needs to send/receive GUI events via a centralized protocol and the Android GUI Module needs to do the same all of the objects used in the protocol for serialization and representation of data should be in this single centralized java package
  * Holds classes for the various GUI events and the routines for serialization of those classes
  * Holds enumerations of all actions and procedures in the protocol

### Android GUI Module ###
  * Receives and sends GUI action protocol from/to the Server Module over a network connection
  * What elements are in this GUI?? ... What information is displayed, what controls are available, look and feel, user flow, etc. needs to be elucidated

### Debugging/Developer GUI Module ###
  * This is up to the developers, it should make use of the event system provided by the game model and respond to it as well as be able to modify it
  * The GUI should sit on the same machine and be part of the same process as the core software (i.e., server, game model)
  * Logging?
  * All functionality in the Android GUI should also be available here