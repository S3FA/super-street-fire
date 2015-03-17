# Purpose and Description #

The IOServer is responsible for bridging the GameModel with input and output devices. Separate threads are responsible for listening for network events from data sources (e.g. gloves, headsets, GUI) and passing them to the GameModel or other interested components, as well as listening for game events and forwarding them to the relevant output devices.

The IOServer is the entry point of the app, and is responsible for initializing and managing the GameModel, network communication, DeveloperGUI etc.

# Details #

## Classes ##

  * **IOServer** contains the `main()` method that kicks everything off. Reads config files, parses command line args, initializes the GameModel, network threads, DevGUI etc.
  * **NetworkEventListener** listens for network events, deserializes them and passes them to the appropriate handlers
  * **GameEventBroadcaster** listens for GameModel events, serializes them and then broadcasts to registered listeners.


# Interactions and Dependencies #

### IOServer depends on GameModel ###
See note at GameModel

### IOServer depends on FireEmitterModel ###
See note at FireEmitterModel

### IOServer depends on [GUIProtocol](GUIProtocol.md) ###
See note at [GUIProtocol](GUIProtocol.md)

### IOServer depends on GestureRecognizer ###
See note at GestureRecognizer!

### IOServer depends on DeveloperGUI ###
But only to kick it off and apply the glue. As a general rule these packages should only communicate via GUIProtocol.