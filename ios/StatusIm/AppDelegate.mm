/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

#import "AppDelegate.h"
#import <ReactNativeNavigation/ReactNativeNavigation.h>

#import <asl.h>
#import "React/RCTLog.h"
#import "RCTBundleURLProvider.h"
#import "RNSplashScreen.h"
#import "RCTLinkingManager.h"

#import <React/RCTBundleURLProvider.h>
#import <React/RCTHTTPRequestHandler.h>
#import <UserNotifications/UserNotifications.h>
#import <RNCPushNotificationIOS.h>

#import <SDWebImage/SDWebImageDownloaderConfig.h>
#import <SDWebImage/SDWebImageDownloaderOperation.h>

#import <Security/Security.h>


//TODO: properly import the framework
extern "C" NSString* StatusgoImageServerTLSCert();

@interface StatusDownloaderOperation : SDWebImageDownloaderOperation
  + (void)URLSession:(NSURLSession *)session task:(NSURLSessionTask *)task didReceiveChallenge:(NSURLAuthenticationChallenge *)challenge completionHandler:(void (^)(NSURLSessionAuthChallengeDisposition disposition, NSURLCredential *credential))completionHandler;
@end

@implementation AppDelegate
{
    UIView *_blankView;
}

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
  [ReactNativeNavigation bootstrapWithDelegate:self launchOptions:launchOptions];

  #if DEBUG
    //InitializeFlipper(application);
  #endif
  signal(SIGPIPE, SIG_IGN);
  NSURL *jsCodeLocation;

/* Set logging level from React Native */
// Todo : uncomment below section when project has been manually linked properly and below error has been fixed
// Undefined symbols for architecture x86_64:
//   "_OBJC_CLASS_$_RNCConfig", referenced from:
//       objc-class-ref in AppDelegate.o
// ld: symbol(s) not found for architecture x86_64
// clang: error: linker command failed with exit code 1 (use -v to see invocation)
// add #import "RNCConfig.h" at the top
//
//   NSString *logLevel = [RNCConfig envFor:@"LOG_LEVEL"];
//   if([logLevel isEqualToString:@"error"]){
//     RCTSetLogThreshold(RCTLogLevelError);
//   }
//   else if([logLevel isEqualToString:@"warn"]){
//     RCTSetLogThreshold(RCTLogLevelWarning);
//   }
//   else if([logLevel isEqualToString:@"info"]){
//     RCTSetLogThreshold(RCTLogLevelInfo);
//   }
//   else if([logLevel isEqualToString:@"debug"]){
//     RCTSetLogThreshold(RCTLogLevelTrace);
//   }

  NSDictionary *appDefaults = [NSDictionary
      dictionaryWithObject:[NSNumber numberWithBool:NO] forKey:@"BLANK_PREVIEW"];
  [[NSUserDefaults standardUserDefaults] registerDefaults:appDefaults];

  [RNSplashScreen show];


  UNUserNotificationCenter *center = [UNUserNotificationCenter currentNotificationCenter];
  center.delegate = self;

  SDWebImageDownloaderConfig.defaultDownloaderConfig.operationClass = [StatusDownloaderOperation class];

  return YES;
}

- (NSURL *)sourceURLForBridge:(RCTBridge *)bridge
{
#if DEBUG
  return [[RCTBundleURLProvider sharedSettings] jsBundleURLForBundleRoot:@"index"];
#else
  return [[NSBundle mainBundle] URLForResource:@"main" withExtension:@"jsbundle"];
#endif
}

