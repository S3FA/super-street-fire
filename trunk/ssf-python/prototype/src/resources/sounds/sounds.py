import pygame
from pygame.mixer import Sound
import os

pygame.mixer.init(frequency=22050, size=-16, channels=2, buffer=2042)

DIR = os.path.dirname(os.path.realpath(__file__))


blockSound = Sound(os.path.join(DIR,'impact_block.wav'))
kickRoundhouseSound = Sound(os.path.join(DIR,'impact_kickroundhouse.wav'))
kickShortSound = Sound(os.path.join(DIR,'impact_kickshort.wav'))
punchJabSound = Sound(os.path.join(DIR,'impact_punchjab.wav'))
punchMediumSound = Sound(os.path.join(DIR,'impact_punchmedium.wav'))
punchFierceSound = Sound(os.path.join(DIR,'impact_punchfierce.wav'))
swipeSound = Sound(os.path.join(DIR,'impact_swipe.wav'))

announceRound = Sound(os.path.join(DIR,'ann_round.wav'))
announceFight = Sound(os.path.join(DIR,'ann_fight.wav'))
announceFinal = Sound(os.path.join(DIR,'ann_final.wav'))
announceYou = Sound(os.path.join(DIR,'ann_you.wav'))
announceWin = Sound(os.path.join(DIR,'ann_win.wav'))
announceLose = Sound(os.path.join(DIR,'ann_lose.wav'))
announcePerfect = Sound(os.path.join(DIR,'ann_perfect.wav'))

spinningBirdKick = Sound(os.path.join(DIR,'chunli_spinningbirdkick.wav'))
yaTa = Sound(os.path.join(DIR,'chunli_yata.wav'))
yoga = Sound(os.path.join(DIR,'dhalsim_yoga.wav'))
fire = Sound(os.path.join(DIR,'dhalsim_fire.wav'))
flame = Sound(os.path.join(DIR,'dhalsim_flame.wav'))
doofGoy = Sound(os.path.join(DIR,'ehonda_doofgoy.wav'))
hadouken = Sound(os.path.join(DIR,'Hadoken.wav'))
shoryuken = Sound(os.path.join(DIR,'Shoryuken.wav'))
sonicBoom = Sound(os.path.join(DIR,'Sonic-boom.wav'))
tetsuMaki = Sound(os.path.join(DIR,'Tetsu-maki.wav'))
tiger = Sound(os.path.join(DIR,'Tiger.wav'))
uppercut = Sound(os.path.join(DIR,'Uppercut.wav'))
yodel = Sound(os.path.join(DIR,'Vega-victory.wav'))


def play_em_all():
    import time
    
    noises = [blockSound,kickRoundhouseSound,kickShortSound,
              punchJabSound,punchMediumSound,punchFierceSound,
              swipeSound,
              announceRound,announceFight,announceFinal,
              announceYou,announceWin,announceLose,announcePerfect,
              spinningBirdKick,yaTa,
              yoga,fire,flame,
              doofGoy,
              hadouken,shoryuken,tetsuMaki,
              sonicBoom,tiger,uppercut,yodel]
    
    for noise in noises:
        noise.play()
        time.sleep(noise.get_length())
    