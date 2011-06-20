from ocempgui.widgets import ImageLabel
import pygame

class EmitterWidget(ImageLabel):
    
    onColour = pygame.Color(200,200,200,255)
    offColour = pygame.Color(128,128,128,128)
    p1Colour = pygame.Color(200,0,0,255)
    p2Colour = pygame.Color(0,200,200,255)
    bothOnColour = pygame.Color(0,0,200,255)
    
    def __init__(self, w=16, h=16):
        self.surface = pygame.Surface((w,h))
        ImageLabel.__init__(self,self.surface)
    
    def update_emitter(self,emitter):
        if emitter.flameIsOn:
            if emitter.p1ColourIsOn and emitter.p2ColourIsOn:
                self.surface.fill(EmitterWidget.bothOnColour)
            if emitter.p1ColourIsOn:
                self.surface.fill(EmitterWidget.p1Colour)
            elif emitter.p2ColourIsOn:
                self.surface.fill(EmitterWidget.p2Colour)
            else:
                self.surface.fill(EmitterWidget.onColour)
        else:
            self.surface.fill(EmitterWidget.offColour)
        
        self.set_dirty(True,True)
    