/// This method controls whether the `concurrentRoot`feature of React18 is turned on or off.
///
/// @see: https://reactjs.org/blog/2022/03/29/react-v18.html
/// @note: This requires to be rendering on Fabric (i.e. on the New Architecture).
/// @return: `true` if the `concurrentRoot` feature is enabled. Otherwise, it returns `false`.
- (BOOL)concurrentRootEnabled
{
  return true;
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


#if RCT_NEW_ARCH_ENABLED
#pragma mark - RCTCxxBridgeDelegate
- (std::unique_ptr<facebook::react::JSExecutorFactory>)jsExecutorFactoryForBridge:(RCTBridge *)bridge
{
  _turboModuleManager = [[RCTTurboModuleManager alloc] initWithBridge:bridge
                                                             delegate:self
                                                            jsInvoker:bridge.jsCallInvoker];
  return RCTAppSetupDefaultJsExecutorFactory(bridge, _turboModuleManager);
}
#pragma mark RCTTurboModuleManagerDelegate
- (Class)getModuleClassFromName:(const char *)name
{
  return RCTCoreModulesClassProvider(name);
}
- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:(const std::string &)name
                                                      jsInvoker:(std::shared_ptr<facebook::react::CallInvoker>)jsInvoker
{
  return nullptr;
}
- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:(const std::string &)name
                                                     initParams:
                                                         (const facebook::react::ObjCTurboModule::InitParams &)params
{
  return nullptr;
}
- (id<RCTTurboModule>)getModuleInstanceFromClass:(Class)moduleClass
{
  return RCTAppSetupDefaultModuleFromClass(moduleClass);
}
#endif


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

// Required to register for notifications
- (void)application:(UIApplication *)application didRegisterUserNotificationSettings:(UIUserNotificationSettings *)notificationSettings
{
 [RNCPushNotificationIOS didRegisterUserNotificationSettings:notificationSettings];
}
// Required for the register event.
- (void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken
{
 [RNCPushNotificationIOS didRegisterForRemoteNotificationsWithDeviceToken:deviceToken];
}
// Required for the registrationError event.
- (void)application:(UIApplication *)application didFailToRegisterForRemoteNotificationsWithError:(NSError *)error
{
 [RNCPushNotificationIOS didFailToRegisterForRemoteNotificationsWithError:error];
}
// IOS 10+ Required for localNotification event
- (void)userNotificationCenter:(UNUserNotificationCenter *)center
didReceiveNotificationResponse:(UNNotificationResponse *)response
         withCompletionHandler:(void (^)(void))completionHandler
{
  [RNCPushNotificationIOS didReceiveNotificationResponse:response];
  completionHandler();
}
// IOS 4-10 Required for the localNotification event.
- (void)application:(UIApplication *)application didReceiveLocalNotification:(UILocalNotification *)notification
{
 [RNCPushNotificationIOS didReceiveLocalNotification:notification];
}
// Manage notifications while app is in the foreground
- (void)userNotificationCenter:(UNUserNotificationCenter *)center willPresentNotification:(UNNotification *)notification withCompletionHandler:(void (^)(UNNotificationPresentationOptions options))completionHandler
{
  NSDictionary *userInfo = notification.request.content.userInfo;

  NSString *notificationType = userInfo[@"notificationType"]; // check your notification type
  if (![notificationType  isEqual: @"local-notification"]) { // we silence all notifications which are not local
    completionHandler(UNNotificationPresentationOptionNone);
    return;
  }

  completionHandler(UNAuthorizationOptionSound | UNAuthorizationOptionAlert | UNAuthorizationOptionBadge);
}

@end

@implementation StatusDownloaderOperation

+ (void)URLSession:(NSURLSession *)session task:(NSURLSessionTask *)task didReceiveChallenge:(NSURLAuthenticationChallenge *)challenge completionHandler:(void (^)(NSURLSessionAuthChallengeDisposition disposition, NSURLCredential *credential))completionHandler {
  NSURLSessionAuthChallengeDisposition disposition = NSURLSessionAuthChallengeCancelAuthenticationChallenge;
  __block NSURLCredential *credential = nil;

  NSString *pemCert = StatusgoImageServerTLSCert();
  pemCert = [pemCert stringByReplacingOccurrencesOfString:@"-----BEGIN CERTIFICATE-----\n" withString:@""];
  pemCert = [pemCert stringByReplacingOccurrencesOfString:@"\n-----END CERTIFICATE-----" withString:@""];
  NSData *derCert = [[NSData alloc] initWithBase64EncodedString:pemCert options:NSDataBase64DecodingIgnoreUnknownCharacters];
  SecCertificateRef certRef = SecCertificateCreateWithData(NULL, (__bridge_retained CFDataRef) derCert);
  CFArrayRef certArrayRef = CFArrayCreate(NULL, (const void **)&(certRef), 1, NULL);
  SecTrustSetAnchorCertificates(challenge.protectionSpace.serverTrust, certArrayRef);

  SecTrustResultType trustResult;
  SecTrustEvaluate(challenge.protectionSpace.serverTrust, &trustResult);

  if ((trustResult == kSecTrustResultProceed) || (trustResult == kSecTrustResultUnspecified)) {
    disposition = NSURLSessionAuthChallengeUseCredential;
    credential = [NSURLCredential credentialForTrust:challenge.protectionSpace.serverTrust];
  }

  if (completionHandler) {
    completionHandler(disposition, credential);
  }
}

- (void)URLSession:(NSURLSession *)session task:(NSURLSessionTask *)task didReceiveChallenge:(NSURLAuthenticationChallenge *)challenge completionHandler:(void (^)(NSURLSessionAuthChallengeDisposition disposition, NSURLCredential *credential))completionHandler {
  if ([challenge.protectionSpace.authenticationMethod isEqualToString:NSURLAuthenticationMethodServerTrust] &&
      [challenge.protectionSpace.host isEqualToString:@"localhost"]) {
    [StatusDownloaderOperation URLSession:session task:task didReceiveChallenge:challenge completionHandler:completionHandler];
  } else {
    [super URLSession:session task:task didReceiveChallenge:challenge completionHandler:completionHandler];
  }
}

@end

@implementation RCTHTTPRequestHandler (SelfSigned)

- (void)URLSession:(NSURLSession *)session task:(NSURLSessionTask *)task didReceiveChallenge:(NSURLAuthenticationChallenge *)challenge completionHandler:(void (^)(NSURLSessionAuthChallengeDisposition disposition, NSURLCredential *credential))completionHandler {
  if ([challenge.protectionSpace.authenticationMethod isEqualToString:NSURLAuthenticationMethodServerTrust] &&
      [challenge.protectionSpace.host isEqualToString:@"localhost"]) {
    [StatusDownloaderOperation URLSession:session task:task didReceiveChallenge:challenge completionHandler:completionHandler];
  } else {
    if (completionHandler) {
      completionHandler(NSURLSessionAuthChallengePerformDefaultHandling, [NSURLCredential credentialForTrust:challenge.protectionSpace.serverTrust]);
    }
  }
}

@end
