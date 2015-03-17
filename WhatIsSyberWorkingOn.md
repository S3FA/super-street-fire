## Introduction ##

After a bit of fiddling with the IMU output, the three basic states are defined (jab, hook, block). We're looking to expand to hadouken, dragon punch, and a couple more. In discussion.

## Gestures ##

There are some test programs in the "GloveTests" project that work with a single device over direct serial, plus some "testdata".

The data is four streams of x-y-z or roll-pitch-yaw:
  * Headings (orientation relative to the calibration orientation)
  * Acceleration (useful for punches, and power of move)
  * Gyros (useful for twisting/turn based movement)
  * Magnetometer (not used)

For any real testing, (as of May 1st) the game is processing wireless input and throwing it into the gesture state machine.

### Sound effects added ###

Using pygame.mixer and some theme sound effects, there's now audio feedback when the GloveData sets the attack/block state.

Pretty useful for testing!

### What makes a move? ###

The elements of a move are building blocks of data-changes.

Generally using acceleration as a threshold value, and gyros as a delta value. Orientation (headings) are good for block or more complicated combos.

To make the simplest definition possible - detect the more complicated of similar moves first. i.e. hadouken (both hands must match), hook (L/or/R), then jab (L/or/R).

  * Jab: X acceleration exceeds 700-800, maybe include gyros increasing X, discard orientation because humans can't be relied on to have good punching technique.
  * Hook: X + Y acceleration exceeding ~700/~400, gyros increasing for both X,Y, again ignore orientation.
  * Block: hands up, vertical with body; low acceleration, orientation (Pitch) exceeds 70 degrees from flat.
  * Hadouken: X+Y acceleration, Y+Z gyros increasing, hands **must** be correct position (Roll + Pitch greater than 40 degrees from flat?).


### I/O ###

Now all the data goes through the Receiver module. Which means I should probably rename it "IOChannel" or something.

On each tick, the incoming data is parsed (parser module) and creates GloveData objects.

At the end of the tick process, all fire emitter states are all queried and sent to the xbee (fake address for now).
