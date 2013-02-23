
# Mar 28, 2011
# Jason Spitkoski
# spitkoski@gmail.com
#
# This code uses pygame, available here for download:
# http://www.lfd.uci.edu/~gohlke/pythonlibs/#pygame
# NOTE: the above download is for python 2.7. If you have a different version,
# download the appropriate library at http://www.pygame.org/download.shtml

import sys, pygame
from gameobjects.Player import Player

class Main:
    """The Main Class - This class handles the main 
    initialization and creating of the Game."""
    
    def __init__(self, width=640,height=480):
        
        """Initialize PyGame"""
        pygame.init()
        """Set the window Size"""
        self.width = width
        self.height = height
        """Create the Screen"""
        self.screen = pygame.display.set_mode((self.width, self.height))
        
        self.background = pygame.Surface(self.screen.get_size())
        self.background = self.background.convert()
        self.background.fill((0, 250, 250))

    def MainLoop(self):
        """This is the Main Loop of the Game"""
        
        """Load All of our Sprites"""
        self.LoadSprites()
        
        while 1:
            for event in pygame.event.get():
                if event.type == pygame.QUIT:
                    return
                elif event.type == pygame.KEYDOWN and event.key == pygame.K_ESCAPE:
                    return
        
            self.screen.blit(self.background, (0, 0))
            
            self.player1_sprites.update()
            self.player1_sprites.draw(self.screen)
            
            self.player2_sprites.update()
            self.player2_sprites.draw(self.screen)
            
            pygame.display.flip()

    def LoadSprites(self):
        """Load the sprites that we need"""
        self.player1 = Player(200, 111)
        self.player1_sprites = pygame.sprite.RenderPlain((self.player1))
        self.player2 = Player(10, 10)
        self.player2_sprites = pygame.sprite.RenderPlain((self.player2))


if __name__ == "__main__":
    MainWindow = Main()
    MainWindow.MainLoop()
