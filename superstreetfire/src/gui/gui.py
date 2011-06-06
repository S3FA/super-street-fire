'''
gui.py

GUI (widgets and controllers) for super street fire

@author: greg
'''

from __future__ import division
import os
import pygame
from ocempgui.widgets import * # http://ocemp.sourceforge.net/guidown.html
from ocempgui.draw import Image
from gamemodel.game_model_listener import GameModelListener
from gamemodel import game_states

class UIController(GameModelListener):
    def __init__(self, ssfGame):
        GameModelListener.__init__(self)
        self.game = ssfGame
        self._initUI(ssfGame)
        self.game.RegisterListener(self)
    
        
    def _initUI(self,ssfGame):
        # Initialize the drawing window.
        w = 800
        h = 600
        screen = pygame.display.set_mode ((w, h));
        screen.fill ((255, 200, 100))

        self.renderer = Renderer ()
        self.renderer.screen = screen
        self.renderer.title = "Super Street Fire"
        self.renderer.color = (250, 250, 250)
        
        topmargin = 5
        logosize = (67,55)
        
        try:
            logo_img = Image.load_image('/'.join((os.path.dirname(__file__),'logo.jpeg')))
            logo = ImageLabel (logo_img)
            logo.topleft = (w/2 - logosize[0]/2, topmargin)
            self.renderer.add_widget(logo)
        except pygame.error:
            print "couldn't load logo. crappy version of python on a mac? (try 2.6)"
        
        self.roundLabel = Label("Round %d" % self.game.roundNumber)
        self.roundLabel.topleft = (w/2 - logosize[0]/2, topmargin + logosize[1] + 5)
        
        self.renderer.add_widget(self.roundLabel)
        
        self.timerLabel = Label("60")
        self.timerLabel.topleft = (self.roundLabel.topleft[0], 
                                   self.roundLabel.topleft[1] + self.timerLabel.height + 5)
        self.renderer.add_widget(self.timerLabel)
        
        buttonTable = Table(7,1)
        buttonTable.spacing = 5
        buttonTable.topleft = (10, 10)
        
        startBtn = Button ("Start Round")
        startBtn.connect_signal(Constants.SIG_CLICKED, self.game.StartGame)
        buttonTable.add_child(0,0,startBtn)
        
        self.pauseBtn = Button ("Pause")
        self.pauseBtn.connect_signal(Constants.SIG_CLICKED, self.game.TogglePauseGame)
        buttonTable.add_child(1,0,self.pauseBtn)
        
        endRoundBtn = Button ("End Round")
        buttonTable.add_child(2,0,endRoundBtn)
        
        cancelMatchBtn = Button("Cancel Match")
        cancelMatchBtn.connect_signal(Constants.SIG_CLICKED, self.game.StopGame)
        buttonTable.add_child(3,0,cancelMatchBtn)
        
        detectBtn = Button("Detect Devices")
        buttonTable.add_child(4,0,detectBtn)
        
        calibrateBtn = Button("Calibrate")
        buttonTable.add_child(5,0,calibrateBtn)
        
        estop = Button("ESTOP")
        buttonTable.add_child(6,0,estop)
        
        self.renderer.add_widget (buttonTable)
        
        
        healthTable = Table(2,1)
        healthTable.spacing = 5
        healthTable.topleft = (600, topmargin)
        
        self.p1Health = ProgressBar()
        self.p1Health.text = "Player 1"
        self.p1Health.value = 100.0
        healthTable.add_child(0,0,self.p1Health)
        
        self.p2Health = ProgressBar()
        self.p2Health.text = "Player 2"
        self.p2Health.value = 100.0
        healthTable.add_child(1,0,self.p2Health)
        self.renderer.add_widget(healthTable)
        
        
        
        # what we need:
        # info: timer, round #, p1/p2 health, device status/link (RSSI)
        #       simulator, detected move, console log, p1/p2 att/med values, fire system armed status
        # btns: start round, pause round, end round, cancel match, detect devices, calibrate, ESTOP,
        #       move generation (e.g. trigger p1 hadouken), demo mode on/off
        


    def OnGameStateChanged(self, state):
        GameModelListener.OnGameStateChanged(self, state)
        print 'state change: %s ' % str(state)
        
        if state.GetStateType() == game_states.ROUND_BEGIN_GAME_STATE:
            self.roundLabel.text = "Round %d" % state.roundNumber
        
    def OnTimerStateChanged(self, newTime):
        self.timerLabel.text = '%.0f' % newTime
    
    def OnPlayerHealthChanged(self, players):
        self.p1Health = players[0].GetHealth()
        self.p2Health = players[1].GetHealth()
    
    
    def print_sth(self):
        print 'oh hai'
