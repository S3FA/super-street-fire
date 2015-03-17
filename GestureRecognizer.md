# Purpose and Description #

The GestureRecognizer contains all of the code responsible for analyzing incoming data from a player's (or ring master's) gloves (i.e., gyroscopic, accelerometer and magnetometer data from each of the glove's inertial measurement units) and then recognizing and outputting gestures that are relevant to the Super Street Fire game. The GestureRecognizer package will likely contain a machine learning data structure that will have been trained to recognize player and ring master gestures. In the case of the players, recognizable gestures will include all types of attacks (e.g., jab, hook, hadouken) and the blocking gesture. For the ring master, the GestureRecognizer will need to recognize certain "spectacle" gestures (e.g., raising of hands, twirling of hand in the air).

In order to train the machine learning data structure in the GestureRecognizer package, several sub-packages will be required. These sub-packages will allow gesture data to be captured/recorded, written to file on disk, read from file on disk and then used to train the data structure / algorithm that is chosen (e.g., Neural Network, Bayesian Network).

Data from the gloves will come over wifi in UDP. Each value (gyroscope, accelerometer and magnetometer.) will be 10-bits packed into 2 bytes. Clarification needed, is gyroscope one per axis or one overall?

# Interface #

### Events ###

The GestureRecognizer will have one or more events that are raised when a gesture has been detected. These events will only be raised once per gesture/move.

For example:
```
void OnPlayerMoveExecuted(int playerNum, PlayerActionType actionType, ActionQualities qualities)
void OnRingMasterMoveExecuted(RingMasterActionType actionType, ActionQualities qualities)
```

### Commands ###

The command interface to the GestureRecognizer will consist of a single-purpose method or set of methods for inputting data obtained from player and ring master gloves. The interface will distinguish whether the data came from a player or a ring master, which player it came from (if it came from a player), and also the nature of the data i.e., which hand it came from on the player/ring master.

For example:
```
void AddPlayerGloveData(int playerNum, int leftOrRightHand, GloveData data)
void AddRingMasterGloveData(int leftOrRightHand, GloveData data)
```

# Interactions and Dependencies #

### IOServer depends on GestureRecognizer ###

The IOServer package will be receiving data from the glove peripherals for both players and the ring master. It will be the responsibility of the IOServer to make sense of the incoming data packets and convert them into higher-level, understandable data structures, which it will then forward to the GestureRecognizer via its input interface.

### GameModel depends on GestureRecognizer ###

The GameModel package will be listening for gesture events from the GestureRecognizer and then responding to them by changing its state.