#import "RCTStatus.h"
#import "React/RCTBridge.h"
#import "React/RCTEventDispatcher.h"
#import <Statusgo/Statusgo.h>
@import Instabug;

@interface NSDictionary (BVJSONString)
-(NSString*) bv_jsonStringWithPrettyPrint:(BOOL) prettyPrint;
@end

@implementation NSDictionary (BVJSONString)

-(NSString*) bv_jsonStringWithPrettyPrint:(BOOL) prettyPrint {
    NSError *error;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:self
                                                       options:(NSJSONWritingOptions)    (prettyPrint ? NSJSONWritingPrettyPrinted : 0)
                                                         error:&error];
    
    if (! jsonData) {
        NSLog(@"bv_jsonStringWithPrettyPrint: error: %@", error.localizedDescription);
        return @"{}";
    } else {
        return [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    }
}
@end

@interface NSArray (BVJSONString)
- (NSString *)bv_jsonStringWithPrettyPrint:(BOOL)prettyPrint;
@end

@implementation NSArray (BVJSONString)
-(NSString*) bv_jsonStringWithPrettyPrint:(BOOL) prettyPrint {
    NSError *error;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:self
                                                       options:(NSJSONWritingOptions) (prettyPrint ? NSJSONWritingPrettyPrinted : 0)
                                                         error:&error];
    
    if (! jsonData) {
        NSLog(@"bv_jsonStringWithPrettyPrint: error: %@", error.localizedDescription);
        return @"[]";
    } else {
        return [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    }
}
@end

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
#if DEBUG
        int devCluster = 1;
#else
        int devCluster = 0;
#endif
        char *configChars = GenerateConfig([folderName.path UTF8String], 3, devCluster);
        NSString *upstreamURL = @"https://ropsten.infura.io/z6GCTmjdP3FETEJmMBI4";
        NSString *config = [NSString stringWithUTF8String: configChars];
        NSData *configData = [config dataUsingEncoding:NSUTF8StringEncoding];
        NSDictionary *resultingConfigJson = [NSJSONSerialization JSONObjectWithData:configData options:NSJSONReadingMutableContainers error:nil];
        [resultingConfigJson setValue:[NSNumber numberWithBool:YES] forKey:@"LogEnabled"];
        [resultingConfigJson setValue:@"geth.log" forKey:@"LogFile"];
        [resultingConfigJson setValue:@"DEBUG" forKey:@"LogLevel"];
        [resultingConfigJson setValue:[NSNumber numberWithBool:YES] forKeyPath:@"UpstreamConfig.Enabled"];
        [resultingConfigJson setValue:upstreamURL forKeyPath:@"UpstreamConfig.URL"];
        NSString *resultingConfig = [resultingConfigJson bv_jsonStringWithPrettyPrint:NO];
        NSURL *logUrl = [folderName URLByAppendingPathComponent:@"geth.log"];
        NSFileManager *manager = [NSFileManager defaultManager];
        if([[NSFileManager defaultManager] fileExistsAtPath:logUrl.path]) {
            [manager removeItemAtPath:logUrl.path error:nil];
        }
        
        if(![manager fileExistsAtPath:folderName.path]) {
            [manager createDirectoryAtPath:folderName.path withIntermediateDirectories:YES attributes:nil error:nil];
        }
        
        NSMutableDictionary *dict = [[NSMutableDictionary alloc] init];
        [dict setObject:[NSNumber numberWithInt:511] forKey:NSFilePosixPermissions];
        [manager createFileAtPath:logUrl.path contents:nil attributes:dict];
#ifndef DEBUG
        [Instabug addFileAttachmentWithURL:[folderName URLByAppendingPathComponent:@"geth.log"]];
#endif
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0),
                       ^(void) {
            StartNode((char *) [resultingConfig UTF8String]);
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
#pragma mark - shouldMoveToInternalStorage
//////////////////////////////////////////////////////////////////// shouldMoveToInternalStorage
RCT_EXPORT_METHOD(shouldMoveToInternalStorage:(RCTResponseSenderBlock)onResultCallback) {
    // Android only
    onResultCallback(@[[NSNull null]]);
}

////////////////////////////////////////////////////////////////////
#pragma mark - moveToInternalStorage
//////////////////////////////////////////////////////////////////// moveToInternalStorage
RCT_EXPORT_METHOD(moveToInternalStorage:(RCTResponseSenderBlock)onResultCallback) {
    // Android only
    onResultCallback(@[[NSNull null]]);
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
#pragma mark - Complete Transactions
//////////////////////////////////////////////////////////////////// completeTransactions
RCT_EXPORT_METHOD(completeTransactions:(NSString *)hashes
                  password:(NSString *)password
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"CompleteTransactions() method called");
#endif
    char * result = CompleteTransactions((char *) [hashes UTF8String], (char *) [password UTF8String]);
    callback(@[[NSString stringWithUTF8String: result]]);
}

////////////////////////////////////////////////////////////////////
#pragma mark - Discard Transaction
//////////////////////////////////////////////////////////////////// discardTransaction
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

RCT_EXPORT_METHOD(sendWeb3Request:(NSString *)host
                  password:(NSString *)payload
                  callback:(RCTResponseSenderBlock)callback) {
    dispatch_async( dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        char * result = CallRPC((char *) [payload UTF8String]);
        dispatch_async( dispatch_get_main_queue(), ^{
            callback(@[[NSString stringWithUTF8String: result]]);
        });
    });
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
