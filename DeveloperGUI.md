# Purpose and Description #

The DeveloperGUI package is intended to provide the software developers with a dashboard for viewing the internal state of the Super Street Fire game. This dashboard will consist of a full Graphical User Interface (GUI), designed to provide any useful information for comprehension/debugging purposes. The DeveloperGUI package will be designed to provide all of the basic functionality as the AndroidGUI package i.e., it listens to all of the same events and issues all of the same commands. By doing this, the DeveloperGUI will act as an initial testing ground for the final GUI as well.

# Interface #

The DeveloperGUI will directly interact with the various packages that it depends on in order to forward commands to them. For displaying information, the DeveloperGUI will react to events that it listens to from those same packages.

# Interactions and Dependencies #

### DeveloperGUI depends on GameModel ###

The DeveloperGUI will listen to the GameModel and then change and update its display based off of those events. Upon receiving events from the user via the GUI, which have an effect on the GameModel, the DeveloperGUI will turn those events into commands that will be executed on the GameModel.

### DeveloperGUI depends on FireEmitterModel ###

The DeveloperGUI will listen to the FireEmitterModel and then change and update its display based off of those events. For example, it will show the state of each fire emitter in the game via its GUI - whenever an emitter changes state the DeveloperGUI will be listening for the change and then update its display to reflect that change.

### DeveloperGUI depends on GestureRecognizer ###

The DeveloperGUI will listen to the GestureRecognizer and change and update its display based off of events that are raised by it. This will be used for display what moves are currently being issued by what players in the game.