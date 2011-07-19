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
        self.framewidth = 800
        self.frameheight = 650
        screen = pygame.display.set_mode ((self.framewidth, self.frameheight));
        screen.fill ((255, 200, 100))

        base.GlobalStyle.load ('/'.join((os.path.dirname(__file__),'theme.rc')))

        self.renderer = Renderer ()
        self.renderer.screen = screen
        self.renderer.title = "SUPER STREET FIRE: TURBO CHAMPIONSHIP EDITION"
        self.renderer.color = (250, 250, 250)
        
        topmargin = 10
        logosize = (67,55)
        
        try:
            self._logger.debug("Loading logo from " + "/".join((os.path.dirname(__file__),'logo.bmp')))

            logo_img = Image.load_image('/'.join((os.path.dirname(__file__),'logo.bmp')))
            logo = ImageLabel (logo_img)
            logo.topleft = (self.framewidth/2 - logosize[0]/2, topmargin)
            self.renderer.add_widget(logo)
            
        except pygame.error:
            self._logger.warning("Couldn't load logo. crappy version of python on a mac? (try 2.6)")
        
        self.roundLabel = Label("Round %d" % self.game.roundNumber)
        self.roundLabel.topleft = (self.framewidth/2 - logosize[0]/2, topmargin + logosize[1] + 5)
        self.roundLabel.get_style()["font"]["size"] = 24
        self.roundLabel.minsize = (logosize[0], self.roundLabel.minsize[1])
        self.renderer.add_widget(self.roundLabel)
        
        self.timerLabel = Label("99")
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
        
        ###################
        # Game Control
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
        self.demoBtn.connect_signal(Constants.SIG_CLICKED, self.game.DemoGame)
        buttonTable.add_child(0,3,self.demoBtn)
        
        gameControlFrame.topleft = (10, topmargin)
        gameControlFrame.add_child(buttonTable)
        gameControlFrame.set_align (ALIGN_LEFT)
        self.renderer.add_widget (gameControlFrame)
        
        
        fpsFrame = HFrame(Label(' FPS '))
        self.fps = 0
        self.fpsLabel = Label('0 fps')
        fpsFrame.add_child(self.fpsLabel)
        fpsFrame.topleft = (10, gameControlFrame.topleft[1]+gameControlFrame.height+8)
        self.renderer.add_widget(fpsFrame)
        
        ##################
        # Simulator
        self.leftEmitters = Label("  ".join('O'*8))
        self.leftEmitters.get_style()["font"]["name"] = "Consolas"
        self.leftEmitters.get_style()["font"]["size"] = 40
        self.leftEmitters.topleft = (self.framewidth/2.0 - self.leftEmitters.width/2.,160)
        
        self.rightEmitters = Label("  ".join('O'*8))
        self.rightEmitters.get_style()["font"]["name"] = "Consolas"
        self.rightEmitters.get_style()["font"]["size"] = 40
        self.rightEmitters.topleft = (self.framewidth/2.0 - self.leftEmitters.width/2.,264)
        
        self.renderer.add_widget(self.leftEmitters)
        self.renderer.add_widget(self.rightEmitters)
        
        
        ##################
        # Health
        p1HealthFrame = HFrame(Label(" Player 1 Health "))
        self.p1Health = ProgressBar()
        self.p1Health.value = 100.0
        p1HealthFrame.add_child(self.p1Health)
        p1HealthFrame.topleft = (self.leftEmitters.topleft[0], 210)
        self.renderer.add_widget(p1HealthFrame)
        
        p2HealthFrame = HFrame(Label(" Player 2 Health "))
        self.p2Health = ProgressBar()
        self.p2Health.value = 100.0
        p2HealthFrame.add_child(self.p2Health)
        p2HealthFrame.topleft = (self.leftEmitters.topleft[0]+self.leftEmitters.width - p2HealthFrame.width, 210)
        self.renderer.add_widget(p2HealthFrame)

        
        ###############
        # Status        
        self.statusLabel = Label("Status")
        self.statusLabel.topleft = (100, 80)
        self.statusLabel.get_style()["font"]["size"] = 26
        self.statusLabel.minsize = (logosize[0], self.estop.minsize[1])
        self.renderer.add_widget(self.statusLabel)

        
        
        ###############
        # Move Logs
        p1moveframe = HFrame (Label ("Player 1 Moves"))
        p1moveframe.topleft = (10, fpsFrame.topleft[1]+fpsFrame.height+8)
        p1moveInfo = ScrolledList(100, 100, ListItemCollection())
        self.p1moves = ListItemCollection()
        self.p1moves.append (TextListItem ('No moves yet') )
        p1moveInfo.set_items( self.p1moves )
        p1moveframe.add_child(p1moveInfo)
        p1moveframe.topleft = (p1HealthFrame.topleft[0], 412)
        self.renderer.add_widget(p1moveframe)        
        
        p2moveframe = HFrame (Label ("Player 2 Moves"))
        p2moveframe.topleft = (10, fpsFrame.topleft[1]+fpsFrame.height+8)
        p2moveInfo = ScrolledList(100, 100, ListItemCollection())
        self.p2moves = ListItemCollection()
        self.p2moves.append (TextListItem ('No moves yet') )
        p2moveInfo.set_items( self.p2moves )
        p2moveframe.add_child(p2moveInfo)
        p2moveframe.topleft = (p2HealthFrame.topleft[0], 412)
        self.renderer.add_widget(p2moveframe)        
        
        
        
        #################
        # Test Moves
        
        # table (rows, cols)
        moveTestFrame = HFrame (Label ("Test Moves"))
        moveTestFrame.minsize = 200, 70
        moveTestFrame.topleft = (p1HealthFrame.topleft[0], 320)
        
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
        
        
        #################
        # Hardware
        hwFrame = HFrame (Label (" Hardware "))
        hwFrame.topleft = (10, 550)
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
        
        
        #################
        # Peripherals
        peripheralFrame = HFrame (Label (" Peripherals "))
        
        hwTable = Table(2,12)
        hwTable.spacing = 5
        
        maxw = 70
        maxh = 24
        
        self.p1LeftGloveRSSI = ProgressBar()
        self.p1LeftGloveRSSI.set_minimum_size(maxw,maxh)
        self.p1LeftGloveRSSI.value = 0
        self.p1LeftGloveRSSI.text = 'P1 Left'
        hwTable.add_child(0,0,self.p1LeftGloveRSSI)
        
        self.p1LeftGloveAddr = Label("")
        self.p1LeftGloveAddr.text = hexlify(ioserver.xbeeio.parser.getAddrS("SSFP1L"))
        hwTable.add_child(0,1,self.p1LeftGloveAddr)
        
        self.p1RightGloveRSSI = ProgressBar()
        self.p1RightGloveRSSI.set_minimum_size(maxw,maxh)
        self.p1RightGloveRSSI.value = 0
        self.p1RightGloveRSSI.text = 'P1 Right'
        hwTable.add_child(1,0,self.p1RightGloveRSSI)
        
        self.p1RightGloveAddr = Label("")
        self.p1RightGloveAddr.text = hexlify(ioserver.xbeeio.parser.getAddrS("SSFP1R"))
        hwTable.add_child(1,1,self.p1RightGloveAddr)
        
        
        self.p2LeftGloveRSSI = ProgressBar()
        self.p2LeftGloveRSSI.set_minimum_size(maxw,maxh)
        self.p2LeftGloveRSSI.value = 0
        self.p2LeftGloveRSSI.text = 'P2 Left'
        hwTable.add_child(0,2,self.p2LeftGloveRSSI)
        
        self.p2LeftGloveAddr = Label("")
        self.p2LeftGloveAddr.text = hexlify(ioserver.xbeeio.parser.getAddrS("SSFP2L"))
        hwTable.add_child(0,3,self.p2LeftGloveAddr)
        
        self.p2RightGloveRSSI = ProgressBar()
        self.p2RightGloveRSSI.set_minimum_size(maxw,maxh)
        self.p2RightGloveRSSI.value = 0
        self.p2RightGloveRSSI.text = 'P2 Right'
        hwTable.add_child(1,2,self.p2RightGloveRSSI)
        
        self.p2RightGloveAddr = Label("")
        self.p2RightGloveAddr.text = hexlify(ioserver.xbeeio.parser.getAddrS("SSFP2R"))
        hwTable.add_child(1,3,self.p2RightGloveAddr)
        
        
        self.p1HeadsetRSSI = ProgressBar()
        self.p1HeadsetRSSI.set_minimum_size(maxw,maxh)
        self.p1HeadsetRSSI.value = 0
        self.p1HeadsetRSSI.text = 'P1 Headset'
        hwTable.add_child(0,4,self.p1HeadsetRSSI)
        
        self.p1HeadsetAddr = Label("")
        self.p1HeadsetAddr.text = hexlify(ioserver.xbeeio.parser.getAddrS("SSFP1H"))
        hwTable.add_child(0,5,self.p1HeadsetAddr)
        
        self.p2HeadsetRSSI = ProgressBar()
        self.p2HeadsetRSSI.set_minimum_size(maxw,maxh)
        self.p2HeadsetRSSI.value = 0
        self.p2HeadsetRSSI.text = 'P2 Headset'
        hwTable.add_child(1,4,self.p2HeadsetRSSI)
        
        self.p2HeadsetAddr = Label("")
        self.p2HeadsetAddr.text = hexlify(ioserver.xbeeio.parser.getAddrS("SSFP2H"))
        hwTable.add_child(1,5,self.p2HeadsetAddr)
        
        self.TimerRSSI = ProgressBar()
        self.TimerRSSI.set_minimum_size(maxw,maxh)
        self.TimerRSSI.value = 0
        self.TimerRSSI.text = 'Timer'
        hwTable.add_child(0,6,self.TimerRSSI)
        
        self.TimerAddr = Label("")
        self.TimerAddr.text = hexlify(ioserver.xbeeio.parser.getAddrS("SSFTIMER"))
        hwTable.add_child(0,7,self.TimerAddr)
        
        self.p1LifeRSSI = ProgressBar()
        self.p1LifeRSSI.set_minimum_size(maxw,maxh)
        self.p1LifeRSSI.value = 0
        self.p1LifeRSSI.text = 'P1 Life'
        hwTable.add_child(1,6,self.p1LifeRSSI)
        
        self.p1LifeAddr = Label("")
        self.p1LifeAddr.text = hexlify(ioserver.xbeeio.parser.getAddrS("SSFP1LIFE"))
        hwTable.add_child(1,7,self.p1LifeAddr)
        
        self.p2LifeRSSI = ProgressBar()
        self.p2LifeRSSI.set_minimum_size(maxw,maxh)
        self.p2LifeRSSI.value = 0
        self.p2LifeRSSI.text = 'P2 Life'
        hwTable.add_child(0,8,self.p2LifeRSSI)
        
        self.p2LifeAddr = Label("")
        self.p2LifeAddr.text = hexlify(ioserver.xbeeio.parser.getAddrS("SSFP2LIFE"))
        hwTable.add_child(0,9,self.p2LifeAddr)
        
        self.koRSSI = ProgressBar()
        self.koRSSI.set_minimum_size(maxw,maxh)
        self.koRSSI.value = 0
        self.koRSSI.text = 'KO Box'
        hwTable.add_child(1,8,self.koRSSI)
                
        self.koAddr = Label("")
        self.koAddr.text = hexlify(ioserver.xbeeio.parser.getAddrS("SSFKO"))
        hwTable.add_child(1,9,self.koAddr)
        
        self.FireRSSI = ProgressBar()
        self.FireRSSI.set_minimum_size(maxw,maxh)
        self.FireRSSI.value = 0
        self.FireRSSI.text = 'Fire Control'
        hwTable.add_child(0,10,self.FireRSSI)
        
        self.FireAddr = Label("")
        self.FireAddr.text = hexlify(ioserver.xbeeio.parser.getAddrS("SSFFIRE"))
        hwTable.add_child(0,11,self.FireAddr)
        
        self.LightsRSSI = ProgressBar()
        self.LightsRSSI.set_minimum_size(maxw,maxh)
        self.LightsRSSI.value = 0
        self.LightsRSSI.text = 'Lighting'
        hwTable.add_child(1,10,self.LightsRSSI)
        
        self.LightsAddr = Label("")
        self.LightsAddr.text = hexlify(ioserver.xbeeio.parser.getAddrS("SSFLIGHTS"))
        hwTable.add_child(1,11,self.LightsAddr)
        
        peripheralFrame.topleft = (hwFrame.topleft[0] + hwFrame.width + 4, hwFrame.topleft[1])
        peripheralFrame.add_child(hwTable)
        self.renderer.add_widget(peripheralFrame)
        
        #################
        # Fire Test
        ftFrame = HFrame (Label (" Fire Test "))
        ftFrame.topleft = (650, 550)
        ftBtnTable = Table(2,1)
        
        self.allOnBtn = Button(" Fire All ")
        self.allOnBtn.connect_signal(Constants.SIG_CLICKED, self.ioListener.AllFireOn)
        ftBtnTable.add_child(0,0,self.allOnBtn)
        
        self.allOffBtn = Button(" Fire Off ")
        self.allOffBtn.connect_signal(Constants.SIG_CLICKED, self.ioListener.AllFireOff)
        ftBtnTable.add_child(1,0,self.allOffBtn)

        ftFrame.add_child(ftBtnTable)
        ftFrame.set_align(ALIGN_LEFT)
        self.renderer.add_widget(ftFrame)
        
        

    def set_fps(self,fps):
        self.fps = fps
        self.fpsLabel.text = '%.2f fps' % fps

    
    
    ##################
    ## Updating
    ##################
    
    def updateEmitters(self):
        leftEmitterStr  = "  ".join([char_for_state(state) for state in self.game.GetLeftEmitterArc(1)])
        rightEmitterStr = "  ".join([char_for_state(state) for state in self.game.GetRightEmitterArc(1)])
        
        # Only update the emitter text when we need to
        if leftEmitterStr != self.leftEmitters.text:
            self.leftEmitters.text = leftEmitterStr
        if rightEmitterStr != self.rightEmitters.text:
            self.rightEmitters.text = rightEmitterStr

    
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
        elif cur_state == game_states.ROUND_ENDED_GAME_STATE:
            self.timerLabel.text = '0'
            self.statusLabel.text = 'Round ' + str(state.roundNumber) + ': Winner ' + str(state.roundWinner)
        elif cur_state == game_states.MATCH_OVER_GAME_STATE:
            self.statusLabel.text = 'Match Won by Player ' + str(state.winnerPlayerNum)
        
        self.startBtn.sensitive = cur_state == game_states.IDLE_GAME_STATE or cur_state == game_states.ROUND_ENDED_GAME_STATE
        self.cancelMatchBtn.sensitive = cur_state != game_states.IDLE_GAME_STATE

        if cur_state == game_states.PAUSED_GAME_STATE:
            self.pauseBtn.text = "Unpause Game"
        else:
            self.pauseBtn.text = "Pause Game"
        
        
    def OnTimerStateChanged(self, newTime):
        if newTime < 0:
            newTime = 0
        
        # Only update the timer when we need to
        timeStr = '%.0f' % newTime
        if self.timerLabel.text != timeStr:
            self.timerLabel.text = timeStr
    
    def OnEmitterStateChanged(self):
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
            if actions[x].playerNum == 1:
                self.p1moves.insert (x, TextListItem (str(actions[x])) )
            else:
                self.p2moves.insert (x, TextListItem (str(actions[x])) )


def char_for_state(emitter_state):
    if emitter_state.flameIsOn:
        if emitter_state.p1ColourIsOn and emitter_state.p2ColourIsOn:
            return "B"
        elif emitter_state.p1ColourIsOn:
            return "1"
        elif emitter_state.p2ColourIsOn:
            return "2"
        else:
            return "X"
    else:
        return "O"
