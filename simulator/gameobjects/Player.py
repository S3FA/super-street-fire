import sys, os, pygame

class Player(pygame.sprite.Sprite):
    """This is our player that will fight the other player"""
    
    def __init__(self, _x, _y):
        pygame.sprite.Sprite.__init__(self) #call Sprite intializer
        self.image, self.rect = load_image('../images/player.png')
        screen = pygame.display.get_surface()
        self.area = screen.get_rect()
        self.rect.topleft = _x, _y
        

    def update(self):
        pass
        #self.image = pygame.transform.flip(self.image, 1, 0)
        
            
def load_image(name):
    fullname = os.path.join('data', name)
    try:
        image = pygame.image.load(fullname)
    except pygame.error, message:
        print 'Cannot load image:', name
        raise SystemExit, message
    image = image.convert()
    
    return image, image.get_rect()

if __name__ == "__main__":
    MainWindow = Main()
    MainWindow.MainLoop()