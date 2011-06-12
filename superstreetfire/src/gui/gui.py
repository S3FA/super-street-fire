'''
gui.py

GUI (widgets and controllers) for super street fire

@author: greg
'''

from __future__ import division
import os
import sys
import pygame
from ocempgui.widgets import * # http://ocemp.sourceforge.net/guidown.html
from ocempgui.draw import Image
from ocempgui.draw import String
from ocempgui.widgets.components import *
from ocempgui.widgets.Constants import *
from ocempgui.object import BaseObject
from ocempgui.events import EventManager
from gamemodel.game_model_listener import GameModelListener
from gamemodel import *
import ioserver
from binascii import hexlify

class UIController(GameModelListener):
    def __init__(self, ssfGame, receiverObj):
        GameModelListener.__init__(self)
        self.game = ssfGame
        self.receiver = receiverObj
        self._initUI(ssfGame)
        self.game.RegisterListener(self)
    
    def _initUI(self,ssfGame):
        # Initialize the drawing window.
        w = 800
        h = 600
        screen = pygame.display.set_mode ((w, h));
        screen.fill ((255, 200, 100))

        self.sans = String.create_system_font ("Sans", 10)

        self.renderer = Renderer ()
        self.renderer.screen = screen
        self.renderer.title = "Super Street Fire"
        self.renderer.color = (250, 250, 250)
        
        topmargin = 5
        logosize = (67,55)
        
        try:
            logo_img = Image.load_image('/'.join((os.path.dirname(__file__),'logo.bmp')))
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
        
        gameControlFrame = HFrame (Label (" Game Control "))
        gameControlFrame.topleft = (10, 10)

        buttonTable = Table(7,1)
        buttonTable.spacing = 5
        buttonTable.set_column_align (0, ALIGN_LEFT)

        
        self.startBtn = Button ("Start Round")
        self.startBtn.connect_signal(Constants.SIG_CLICKED, self.game.StartGame)
        buttonTable.add_child(0,0,self.startBtn)
        
        self.pauseBtn = Button ("Pause Game")
        self.pauseBtn.connect_signal(Constants.SIG_CLICKED, self.game.TogglePauseGame)
        buttonTable.add_child(1,0,self.pauseBtn)
        
        # is end round really necessary? not straightforward to implement with current state machine 
        #self.endRoundBtn = Button ("End Round")
        #self.endRoundBtn.connect_signal(Constants.SIG_CLICKED, self.game.TogglePauseGame)
        #buttonTable.add_child(2,0,self.endRoundBtn)
        
        self.cancelMatchBtn = Button("Cancel Match")
        self.cancelMatchBtn.connect_signal(Constants.SIG_CLICKED, self.game.StopGame)
        buttonTable.add_child(3,0,self.cancelMatchBtn)
        
        self.detectBtn = Button("Detect Devices")
        self.detectBtn.connect_signal(Constants.SIG_CLICKED, self.receiver.NodeDiscovery)
        buttonTable.add_child(4,0,self.detectBtn)
        
        self.calibrateBtn = Button("Calibrate Game")
        #self.calibrateBtn.connect_signal(Constants.SIG_CLICKED, self.game.Calibrate)
        buttonTable.add_child(5,0,self.calibrateBtn)
        
        self.estop = Button("STOP ALL")
        #self.estop.connect_signal(Constants.SIG_CLICKED, OMGWTFBBQ)
        buttonTable.add_child(6,0,self.estop)

        gameControlFrame.add_child(buttonTable)
        gameControlFrame.set_align (ALIGN_LEFT)
        self.renderer.add_widget (gameControlFrame)
        
        
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
        
        moveframe = HFrame (Label ("Recent moves"))
        moveframe.topleft = (140, 20)
        moveInfo = ScrolledList(100, 100, ListItemCollection())
        self.moves = ListItemCollection()
        self.moves.append (TextListItem ('No moves yet') )
        moveInfo.set_items( self.moves )
        moveframe.add_child(moveInfo)
        self.renderer.add_widget(moveframe)        
        
        # table (rows, cols)
        moveTestFrame = HFrame (Label ("Test Moves"))
        moveTestFrame.minsize = 200, 70
        moveTestFrame.topleft = (10, 220)
        
        moveTable = Table(2,8)
        moveTable.spacing = 4
        self.p1jab = Button("P1 Jab")
        self.p1jab.connect_signal(Constants.SIG_CLICKED, self.game.TestP1Jab )
        self.p2jab = Button("P2 Jab")
        self.p2jab.connect_signal(Constants.SIG_CLICKED, self.game.TestP2Jab )
        self.p1hok = Button("P1 Hook")
        self.p1hok.connect_signal(Constants.SIG_CLICKED, self.game.TestP1Hook )
        self.p2hok = Button("P2 Hook")
        self.p2hok.connect_signal(Constants.SIG_CLICKED, self.game.TestP2Hook )
        self.p1had = Button("P1 Hadouken")
        self.p1had.connect_signal(Constants.SIG_CLICKED, self.game.TestP1Hadouken )
        self.p2had = Button("P2 Hadouken")
        self.p2had.connect_signal(Constants.SIG_CLICKED, self.game.TestP2Hadouken )
        self.p1sbm = Button("P1 Sonic Boom")
        self.p1sbm.connect_signal(Constants.SIG_CLICKED, self.game.TestP1Hadouken )
        self.p2sbm = Button("P2 Sonic Boom")
        self.p2sbm.connect_signal(Constants.SIG_CLICKED, self.game.TestP2Hadouken )
        self.p1blk = Button("P1 Block")
        self.p1blk.connect_signal(Constants.SIG_CLICKED, self.game.TestP1Block )
        self.p2blk = Button("P2 Block")
        self.p2blk.connect_signal(Constants.SIG_CLICKED, self.game.TestP2Block )

        moveTable.add_child(0,0,self.p1jab)
        moveTable.add_child(1,0,self.p2jab)
        moveTable.add_child(0,1,self.p1hok)
        moveTable.add_child(1,1,self.p2hok)
        moveTable.add_child(0,2,self.p1had)
        moveTable.add_child(1,2,self.p2had)
        moveTable.add_child(0,3,self.p1sbm)
        moveTable.add_child(1,3,self.p2sbm)
        moveTable.add_child(0,4,self.p1blk)
        moveTable.add_child(1,4,self.p2blk)

        moveTestFrame.add_child(moveTable)
        self.renderer.add_widget(moveTestFrame)
        
        hwTable = Table(12,2)
        hwTable.spacing = 5
        hwTable.add_child(0,0,Label('RSSI'))
        hwTable.add_child(0,1,Label('Address'))
        
        self.p1LeftGloveRSSI = ProgressBar()
        self.p1LeftGloveRSSI.value = 0
        self.p1LeftGloveRSSI.text = 'P1 Left Glove'
        hwTable.add_child(1,0,self.p1LeftGloveRSSI)
        
        self.p1LeftGloveAddr = Label("")
        self.p1LeftGloveAddr.text = hexlify(ioserver.xbeeio.parser.getAddrS("SSFP1L"))
        hwTable.add_child(1,1,self.p1LeftGloveAddr)
        
        self.p1RightGloveRSSI = ProgressBar()
        self.p1RightGloveRSSI.value = 0
        self.p1RightGloveRSSI.text = 'P1 Right Glove'
        hwTable.add_child(2,0,self.p1RightGloveRSSI)
        
        self.p1RightGloveAddr = Label("")
        self.p1RightGloveAddr.text = hexlify(ioserver.xbeeio.parser.getAddrS("SSFP1R"))
        hwTable.add_child(2,1,self.p1RightGloveAddr)
        
        self.p1HeadsetRSSI = ProgressBar()
        self.p1HeadsetRSSI.value = 0
        self.p1HeadsetRSSI.text = 'P1 Headset'
        hwTable.add_child(3,0,self.p1HeadsetRSSI)
        
        self.p1HeadsetAddr = Label("")
        self.p1HeadsetAddr.text = hexlify(ioserver.xbeeio.parser.getAddrS("SSFP1H"))
        hwTable.add_child(3,1,self.p1HeadsetAddr)
        
        self.p2LeftGloveRSSI = ProgressBar()
        self.p2LeftGloveRSSI.value = 0
        self.p2LeftGloveRSSI.text = 'P2 Left Glove'
        hwTable.add_child(4,0,self.p2LeftGloveRSSI)
        
        self.p2LeftGloveAddr = Label("")
        self.p2LeftGloveAddr.text = hexlify(ioserver.xbeeio.parser.getAddrS("SSFP2L"))
        hwTable.add_child(4,1,self.p2LeftGloveAddr)
        
        self.p2RightGloveRSSI = ProgressBar()
        self.p2RightGloveRSSI.value = 0
        self.p2RightGloveRSSI.text = 'P2 Right Glove'
        hwTable.add_child(5,0,self.p2RightGloveRSSI)
        
        self.p2RightGloveAddr = Label("")
        self.p2RightGloveAddr.text = hexlify(ioserver.xbeeio.parser.getAddrS("SSFP2R"))
        hwTable.add_child(5,1,self.p2RightGloveAddr)
        
        self.p2HeadsetRSSI = ProgressBar()
        self.p2HeadsetRSSI.value = 0
        self.p2HeadsetRSSI.text = 'P2 Headset'
        hwTable.add_child(6,0,self.p2HeadsetRSSI)
        
        self.p2HeadsetAddr = Label("")
        self.p2HeadsetAddr.text = hexlify(ioserver.xbeeio.parser.getAddrS("SSFP2H"))
        hwTable.add_child(6,1,self.p2HeadsetAddr)
        
        self.TimerRSSI = ProgressBar()
        self.TimerRSSI.value = 0
        self.TimerRSSI.text = 'Timer'
        hwTable.add_child(7,0,self.TimerRSSI)
        
        self.TimerAddr = Label("")
        self.TimerAddr.text = hexlify(ioserver.xbeeio.parser.getAddrS("SSFTIMER"))
        hwTable.add_child(7,1,self.TimerAddr)
        
        self.p1LifeRSSI = ProgressBar()
        self.p1LifeRSSI.value = 0
        self.p1LifeRSSI.text = 'P1 Life'
        hwTable.add_child(8,0,self.p1LifeRSSI)
        
        self.p1LifeAddr = Label("")
        self.p1LifeAddr.text = hexlify(ioserver.xbeeio.parser.getAddrS("SSFP1LIFE"))
        hwTable.add_child(8,1,self.p1LifeAddr)
        
        self.p2LifeRSSI = ProgressBar()
        self.p2LifeRSSI.value = 0
        self.p2LifeRSSI.text = 'P2 Life'
        hwTable.add_child(9,0,self.p2LifeRSSI)
        
        self.p2LifeAddr = Label("")
        self.p2LifeAddr.text = hexlify(ioserver.xbeeio.parser.getAddrS("SSFP2LIFE"))
        hwTable.add_child(9,1,self.p2LifeAddr)
        
        self.FireRSSI = ProgressBar()
        self.FireRSSI.value = 0
        self.FireRSSI.text = 'Fire Control'
        hwTable.add_child(10,0,self.FireRSSI)
        
        self.FireAddr = Label("")
        self.FireAddr.text = hexlify(ioserver.xbeeio.parser.getAddrS("SSFFIRE"))
        hwTable.add_child(10,1,self.FireAddr)
        
        self.LightsRSSI = ProgressBar()
        self.LightsRSSI.value = 0
        self.LightsRSSI.text = 'Lighting'
        hwTable.add_child(11,0,self.LightsRSSI)
        
        self.LightsAddr = Label("")
        self.LightsAddr.text = hexlify(ioserver.xbeeio.parser.getAddrS("SSFLIGHTS"))
        hwTable.add_child(11,1,self.LightsAddr)
        
        hwTable.topleft = (600,250)
        self.renderer.add_widget(hwTable)
        
        # what we need:
        # info: timer, round #, p1/p2 health, device status/link (RSSI)
        #       simulator, detected move, console log, p1/p2 att/med values, fire system armed status
        # btns: start round, pause round, end round, cancel match, detect devices, calibrate, ESTOP,
        #       move generation (e.g. trigger p1 hadouken), demo mode on/off
        #
        # snb: 
        # detected move
        # cancel match, detect devices, calibrate, ESTOP,
        # move generation (e.g. trigger p1 hadouken), demo mode on/off

    def OnGameStateChanged(self, state):
        GameModelListener.OnGameStateChanged(self, state)
        print 'state change: %s ' % str(state)
        
        cur_state = state.GetStateType()
        
        # update round # / timer
        if cur_state == game_states.ROUND_BEGIN_GAME_STATE:
            self.roundLabel.text = "Round %d" % state.roundNumber
        elif cur_state == game_states.IDLE_GAME_STATE:
            # everything reset
            self.roundLabel.text = '-'
            self.timerLabel.text = '-'
        
        self.startBtn.sensitive = cur_state == game_states.IDLE_GAME_STATE
        self.cancelMatchBtn.sensitive = cur_state != game_states.IDLE_GAME_STATE

        if cur_state == game_states.PAUSED_GAME_STATE:
            self.pauseBtn.text = "Restart Game"
        else:
            self.pauseBtn.text = "Pause Game"
        
        
    def OnTimerStateChanged(self, newTime):
        self.timerLabel.text = '%.0f' % newTime
    
    def OnPlayerHealthChanged(self, players):
        try:
            if (players[0].GetHealth() < 0):
                self.p1Health.value = 0
            else:
                self.p1Health.value = players[0].GetHealth()
            if (players[1].GetHealth() < 0):
                self.p2Health.value = 0
            else:
                self.p2Health.value = players[1].GetHealth()
        except:
            print "Health change progess bar error ", sys.exc_info()[0]
        
    def OnHWAddrChanged(self, hwaddr):
        self.p1LeftGloveAddr.text  = hexlify(hwaddr["SSFP1L"][1])
        self.p1RightGloveAddr.text = hexlify(hwaddr["SSFP1R"][1])
        self.p1HeadsetAddr.text    = hexlify(hwaddr["SSFP1H"][1])
        self.p2LeftGloveAddr.text  = hexlify(hwaddr["SSFP2L"][1])
        self.p2RightGloveAddr.text = hexlify(hwaddr["SSFP2R"][1])
        self.p2HeadsetAddr.text    = hexlify(hwaddr["SSFP2H"][1])
        self.TimerAddr.text        = hexlify(hwaddr["SSFTIMER"][1])
        self.p1LifeAddr.text       = hexlify(hwaddr["SSFP1LIFE"][1])
        self.p2LifeAddr.text       = hexlify(hwaddr["SSFP2LIFE"][1])
        self.FireAddr.text         = hexlify(hwaddr["SSFFIRE"][1])

    def OnRSSIChanged(self,rssi_dict):
        pass
        
    def OnPlayerMoves(self, actions):
        for x in range(0,len(actions)):
            self.moves.insert (x, TextListItem (str(actions[x])) )