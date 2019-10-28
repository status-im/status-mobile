/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

#import "AppDelegate.h"

#import <asl.h>
#import "ReactNativeConfig.h"
#import "React/RCTLog.h"
#import "RCTBundleURLProvider.h"
#import "RCTRootView.h"
#import "RNSplashScreen.h"
#import "RCTLinkingManager.h"

@implementation AppDelegate 
{
    UIView *_blankView;
}

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
  signal(SIGPIPE, SIG_IGN);
  NSURL *jsCodeLocation;

  /* Set logging level from React Native */
  NSString *logLevel = [ReactNativeConfig envFor:@"LOG_LEVEL"];
  if([logLevel isEqualToString:@"error"]){
    RCTSetLogThreshold(RCTLogLevelError);
  }
  else if([logLevel isEqualToString:@"warn"]){
    RCTSetLogThreshold(RCTLogLevelWarning);
  }
  else if([logLevel isEqualToString:@"info"]){
    RCTSetLogThreshold(RCTLogLevelInfo);
  }
  else if([logLevel isEqualToString:@"debug"]){
    RCTSetLogThreshold(RCTLogLevelTrace);
  }

  NSDictionary *appDefaults = [NSDictionary
      dictionaryWithObject:[NSNumber numberWithBool:NO] forKey:@"BLANK_PREVIEW"];
  [[NSUserDefaults standardUserDefaults] registerDefaults:appDefaults];

  RCTBridge *bridge = [[RCTBridge alloc] initWithDelegate:self launchOptions:launchOptions];
  RCTRootView *rootView = [[RCTRootView alloc] initWithBridge:bridge
                                                      moduleName:@"StatusIm"
                                               initialProperties:nil];
  rootView.backgroundColor = [[UIColor alloc] initWithRed:1.0f green:1.0f blue:1.0f alpha:1];

  self.window = [[UIWindow alloc] initWithFrame:[UIScreen mainScreen].bounds];
  _blankView = [[UIView alloc]initWithFrame:self.window.frame];
  _blankView.backgroundColor = [UIColor whiteColor];
  _blankView.alpha = 0;

  UIViewController *rootViewController = [UIViewController new];
  rootViewController.view = rootView;
  self.window.rootViewController = rootViewController;
  [self.window makeKeyAndVisible];
  [RNSplashScreen show];

  return YES;
}

- (BOOL)application:(UIApplication *)application
            openURL:(NSURL *)url
            options:(NSDictionary<UIApplicationOpenURLOptionsKey,id> *)options
{
  return [RCTLinkingManager application:application openURL:url options:options];
}


- (BOOL)application:(UIApplication *)application continueUserActivity:(NSUserActivity *)userActivity
 restorationHandler:(void (^)(NSArray * _Nullable))restorationHandler
{
  return [RCTLinkingManager application:application
                   continueUserActivity:userActivity
                     restorationHandler:restorationHandler];
}

- (NSURL *)sourceURLForBridge:(RCTBridge *)bridge
{
#if DEBUG
  return [[RCTBundleURLProvider sharedSettings] jsBundleURLForBundleRoot:@"index" fallbackResource:nil];
#else
  return [[NSBundle mainBundle] URLForResource:@"main" withExtension:@"jsbundle"];
#endif
}

- (void)applicationWillResignActive:(UIApplication *)application {
  if ([[NSUserDefaults standardUserDefaults] boolForKey:@"BLANK_PREVIEW"]) {
    [self.window addSubview:_blankView];
    [self.window bringSubviewToFront:_blankView];

    [UIView animateWithDuration:0.5 animations:^{
      _blankView.alpha = 1;
    }];
  } 
}

- (void)applicationDidBecomeActive:(UIApplication *)application {
  if ([[NSUserDefaults standardUserDefaults] boolForKey:@"BLANK_PREVIEW"]) {
    [UIView animateWithDuration:0.5 animations:^{
      _blankView.alpha = 0;
    } completion:^(BOOL finished) {
      [_blankView removeFromSuperview];
    }];
  }
}

@end
