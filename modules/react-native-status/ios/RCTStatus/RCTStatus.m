#import "RCTStatus.h"
#import "RCTBridge.h"
#import "RCTEventDispatcher.h"
#import <Statusgo/Statusgo.h>

static bool isStatusInitialized;
static RCTBridge *bridge;
@implementation Status{
}

-(RCTBridge *)bridge
{
    return bridge;
}

-(void)setBridge:(RCTBridge *)newBridge
{
    bridge = newBridge;
}

RCT_EXPORT_MODULE();

////////////////////////////////////////////////////////////////////
#pragma mark - Jails functions
//////////////////////////////////////////////////////////////////// initJail
RCT_EXPORT_METHOD(initJail: (NSString *) js
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"InitJail() method called");
#endif
    InitJail((char *) [js UTF8String]);
    callback(@[[NSNull null]]);
}

//////////////////////////////////////////////////////////////////// parseJail
RCT_EXPORT_METHOD(parseJail:(NSString *)chatId
                  js:(NSString *)js
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"ParseJail() method called");
#endif
    char * result = Parse((char *) [chatId UTF8String], (char *) [js UTF8String]);
    callback(@[[NSString stringWithUTF8String: result]]);
}

//////////////////////////////////////////////////////////////////// callJail
RCT_EXPORT_METHOD(callJail:(NSString *)chatId
                  path:(NSString *)path
                  params:(NSString *)params
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"CallJail() method called");
#endif
    dispatch_async( dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        char * result = Call((char *) [chatId UTF8String], (char *) [path UTF8String], (char *) [params UTF8String]);
        dispatch_async( dispatch_get_main_queue(), ^{
            callback(@[[NSString stringWithUTF8String: result]]);
        });
    });
}


const int STATE_ACTIVE = 0;
const int STATE_LOCKED_WITH_ACTIVE_APP = 1;
const int STATE_BACKGROUND = 2;
const int STATE_LOCKED_WITH_INACTIVE_APP = 3;
int wozniakConstant = STATE_ACTIVE;


static void displayStatusChanged(CFNotificationCenterRef center, void *observer, CFStringRef name, const void *object, CFDictionaryRef userInfo)
{
    // the "com.apple.springboard.lockcomplete" notification will always come after the "com.apple.springboard.lockstate" notification
    CFStringRef nameCFString = (CFStringRef)name;
    NSString *lockState = (__bridge NSString*)nameCFString;
    NSLog(@"Darwin notification NAME = %@",name);
    
    NSString* sm = [NSString stringWithFormat:@"%i", wozniakConstant];
    NSString *s1 = [NSString stringWithFormat:@"%@ %@", @"LOCK MAGIC", sm];
    NSLog(s1);
    if([lockState isEqualToString:@"com.apple.springboard.lockcomplete"])
    {
        NSLog(@"DEVICE LOCKED");
        // User locks phone when application is active
        if(wozniakConstant == STATE_ACTIVE){
            wozniakConstant = STATE_LOCKED_WITH_ACTIVE_APP;
            StopNodeRPCServer();
        }
        
        // Here lockcomplete event comes when app is unlocked
        // because it couldn't come when locking happened
        // as application was not active (it could not handle callback)
        if (wozniakConstant == STATE_LOCKED_WITH_INACTIVE_APP) {
            wozniakConstant = STATE_ACTIVE;
            StopNodeRPCServer();
            StartNodeRPCServer();
        }
    }
    else
    {
        NSLog(@"LOCK STATUS CHANGED");
        NSString *s = [NSString stringWithFormat:@"%@ %@", @"LOCK", lockState];
        NSLog(s);
        
        // if lockstate happens before lockcomplete it means
        // that phone was locked when application was not active
        if(wozniakConstant == STATE_ACTIVE){
            wozniakConstant = STATE_LOCKED_WITH_INACTIVE_APP;
        }
        
        if(wozniakConstant == STATE_BACKGROUND){
            wozniakConstant = STATE_ACTIVE;
            StartNodeRPCServer();
        }
        
        // one more lockstate event comes along with lockcomplete
        // when phone is locked with active application
        if(wozniakConstant == STATE_LOCKED_WITH_ACTIVE_APP){
            wozniakConstant = STATE_BACKGROUND;
        }
        
    }
}

