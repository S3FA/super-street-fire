# Introduction #

These are just some initial notes I had on what we'll have to implement in terms of game state, data in and out, and on the gesture recognition.

# Server Tasks #

general program flow loop:
  * process incoming serial data
  * gesture recognition
  * update game state
  * send outgoing data based on changed game state

some libraries that may help:
  * pySerial for processing of serial data
    * see visualization program from 9DoFAHRS for example
  * python-xbee (on google code) - but this may be overkill if we're only using serial data
  * the 9DoF AHRS arduino code for the glove modules
  * Vpython for easy easy visualization


# Notes #

game:
  * player1, player2
  * ring
  * round number
  * timer
  * practice mode

ring:
  * N flame effects
    * on or off
    * controlled by serial protocol
  * M colors per flame effect
    * 2 colors for player1, player2 minimum?
    * on or off
    * controlled by serial protocol
    * can only be on if flame effect is on
    * automatically shuts off if flame effect turned off

safety lighting:
  * armed or disarmed

serial interface:
  * read incoming data
  * set device to communicate with (or broadcast?)
  * write outgoing data to device (or broadcast?)

player:
  * active or inactive
  * health

monitoring interface:
  * armed indicator
  * gloves/headsets linked indicator
  * start/stop match indicator
  * round indicator
  * health indicator
  * timer indicator
  * practice mode toggle
  * glove data
  * headset data
  * glove visualizer: 2 for each player
  * headset visualizer: for each player

gesture:
  * motion: what motion needs to be made (L/R)
  * orientation: how glove needs to be oriented to start gesture (L/R)
  * rotation: what rotation needs to be made (L/R)
  * effect: what pattern of flames
  * recoil time
  * headset requirement?


gestures -> moves -> ring
ring needs to keep track of multiple moves currently happening
  * queue/list for move effect?
  * what happens if move is finished early, e.g. blocking

gesture recognition:
  * one thread for each gesture monitor?
  * watches incoming data stream, communicates back if gesture found
  * there's probably a much better way to do this (state machine?)