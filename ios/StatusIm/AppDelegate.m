/**
 * Copyright (c) 2015-present, Facebook, Inc.
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
#import "TestFairy.h"
#import "RCTLinkingManager.h"
#import <Firebase.h>
#import "RNFirebaseNotifications.h"
#import "RNFirebaseMessaging.h"

@implementation AppDelegate

/* Modified version of RCTDefaultLogFunction that also directs all app logs to TestFairy. */
RCTLogFunction RCTTestFairyLogFunction = ^(
  RCTLogLevel level,
  __unused RCTLogSource source,
  NSString *fileName,
  NSNumber *lineNumber,
  NSString *message
  )
{
  NSString *log = RCTFormatLog([NSDate date], level, fileName, lineNumber, message);
  fprintf(stderr, "%s\n", log.UTF8String);
  fflush(stderr);

  /* Only custom part */
  TFLog(log);

  int aslLevel;
  switch(level) {
  case RCTLogLevelTrace:
    aslLevel = ASL_LEVEL_DEBUG;
    break;
  case RCTLogLevelInfo:
    aslLevel = ASL_LEVEL_NOTICE;
    break;
  case RCTLogLevelWarning:
    aslLevel = ASL_LEVEL_WARNING;
    break;
  case RCTLogLevelError:
    aslLevel = ASL_LEVEL_ERR;
    break;
  case RCTLogLevelFatal:
    aslLevel = ASL_LEVEL_CRIT;
    break;
  }
  asl_log(NULL, NULL, aslLevel, "%s", message.UTF8String);

};

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
  signal(SIGPIPE, SIG_IGN);
  NSURL *jsCodeLocation;

  [FIRApp configure];
  [RNFirebaseNotifications configure];
  [application registerForRemoteNotifications];


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
    RCTSetLogFunction(RCTTestFairyLogFunction);
  }

  jsCodeLocation = [[RCTBundleURLProvider sharedSettings] jsBundleURLForBundleRoot:@"index.ios" fallbackResource:nil];

  RCTRootView *rootView = [[RCTRootView alloc] initWithBundleURL:jsCodeLocation
                                                      moduleName:@"StatusIm"
                                               initialProperties:nil
                                                   launchOptions:launchOptions];
  rootView.backgroundColor = [[UIColor alloc] initWithRed:1.0f green:1.0f blue:1.0f alpha:1];

  self.window = [[UIWindow alloc] initWithFrame:[UIScreen mainScreen].bounds];
  UIViewController *rootViewController = [UIViewController new];
  rootViewController.view = rootView;
  self.window.rootViewController = rootViewController;
  [self.window makeKeyAndVisible];
  [RNSplashScreen show];

  return YES;
}

- (void)application:(UIApplication *)application didReceiveLocalNotification:(UILocalNotification *)notification {
  [[RNFirebaseNotifications instance] didReceiveLocalNotification:notification];
}

- (void)application:(UIApplication *)application didReceiveRemoteNotification:(nonnull NSDictionary *)userInfo
                                                       fetchCompletionHandler:(nonnull void (^)(UIBackgroundFetchResult))completionHandler{
  [[RNFirebaseNotifications instance] didReceiveRemoteNotification:userInfo fetchCompletionHandler:completionHandler];
}

- (void)application:(UIApplication *)application didRegisterUserNotificationSettings:(UIUserNotificationSettings *)notificationSettings {
  [[RNFirebaseMessaging instance] didRegisterUserNotificationSettings:notificationSettings];
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

@end
