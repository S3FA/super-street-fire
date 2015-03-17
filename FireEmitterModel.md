# Purpose and Description #

The FireEmitterModel package contains the software representation of the hardware flame effects ("fire emitters") that make up the player feedback mechanism in the Super Street Fire arena. The representation for each of the fire emitters will hold information concerning its

  * current colour (or mix of colours), which represent the player(s) that the flame belongs to
  * percentage of flame intensity currently active on the emitter
  * Identifier/position in the arena, this will also identify whether its a rail emitter (i.e., its one of the emitters on the interior of the arena, between the two players) or a ring emitter (i.e., its in the arena's outer ring of fire emitters)

When values change for a particular emitter or set of emitters, events will be raised by the FireEmitterModel to indicate the change to any modules/packages that are listening. For example, the IOServer package will be listening so that it can react to changes by sending out the appropriate data packets to the relevant flame effects hardware, thereby changing the physical arena's state and providing feedback to the players, ring master and audience.

# Interface #

### Events ###

An event will be raised whenever a fire emitter changes its state, this could be a change in its colour or intensity. The event will look similar to the following:
```
void OnFireEmitterChanged(FireEmitter emitter)
```

### Commands ###

  * Set the flame output for a given emitter, the amount is a percentage or number in [0,1] with 0% as no flame and 100% as the full flame
  * Add a colour output for a particular flame emitter that ignores duplicate additions of the same colour for the same emitter
  * Remove a colour output for a particular flame emitter, ignores the call if the emitter already has the given colour removed from it

Example interface functions:
```
void SetEmitterFlameIntensity(FireEmitterID id, float amount)
void AddEmitterColour(FireEmitterID id, ColourType colourType)
void RemoveEmitterColour(FireEmitterID id, ColourType colourType)
```

# Interactions and Dependencies #

### GameModel depends on FireEmitterModel ###

The GameModel will be performing the game simulation/logic which will drive the changes in the FireEmitterModel. Therefore, the GameModel will be using the command interface of the FireEmitterModel to change the state of the emitters.

### IOServer depends on FireEmitterModel ###

The IOServer will be listening to the FireEmitterModel's event interface and reacting to the events it raises by sending out its own data packets to the flame effect hardware as well as send updates to the AndroidGUI via the GUIProtocol module.

### DeveloperGUI depends on FireEmitterModel ###

The DeveloperGUI will be listening to the FireEmitterModel's event interface so that it can update itself to illustrate the current state of the fire emitters in its graphical user interface.

