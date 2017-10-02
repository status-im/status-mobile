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

////////////////////////////////////////////////////////////////////
#pragma mark - startNode
//////////////////////////////////////////////////////////////////// startNode
RCT_EXPORT_METHOD(startNode:(NSString *)configString) {
#if DEBUG
    NSLog(@"StartNode() method called");
#endif
    NSFileManager *fileManager = [NSFileManager defaultManager];
    NSError *error = nil;
    NSURL *rootUrl =[[fileManager
                      URLsForDirectory:NSDocumentDirectory inDomains:NSUserDomainMask]
                     lastObject];
    NSURL *testnetFolderName = [rootUrl URLByAppendingPathComponent:@"ethereum/testnet"];
    
    if (![fileManager fileExistsAtPath:testnetFolderName.path])
        [fileManager createDirectoryAtPath:testnetFolderName.path withIntermediateDirectories:YES attributes:nil error:&error];
    
    NSURL *flagFolderUrl = [rootUrl URLByAppendingPathComponent:@"ropsten_flag"];
    
    if(![fileManager fileExistsAtPath:flagFolderUrl.path]){
        NSLog(@"remove lightchaindata");
        NSURL *lightChainData = [testnetFolderName URLByAppendingPathComponent:@"StatusIM/lightchaindata"];
        if([fileManager fileExistsAtPath:lightChainData.path]) {
            [fileManager removeItemAtPath:lightChainData.path
                                    error:nil];
        }
        [fileManager createDirectoryAtPath:flagFolderUrl.path
               withIntermediateDirectories:NO
                                attributes:nil
                                     error:&error];
    }
    
    NSLog(@"after remove lightchaindata");
    
    NSURL *oldKeystoreUrl = [testnetFolderName URLByAppendingPathComponent:@"keystore"];
    NSURL *newKeystoreUrl = [rootUrl URLByAppendingPathComponent:@"keystore"];
    if([fileManager fileExistsAtPath:oldKeystoreUrl.path]){
        NSLog(@"copy keystore");
        [fileManager copyItemAtPath:oldKeystoreUrl.path toPath:newKeystoreUrl.path error:nil];
        [fileManager removeItemAtPath:oldKeystoreUrl.path error:nil];
    }
    
    NSLog(@"after lightChainData");
    
    NSLog(@"preconfig: %@", configString);
    NSData *configData = [configString dataUsingEncoding:NSUTF8StringEncoding];
    NSDictionary *configJSON = [NSJSONSerialization JSONObjectWithData:configData options:NSJSONReadingMutableContainers error:nil];
    int networkId = [configJSON[@"NetworkId"] integerValue];
    NSString *dataDir = [configJSON objectForKey:@"DataDir"];
    NSString *upstreamURL = [configJSON valueForKeyPath:@"UpstreamConfig.URL"];
    NSString *networkDir = [rootUrl.path stringByAppendingString:dataDir];
#ifdef DEBUG
    int dev = 1;
#else
    int dev = 0;
#endif
    char *configChars = GenerateConfig((char *)[networkDir UTF8String], networkId, dev);
    NSString *config = [NSString stringWithUTF8String: configChars];
    configData = [config dataUsingEncoding:NSUTF8StringEncoding];
    NSDictionary *resultingConfigJson = [NSJSONSerialization JSONObjectWithData:configData options:NSJSONReadingMutableContainers error:nil];
    [resultingConfigJson setValue:newKeystoreUrl.path forKey:@"KeyStoreDir"];
    [resultingConfigJson setValue:[NSNumber numberWithBool:YES] forKey:@"LogEnabled"];
    [resultingConfigJson setValue:@"geth.log" forKey:@"LogFile"];
    [resultingConfigJson setValue:@"DEBUG" forKey:@"LogLevel"];

    if(upstreamURL != nil) {
        [resultingConfigJson setValue:[NSNumber numberWithBool:YES] forKeyPath:@"UpstreamConfig.Enabled"];
        [resultingConfigJson setValue:upstreamURL forKeyPath:@"UpstreamConfig.URL"];
    }
    NSString *resultingConfig = [resultingConfigJson bv_jsonStringWithPrettyPrint:NO];
    NSLog(@"node config %@", resultingConfig);
    NSURL *networkDirUrl = [NSURL fileURLWithPath:networkDir];
    NSURL *logUrl = [networkDirUrl URLByAppendingPathComponent:@"geth.log"];
    if([fileManager fileExistsAtPath:logUrl.path]) {
        [fileManager removeItemAtPath:logUrl.path error:nil];
    }
    
    if(![fileManager fileExistsAtPath:networkDirUrl.path]) {
        [fileManager createDirectoryAtPath:networkDirUrl.path withIntermediateDirectories:YES attributes:nil error:nil];
    }
    
    NSLog(@"logUrlPath %@", logUrl.path);
    if(![fileManager fileExistsAtPath:logUrl.path]) {
        NSMutableDictionary *dict = [[NSMutableDictionary alloc] init];
        [dict setObject:[NSNumber numberWithInt:511] forKey:NSFilePosixPermissions];
        [fileManager createFileAtPath:logUrl.path contents:nil attributes:dict];
    }
#ifndef DEBUG
    [Instabug addFileAttachmentWithURL:logUrl];
#endif
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0),
                   ^(void)
                   {
                       char *res = StartNode((char *) [resultingConfig UTF8String]);
                       NSLog(@"StartNode result %@", [NSString stringWithUTF8String: res]);                   });
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

////////////////////////////////////////////////////////////////////
#pragma mark - StopNode method
//////////////////////////////////////////////////////////////////// StopNode
RCT_EXPORT_METHOD(stopNode) {
#if DEBUG
    NSLog(@"StopNode() method called");
#endif
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0),
                   ^(void)
                   {
                       char *res = StopNode();
                       NSLog(@"StopNode result %@", [NSString stringWithUTF8String: res]);
                   });
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

////////////////////////////////////////////////////////////////////
                                #pragma mark - Notify method
//////////////////////////////////////////////////////////////////// notify
RCT_EXPORT_METHOD(notify:(NSString *)token
                 callback:(RCTResponseSenderBlock)callback) {
   char * result = Notify((char *) [token UTF8String]);
   callback(@[[NSString stringWithUTF8String: result]]);
#if DEBUG
   NSLog(@"Notify() method called");
#endif
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
