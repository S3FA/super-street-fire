'''
gui.py

GUI (widgets and controllers) for super street fire

@author: greg
'''

import pygame
from ocempgui.widgets import * # http://ocemp.sourceforge.net/guidown.html
from gamemodel.game_model_listener import GameModelListener


class UIController(GameModelListener):
    def __init__(self, ssfGame):
        self.ssfGame = ssfGame
        self.initUI()
        self.ssfGame.RegisterListener(self)
    
        
    def initUI(self):
        # Initialize the drawing window.
        screen = pygame.display.set_mode ((800, 600));
        screen.fill ((255, 200, 100))

        self.renderer = Renderer ()
        self.renderer.screen = screen
        self.renderer.title = "Super Street Fire"
        self.renderer.color = (250, 250, 250)
        
        button = Button ("oh hai")
        button.topleft = (10, 10)
        button.connect_signal(Constants.SIG_CLICKED, self.print_sth)
        self.renderer.add_widget (button)
        


    def OnGameStateChanged(self, newGameState):
        GameModelListener.OnGameStateChanged(self, newGameState)
        print 'state change: %s ' % str(newGameState)
    
    def print_sth(self):
        print 'oh hai'