////////////////////////////////////////////////////////////////////
#pragma mark - startNode
//////////////////////////////////////////////////////////////////// startNode
RCT_EXPORT_METHOD(startNode:(RCTResponseSenderBlock)onResultCallback) {
#if DEBUG
    NSLog(@"StartNode() method called");
#endif
    if (!isStatusInitialized){
        isStatusInitialized = true;
        
        NSError *error = nil;
        NSURL *folderName =[[[[NSFileManager defaultManager]
                              URLsForDirectory:NSDocumentDirectory inDomains:NSUserDomainMask]
                             lastObject]
                            URLByAppendingPathComponent:@"ethereum/testnet"];
        
        if (![[NSFileManager defaultManager] fileExistsAtPath:folderName.path])
            [[NSFileManager defaultManager] createDirectoryAtPath:folderName.path withIntermediateDirectories:NO attributes:nil error:&error];
        NSURL *flagFolderUrl = [[[[NSFileManager defaultManager]
                                URLsForDirectory:NSDocumentDirectory inDomains:NSUserDomainMask]
                               lastObject]
                              URLByAppendingPathComponent:@"ropsten_flag"];
        
        if(![[NSFileManager defaultManager] fileExistsAtPath:flagFolderUrl.path]){
            NSURL *lightChainData = [folderName URLByAppendingPathComponent:@"StatusIM/lightchaindata"];
            [[NSFileManager defaultManager] removeItemAtPath:lightChainData.path
                                                       error:nil];
            NSString *content = @"";
            NSData *fileContents = [content dataUsingEncoding:NSUTF8StringEncoding];
            [[NSFileManager defaultManager] createDirectoryAtPath:flagFolderUrl.path
                                      withIntermediateDirectories:NO
                                                       attributes:nil
                                                            error:&error];
        }
        
        if (error){
            NSLog(@"error %@", error);
        }else
            NSLog(@"folderName: %@", folderName);

        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0),
                       ^(void) {
            char *config = GenerateConfig([folderName.path UTF8String], 3);
            StartNode(config);
        });
        onResultCallback(@[[NSNull null]]);
        //Screen lock notifications
        CFNotificationCenterAddObserver(CFNotificationCenterGetDarwinNotifyCenter(), //center
                                        NULL, // observer
                                        displayStatusChanged, // callback
                                        CFSTR("com.apple.springboard.lockcomplete"), // event name
                                        NULL, // object
                                        CFNotificationSuspensionBehaviorDeliverImmediately);
        
        CFNotificationCenterAddObserver(CFNotificationCenterGetDarwinNotifyCenter(), //center
                                        NULL, // observer
                                        displayStatusChanged, // callback
                                        CFSTR("com.apple.springboard.lockstate"), // event name
                                        NULL, // object
                                        CFNotificationSuspensionBehaviorDeliverImmediately);
        return;
    }
}

////////////////////////////////////////////////////////////////////
#pragma mark - StartNodeRPCServer method
//////////////////////////////////////////////////////////////////// createAccount
RCT_EXPORT_METHOD(startNodeRPCServer) {
#if DEBUG
    NSLog(@"StartNodeRPCServer() method called");
#endif
    StartNodeRPCServer();
}

////////////////////////////////////////////////////////////////////
#pragma mark - StopNodeRPCServer method
//////////////////////////////////////////////////////////////////// createAccount
RCT_EXPORT_METHOD(stopNodeRPCServer) {
#if DEBUG
    NSLog(@"StopNodeRPCServer() method called");
#endif
    StopNodeRPCServer();
}

RCT_EXPORT_METHOD(stopNode:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"stopNode() method called");
#endif
    // TODO: stop node
    
    callback(@[[NSNull null]]);
}

