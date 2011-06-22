'''
gui.py

GUI (widgets and controllers) for super street fire

@author: greg
'''

from __future__ import division
import os
import sys
import pygame
import logging
from ocempgui.widgets import * # http://ocemp.sourceforge.net/guidown.html
from ocempgui.draw import Image
from ocempgui.widgets.components import *
from ocempgui.widgets.Constants import *
from ocempgui.object import BaseObject
from ocempgui.events import EventManager
from gamemodel.game_model_listener import GameModelListener
from gamemodel import *
from ssf_widgets import EmitterWidget
import ioserver
from binascii import hexlify


class UIController(GameModelListener):
    LOGGER_NAME = 'gui_logger'
    
    def __init__(self, ssfGame, ioListener):
        GameModelListener.__init__(self)
        self._logger = logging.getLogger(UIController.LOGGER_NAME)
        self.game = ssfGame
        self.ioListener = ioListener
        self._initUI(ssfGame)
        self.game.RegisterListener(self)
    
    def _initUI(self,ssfGame):
        # Initialize the drawing window.
        w = 800
        h = 600
        screen = pygame.display.set_mode ((w, h));
        screen.fill ((255, 200, 100))

        base.GlobalStyle.load ('/'.join((os.path.dirname(__file__),'theme.rc')))

        self.renderer = Renderer ()
        self.renderer.screen = screen
        self.renderer.title = "Super Street Fire"
        self.renderer.color = (250, 250, 250)
        
        topmargin = 10
        logosize = (67,55)
        
        try:
            self._logger.debug("Loading logo from " + "/".join((os.path.dirname(__file__),'logo.bmp')))

            logo_img = Image.load_image('/'.join((os.path.dirname(__file__),'logo.bmp')))
            logo = ImageLabel (logo_img)
            logo.topleft = (w/2 - logosize[0]/2, topmargin)
            self.renderer.add_widget(logo)
            
        except pygame.error:
            self._logger.warning("Couldn't load logo. crappy version of python on a mac? (try 2.6)")
        
        self.roundLabel = Label("Round %d" % self.game.roundNumber)
        self.roundLabel.topleft = (w/2 - logosize[0]/2, topmargin + logosize[1] + 5)
        self.roundLabel.get_style()["font"]["size"] = 24
        self.roundLabel.minsize = (logosize[0], self.roundLabel.minsize[1])
        self.renderer.add_widget(self.roundLabel)
        
        self.timerLabel = Label("60")
        self.timerLabel.topleft = (self.roundLabel.topleft[0], 
                                   self.roundLabel.topleft[1] + self.timerLabel.height + 5)
        self.timerLabel.get_style()["font"]["size"] = 32
        self.timerLabel.minsize = (logosize[0], self.roundLabel.minsize[1])
        self.renderer.add_widget(self.timerLabel)
        
        self.estop = Button("STOP ALL")
        self.estop.topleft = (self.timerLabel.topleft[0],
                              self.timerLabel.topleft[1] + self.roundLabel.height + 5)
        self.estop.minsize = (logosize[0], self.estop.minsize[1])
        self.estop.connect_signal(Constants.SIG_CLICKED, self.game.StopAll)
        self.renderer.add_widget(self.estop)
        
        
        gameControlFrame = HFrame (Label (" Game Control "))

        buttonTable = Table(1,4)
        buttonTable.spacing = 5
        buttonTable.set_column_align (0, ALIGN_LEFT)

        
        self.startBtn = Button ("Start Round")
        self.startBtn.connect_signal(Constants.SIG_CLICKED, self.game.StartGame)
        buttonTable.add_child(0,0,self.startBtn)
        
        self.pauseBtn = Button ("Pause Game")
        self.pauseBtn.connect_signal(Constants.SIG_CLICKED, self.game.TogglePauseGame)
        buttonTable.add_child(0,1,self.pauseBtn)
        
        # is end round really necessary? not straightforward to implement with current state machine 
        #self.endRoundBtn = Button ("End Round")
        #self.endRoundBtn.connect_signal(Constants.SIG_CLICKED, self.game.TogglePauseGame)
        #buttonTable.add_child(2,0,self.endRoundBtn)
        
        self.cancelMatchBtn = Button("Cancel Match")
        self.cancelMatchBtn.connect_signal(Constants.SIG_CLICKED, self.game.StopGame)
        buttonTable.add_child(0,2,self.cancelMatchBtn)
        
        self.demoBtn = Button("Demo")
        #self.cancelMatchBtn.connect_signal(Constants.SIG_CLICKED, SweetDemo)
        buttonTable.add_child(0,3,self.demoBtn)
        
        gameControlFrame.topleft = (10, topmargin)
        gameControlFrame.add_child(buttonTable)
        gameControlFrame.set_align (ALIGN_LEFT)
        self.renderer.add_widget (gameControlFrame)
        
        
        hwFrame = HFrame (Label (" Hardware "))
        hwFrame.topleft = (w/2 - logosize[0]/2 + logosize[0] + 48, topmargin)
        hwBtnTable = Table(2,1)
        
        self.detectBtn = Button("Detect Devices")
        self.detectBtn.connect_signal(Constants.SIG_CLICKED, self.ioListener.DetectDevices)
        hwBtnTable.add_child(0,0,self.detectBtn)
        
        self.calibrateBtn = Button("Calibrate Game")
        #self.calibrateBtn.connect_signal(Constants.SIG_CLICKED, self.game.Calibrate)
        hwBtnTable.add_child(1,0,self.calibrateBtn)

        hwFrame.add_child(hwBtnTable)
        hwFrame.set_align(ALIGN_LEFT)
        self.renderer.add_widget(hwFrame)
        
        
        
        
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
        moveframe.topleft = (10, gameControlFrame.topleft[1]+gameControlFrame.height+8)
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
        
        
        peripheralFrame = HFrame (Label (" Peripherals "))
        peripheralFrame.topleft = (575, 175)
        
        hwTable = Table(13,2)
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
        
        self.koRSSI = ProgressBar()
        self.koRSSI.value = 0
        self.koRSSI.text = 'KO Box'
        hwTable.add_child(10,0,self.koRSSI)
                
        self.koAddr = Label("")
        self.koAddr.text = hexlify(ioserver.xbeeio.parser.getAddrS("SSFKO"))
        hwTable.add_child(10,1,self.koAddr)
        
        self.FireRSSI = ProgressBar()
        self.FireRSSI.value = 0
        self.FireRSSI.text = 'Fire Control'
        hwTable.add_child(11,0,self.FireRSSI)
        
        self.FireAddr = Label("")
        self.FireAddr.text = hexlify(ioserver.xbeeio.parser.getAddrS("SSFFIRE"))
        hwTable.add_child(11,1,self.FireAddr)
        
        self.LightsRSSI = ProgressBar()
        self.LightsRSSI.value = 0
        self.LightsRSSI.text = 'Lighting'
        hwTable.add_child(12,0,self.LightsRSSI)
        
        self.LightsAddr = Label("")
        self.LightsAddr.text = hexlify(ioserver.xbeeio.parser.getAddrS("SSFLIGHTS"))
        hwTable.add_child(12,1,self.LightsAddr)
        
        hwTable.topleft = (600,200)
        peripheralFrame.add_child(hwTable)
        self.renderer.add_widget(peripheralFrame)
        
        # fire emitter simulator
        self.initEmitters()
        
        
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


    def initEmitters(self):
        self.leftEmitters = []
        self.rightEmitters = []
        startLeftX = 20
        startLeftY = 350
        startRightX = startLeftX
        startRightY = startLeftY + 50
        w = 16
        h = 16
        spacing = 12
        
        for i in range(0, fire_emitter.FireEmitter.NUM_FIRE_EMITTERS_PER_ARC):
            # straight line left to right layout of emitter widgets
            self.leftEmitters.append(EmitterWidget(w,h))
            self.leftEmitters[i].topleft = ((startLeftX + i*(w+spacing)), startLeftY)
            self.renderer.add_widget(self.leftEmitters[i])
            self.rightEmitters.append(EmitterWidget(w,h))
            self.rightEmitters[i].topleft = ((startRightX + i*(w+spacing)), startRightY)
            self.renderer.add_widget(self.rightEmitters[i])
    
    
    
    ##################
    ## Updating
    ##################
    
    def updateEmitters(self):
        something_changed = False
        for emitter,widget in zip(self.game.GetLeftEmitterArc(1), self.leftEmitters):
            if widget.update_emitter(emitter):
                something_changed = True
        for emitter,widget in zip(self.game.GetRightEmitterArc(1),self.rightEmitters):
            if widget.update_emitter(emitter):
                something_changed = True
        
        if something_changed:
            self.renderer.refresh()

    def OnGameStateChanged(self, state):
        GameModelListener.OnGameStateChanged(self, state)
        cur_state = state.GetStateType()
        
        # update round # / timer
        if cur_state == game_states.ROUND_BEGIN_GAME_STATE:
            self.roundLabel.text = "Round %d" % state.roundNumber
            self.OnPlayerHealthChanged((self.game.player1,self.game.player2))
        elif cur_state == game_states.IDLE_GAME_STATE:
            # everything reset
            self.roundLabel.text = '-'
            self.timerLabel.text = '-'
        
        self.startBtn.sensitive = cur_state == game_states.IDLE_GAME_STATE
        self.cancelMatchBtn.sensitive = cur_state != game_states.IDLE_GAME_STATE

        if cur_state == game_states.PAUSED_GAME_STATE:
            self.pauseBtn.text = "Unpause Game"
        else:
            self.pauseBtn.text = "Pause Game"
        
        
    def OnTimerStateChanged(self, newTime):
        if newTime < 0:
            newTime = 0
            
        self.timerLabel.text = '%.0f' % newTime
        self.updateEmitters()
        
    
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

