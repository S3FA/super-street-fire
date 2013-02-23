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
        self._prevColour = None
        ImageLabel.__init__(self,self.surface)
    
    def update_emitter(self,emitter):
        """Fills this widget with a colour based on the emitter state.
        Returns True if the colour changed, False otherwise.
        """
        newColour = None
        if emitter.flameIsOn:
            if emitter.p1ColourIsOn and emitter.p2ColourIsOn:
                newColour = EmitterWidget.bothOnColour
            elif emitter.p1ColourIsOn:
                newColour = EmitterWidget.p1Colour
            elif emitter.p2ColourIsOn:
                newColour = EmitterWidget.p2Colour
            else:
                newColour = EmitterWidget.onColour
        else:
            newColour = EmitterWidget.offColour
        
        if newColour != self._prevColour:
            self.surface.fill(newColour)
            self.set_dirty(True, False) # set dirty but don't force immediate redraw
            self._prevColour = newColour
            return True
        
        return False