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
#import "RNCConfig.h"
#import "React/RCTLog.h"
#import "RCTBundleURLProvider.h"
#import "RNSplashScreen.h"
#import "RCTLinkingManager.h"

#import <React/RCTBridge.h>
#import <React/RCTBundleURLProvider.h>
#import <React/RCTHTTPRequestHandler.h>

#import <UserNotifications/UserNotifications.h>
#import <RNCPushNotificationIOS.h>

#import <SDWebImage/SDWebImageDownloaderConfig.h>
#import <SDWebImage/SDWebImageDownloaderOperation.h>

#import <Security/Security.h>
// adding this for v0.68.x
#import <React/RCTAppSetupUtils.h>
#if RCT_NEW_ARCH_ENABLED
#import <React/CoreModulesPlugins.h>
#import <React/RCTCxxBridgeDelegate.h>
#import <React/RCTFabricSurfaceHostingProxyRootView.h>
#import <React/RCTSurfacePresenter.h>
#import <React/RCTSurfacePresenterBridgeAdapter.h>
#import <ReactCommon/RCTTurboModuleManager.h>
#import <react/config/ReactNativeConfig.h>
@interface AppDelegate () <RCTCxxBridgeDelegate, RCTTurboModuleManagerDelegate> {
  RCTTurboModuleManager *_turboModuleManager;
  RCTSurfacePresenterBridgeAdapter *_bridgeAdapter;
  std::shared_ptr<const facebook::react::ReactNativeConfig> _reactNativeConfig;
  facebook::react::ContextContainer::Shared _contextContainer;
  }
@end
#endif



//TODO: properly import the framework
extern "C" NSString* StatusgoImageServerTLSCert();

@interface StatusDownloaderOperation : SDWebImageDownloaderOperation
  + (void)URLSession:(NSURLSession *)session task:(NSURLSessionTask *)task didReceiveChallenge:(NSURLAuthenticationChallenge *)challenge completionHandler:(void (^)(NSURLSessionAuthChallengeDisposition disposition, NSURLCredential *credential))completionHandler;
@end

 /*
#if DEBUG
#import <FlipperKit/FlipperClient.h>
#import <FlipperKitLayoutPlugin/FlipperKitLayoutPlugin.h>
#import <FlipperKitUserDefaultsPlugin/FKUserDefaultsPlugin.h>
#import <FlipperKitNetworkPlugin/FlipperKitNetworkPlugin.h>
#import <SKIOSNetworkPlugin/SKIOSNetworkAdapter.h>
#import <FlipperKitReactPlugin/FlipperKitReactPlugin.h>
 */
 /*
static void InitializeFlipper(UIApplication *application) {
  FlipperClient *client = [FlipperClient sharedClient];
  SKDescriptorMapper *layoutDescriptorMapper = [[SKDescriptorMapper alloc] initWithDefaults];
  [client addPlugin:[[FlipperKitLayoutPlugin alloc] initWithRootNode:application withDescriptorMapper:layoutDescriptorMapper]];
  [client addPlugin:[[FKUserDefaultsPlugin alloc] initWithSuiteName:nil]];
  [client addPlugin:[FlipperKitReactPlugin new]];
  [client addPlugin:[[FlipperKitNetworkPlugin alloc] initWithNetworkAdapter:[SKIOSNetworkAdapter new]]];
  [client start];
       }
#endif
  */

@implementation AppDelegate
{
    UIView *_blankView;
}

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
  RCTAppSetupPrepareApp(application);

  RCTBridge *bridge = [[RCTBridge alloc] initWithDelegate:self launchOptions:launchOptions];
  [ReactNativeNavigation bootstrapWithBridge:bridge];

  #if RCT_NEW_ARCH_ENABLED
      _contextContainer = std::make_shared<facebook::react::ContextContainer const>();
      _reactNativeConfig = std::make_shared<facebook::react::EmptyReactNativeConfig const>();
      _contextContainer->insert("ReactNativeConfig", _reactNativeConfig);
      _bridgeAdapter = [[RCTSurfacePresenterBridgeAdapter alloc] initWithBridge:bridge contextContainer:_contextContainer];
      bridge.surfacePresenter = _bridgeAdapter.surfacePresenter;
  #endif

  #if DEBUG
    //InitializeFlipper(application);
  #endif
  signal(SIGPIPE, SIG_IGN);
  NSURL *jsCodeLocation;

  /* Set logging level from React Native */
  NSString *logLevel = [RNCConfig envFor:@"LOG_LEVEL"];
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

  [RNSplashScreen show];

  UNUserNotificationCenter *center = [UNUserNotificationCenter currentNotificationCenter];
  center.delegate = self;

  SDWebImageDownloaderConfig.defaultDownloaderConfig.operationClass = [StatusDownloaderOperation class];

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

- (NSArray<id<RCTBridgeModule>> *)extraModulesForBridge:(RCTBridge *)bridge {
  return [ReactNativeNavigation extraModulesForBridge:bridge];
}

- (NSURL *)sourceURLForBridge:(RCTBridge *)bridge
{
#if DEBUG
  return [[RCTBundleURLProvider sharedSettings] jsBundleURLForBundleRoot:@"index"];
#else
  return [[NSBundle mainBundle] URLForResource:@"main" withExtension:@"jsbundle"];
#endif
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