////////////////////////////////////////////////////////////////////
#pragma mark - Accounts method
//////////////////////////////////////////////////////////////////// createAccount
RCT_EXPORT_METHOD(createAccount:(NSString *)password
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"CreateAccount() method called");
#endif
    char * result = CreateAccount((char *) [password UTF8String]);
    callback(@[[NSString stringWithUTF8String: result]]);
}

//////////////////////////////////////////////////////////////////// recoverAccount
RCT_EXPORT_METHOD(recoverAccount:(NSString *)passphrase
                  password:(NSString *)password
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"RecoverAccount() method called");
#endif
    char * result = RecoverAccount((char *) [password UTF8String], (char *) [passphrase UTF8String]);
    callback(@[[NSString stringWithUTF8String: result]]);
}

//////////////////////////////////////////////////////////////////// login
RCT_EXPORT_METHOD(login:(NSString *)address
                  password:(NSString *)password
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"Login() method called");
#endif
    char * result = Login((char *) [address UTF8String], (char *) [password UTF8String]);
    callback(@[[NSString stringWithUTF8String: result]]);
}

////////////////////////////////////////////////////////////////////
#pragma mark - Complete Transaction
//////////////////////////////////////////////////////////////////// completeTransaction
RCT_EXPORT_METHOD(completeTransaction:(NSString *)hash
                  password:(NSString *)password
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"CompleteTransaction() method called");
#endif
    char * result = CompleteTransaction((char *) [hash UTF8String], (char *) [password UTF8String]);
    callback(@[[NSString stringWithUTF8String: result]]);
}

////////////////////////////////////////////////////////////////////
#pragma mark - Discard Transaction
//////////////////////////////////////////////////////////////////// completeTransaction
RCT_EXPORT_METHOD(discardTransaction:(NSString *)id) {
#if DEBUG
    NSLog(@"DiscardTransaction() method called");
#endif
    DiscardTransaction((char *) [id UTF8String]);
}

////////////////////////////////////////////////////////////////////
#pragma mark - only android methods
////////////////////////////////////////////////////////////////////
RCT_EXPORT_METHOD(setAdjustResize) {
#if DEBUG
    NSLog(@"setAdjustResize() works only on Android");
#endif
}

RCT_EXPORT_METHOD(setAdjustPan) {
#if DEBUG
    NSLog(@"setAdjustPan() works only on Android");
#endif
}

RCT_EXPORT_METHOD(setSoftInputMode: (NSInteger) i) {
#if DEBUG
    NSLog(@"setSoftInputMode() works only on Android");
#endif
}

RCT_EXPORT_METHOD(clearCookies) {
    NSHTTPCookie *cookie;
    NSHTTPCookieStorage *storage = [NSHTTPCookieStorage sharedHTTPCookieStorage];
    for (cookie in [storage cookies]) {
        [storage deleteCookie:cookie];
    }
}

RCT_EXPORT_METHOD(clearStorageAPIs) {
    [[NSURLCache sharedURLCache] removeAllCachedResponses];

    NSString *path = [NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES) lastObject];
    NSArray *array = [[NSFileManager defaultManager] contentsOfDirectoryAtPath:path error:nil];
    for (NSString *string in array) {
        NSLog(@"Removing %@", [path stringByAppendingPathComponent:string]);
    if ([[string pathExtension] isEqualToString:@"localstorage"])
        [[NSFileManager defaultManager] removeItemAtPath:[path stringByAppendingPathComponent:string] error:nil];
    }
}

+ (void)signalEvent:(const char *) signal
{
    if(!signal){
#if DEBUG
    NSLog(@"SignalEvent nil");
#endif
        return;
    }
    
    NSString *sig = [NSString stringWithUTF8String:signal];
#if DEBUG
    NSLog(@"SignalEvent");
    NSLog(sig);
#endif
    [bridge.eventDispatcher sendAppEventWithName:@"gethEvent"
                                            body:@{@"jsonEvent": sig}];
    
    return;
}

@end
