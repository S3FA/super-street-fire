//
//  ViewController.m
//  SuperStreetFireIOSGUI
//
//  Created by beowulf on 2013-04-23.
//  Copyright (c) 2013 beowulf. All rights reserved.
//

#import "ViewController.h"

@interface ViewController ()
@property (weak, nonatomic) IBOutlet UIButton* button;
@property (strong, nonatomic) IBOutlet ArenaView* arenaView;
@end

@implementation ViewController

- (void)dealloc {
    self.arenaView = nil;
}

- (void)viewDidLoad {
    [super viewDidLoad];

}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];

}

- (IBAction)sendColourChange:(id)sender {
    [self.arenaView changeColours];
}
@end
