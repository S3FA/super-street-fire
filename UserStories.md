# pre-ramble... #

For the most part these are very high level, and just flesh out the areas which will need an investment of time.

Please if you are working on an area that affects/consumes these use cases, update and change them to match the game

# Player Interactions #

## Player Object ("Stretch goal") ##

The players may be allowed to enter their names into the database for “high score keeping” and other interesting stats. <br />

Player ID (Unique fighter name)<br />
Player Name (Optional player real name)<br />
Current Fight Matches won<br />
Total Wins<br />
Total Losses<br />
Fire Colour<br />

## Attack ##

Attack function takes an Attack object, and processes commands to the various game systems

**Attack object:**<br />
Name/Type<br />
Damage (In % of total health)<br />
Speed (The travel time the flame wave takes to complete the circuit. Also determines block timer)<br />
Ring Flag (Left/Right/Both)<br />

**Attack Function Triggers the following sub routines**<br />
Fire Control<br />
Block Timer<br />
Move Recorder<br />

## Block ##

Block function will take a Block object (To allow introducing of multiple types of blocking at a later point. High/Low/Parry) And trigger the appropriate reactions.

**Block object**<br />
Name/Type<br />
Deploy delay (How long does it take to become active)<br />
Block Duration (How long it stays active)<br />
Time during attack which block is effective (To parry the parry has to be done with precise timings)<br />
“Chip” damage (In a % of the attack damage)<br />
Other effects (IE. Parry could double the recovery time of the attacker)<br />

**Block Function Triggers the following sub routines**<br />
Fire Control<br />
Move Recorder<br />
Block interpreter<br />

## Meditation Threshold Hit ##

When meditation threshold is hit, the system will automatically trigger an X second auto normal block.  Basically meaning they take 1/8 damage for the duration.  This will not cancel out the ability to parry.

## Attention Threshold Hit ##

**Possibility A**<br />
When the Attention Threshold is hit, any special moves the attacker performs will have the next (Or time frame of) special attack (Hadouken, dragon punch, etc…) to be unblockable.

**Possibility B**<br />
When the attention threshold is hit, and held for a set amount of time, a super move it performed

# System Actions #

## Game UI ##
The consensus is that the game will need a UI.  The UI will allow for; Starting a new game, replaying a saved game, checking the status of the safety cutoffs, checking the fuel level of the colour/fire systems, displaying what player has initiated what, monitoring the attention/med levels, calibration of the gloves, select the handedness of the fighters, monitoring the current round, turning the flame system on and off for a match.

## Round timer ##
The round timer will be a two phase timer, the first will be a short “ready” period to allow the fighters to assume the ready stance and fight.  The second will be a time that when it finishes all systems are shut down.  This will also trigger the “comparative victory” routine to figure out who wins by current health

## Match Counter ##
The system will need a provision to count the number of rounds that have taken place, and who has won each round. (Round wins may be stored in the player object.)

## Health Monitoring ##
The game will maintain the health of each player; these “bars” will be decremented by the attack and block functions.

## Move recoding ##
It was set out as a requirement to be able to record matches and play them back.  By storing the action and time stamp, the games can be played by creating a sub routine that accepts the database inputs and returns outputs to the game in the same manner the state machine does.  The game would know no difference between a real game and a playback game.

## Fire Control ##
The game will need a set of commands that will accept an attack object, and based off of the values computer the shape/speed of the fire wave/fire pillar.

## Safety Cutoffs ##
The system will need provisions for some sort of safety cutoffs (Like dead man switches) to be a full override of all flame based systems.  Games should be allowed to continue if the safety cutoffs are released, however all flame activity should cease immediately.

## Player Storage ##
Since SSFT will be heading to multi-day events.  It may be a neat feature ti be able to store players, and allow players to track their stats.