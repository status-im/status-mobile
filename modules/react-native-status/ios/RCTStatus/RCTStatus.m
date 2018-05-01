#import "RCTStatus.h"
#import "ReactNativeConfig.h"
#import "React/RCTBridge.h"
#import "React/RCTEventDispatcher.h"
#import <Statusgo/Statusgo.h>

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

+ (BOOL)JSCEnabled
{
    return @"1" == [ReactNativeConfig envFor:@"JSC_ENABLED"];
}

////////////////////////////////////////////////////////////////////
#pragma mark - Jails functions
//////////////////////////////////////////////////////////////////// initJail
RCT_EXPORT_METHOD(initJail: (NSString *) js
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"InitJail() method called");
#endif
    if([Status JSCEnabled]){
        if(_jail == nil) {
            _jail = [Jail new];
        }
        [_jail initJail:js];
    } else {
        InitJail((char *) [js UTF8String]);
    }
    callback(@[[NSNull null]]);
}

//////////////////////////////////////////////////////////////////// parseJail
RCT_EXPORT_METHOD(parseJail:(NSString *)chatId
                  js:(NSString *)js
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"ParseJail() method called");
#endif
    NSString *stringResult;
    if([Status JSCEnabled]){
        if(_jail == nil) {
            _jail = [Jail new];
        }
        NSDictionary *result = [_jail parseJail:chatId withCode:js];
        stringResult = [result bv_jsonStringWithPrettyPrint:NO];
    } else {
        char * result = Parse((char *) [chatId UTF8String], (char *) [js UTF8String]);
        stringResult = [NSString stringWithUTF8String: result];
    }
    
    callback(@[stringResult]);
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
        
        NSString *stringResult;
        if([Status JSCEnabled]){
            if(_jail == nil) {
                _jail = [Jail new];
            }
            NSDictionary *result = [_jail call:chatId path:path params:params];
            stringResult = [result bv_jsonStringWithPrettyPrint:NO];
        } else {
            char * result = Call((char *) [chatId UTF8String], (char *) [path UTF8String], (char *) [params UTF8String]);
            stringResult = [NSString stringWithUTF8String: result];
        }

        dispatch_async( dispatch_get_main_queue(), ^{
            callback(@[stringResult]);
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
    NSString *devCluster = [ReactNativeConfig envFor:@"ETHEREUM_DEV_CLUSTER"];
    NSString *logLevel = [[ReactNativeConfig envFor:@"LOG_LEVEL_STATUS_GO"] uppercaseString];
    int dev = 0;
    if([devCluster isEqualToString:@"1"]){
        dev = 1;
    }
    char *configChars = GenerateConfig((char *)[networkDir UTF8String], networkId, dev);
    NSString *config = [NSString stringWithUTF8String: configChars];
    configData = [config dataUsingEncoding:NSUTF8StringEncoding];
    NSDictionary *resultingConfigJson = [NSJSONSerialization JSONObjectWithData:configData options:NSJSONReadingMutableContainers error:nil];
    NSURL *networkDirUrl = [NSURL fileURLWithPath:networkDir];
    NSURL *logUrl = [networkDirUrl URLByAppendingPathComponent:@"geth.log"];
    [resultingConfigJson setValue:newKeystoreUrl.path forKey:@"KeyStoreDir"];
    [resultingConfigJson setValue:[NSNumber numberWithBool:[logLevel length] != 0] forKey:@"LogEnabled"];
    [resultingConfigJson setValue:logUrl.path forKey:@"LogFile"];
    [resultingConfigJson setValue:([logLevel length] == 0 ? [NSString stringWithUTF8String: "ERROR"] : logLevel) forKey:@"LogLevel"];
    
    [resultingConfigJson setValue:[NSNumber numberWithBool:YES] forKeyPath:@"WhisperConfig.LightClient"];
    if(upstreamURL != nil) {
        [resultingConfigJson setValue:[NSNumber numberWithBool:YES] forKeyPath:@"UpstreamConfig.Enabled"];
        [resultingConfigJson setValue:upstreamURL forKeyPath:@"UpstreamConfig.URL"];
    }
    NSString *resultingConfig = [resultingConfigJson bv_jsonStringWithPrettyPrint:NO];
    NSLog(@"node config %@", resultingConfig);
    
    if(![fileManager fileExistsAtPath:networkDirUrl.path]) {
        [fileManager createDirectoryAtPath:networkDirUrl.path withIntermediateDirectories:YES attributes:nil error:nil];
    }
    
    NSLog(@"logUrlPath %@", logUrl.path);
    if(![fileManager fileExistsAtPath:logUrl.path]) {
        NSMutableDictionary *dict = [[NSMutableDictionary alloc] init];
        [dict setObject:[NSNumber numberWithInt:511] forKey:NSFilePosixPermissions];
        [fileManager createFileAtPath:logUrl.path contents:nil attributes:dict];
    }

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
#pragma mark - NotifyUsers method
//////////////////////////////////////////////////////////////////// notifyUsers
RCT_EXPORT_METHOD(notifyUsers:(NSString *)message
                  payloadJSON:(NSString *)payloadJSON
                  tokensJSON:(NSString *)tokensJSON
                  callback:(RCTResponseSenderBlock)callback) {
    char * result = NotifyUsers((char *) [message UTF8String], (char *) [payloadJSON UTF8String], (char *) [tokensJSON UTF8String]);
    callback(@[[NSString stringWithUTF8String: result]]);
#if DEBUG
    NSLog(@"NotifyUsers() method called");
#endif
}

//////////////////////////////////////////////////////////////////// addPeer
RCT_EXPORT_METHOD(addPeer:(NSString *)enode
                  callback:(RCTResponseSenderBlock)callback) {
  char * result = AddPeer((char *) [enode UTF8String]);
  callback(@[[NSString stringWithUTF8String: result]]);
#if DEBUG
  NSLog(@"AddPeer() method called");
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
    if(_jail != nil) {
        [_jail reset];
    }
    char * result = Login((char *) [address UTF8String], (char *) [password UTF8String]);
    callback(@[[NSString stringWithUTF8String: result]]);
}

////////////////////////////////////////////////////////////////////
#pragma mark - Approve Sign Requests
//////////////////////////////////////////////////////////////////// approveSignRequests
RCT_EXPORT_METHOD(approveSignRequests:(NSString *)hashes
                  password:(NSString *)password
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"ApproveSignRequests() method called");
#endif
    char * result = ApproveSignRequests((char *) [hashes UTF8String], (char *) [password UTF8String]);
    callback(@[[NSString stringWithUTF8String: result]]);
}

////////////////////////////////////////////////////////////////////
#pragma mark - Discard Sign Request
//////////////////////////////////////////////////////////////////// discardSignRequest
RCT_EXPORT_METHOD(discardSignRequest:(NSString *)id) {
#if DEBUG
    NSLog(@"DiscardSignRequest() method called");
#endif
    DiscardSignRequest((char *) [id UTF8String]);
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

RCT_EXPORT_METHOD(sendWeb3Request:(NSString *)payload
                  callback:(RCTResponseSenderBlock)callback) {
    dispatch_async( dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        char * result = CallRPC((char *) [payload UTF8String]);
        dispatch_async( dispatch_get_main_queue(), ^{
            callback(@[[NSString stringWithUTF8String: result]]);
        });
    });
}

RCT_EXPORT_METHOD(sendWeb3PrivateRequest:(NSString *)payload
                  callback:(RCTResponseSenderBlock)callback) {
    dispatch_async( dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        char * result = CallPrivateRPC((char *) [payload UTF8String]);
        dispatch_async( dispatch_get_main_queue(), ^{
            callback(@[[NSString stringWithUTF8String: result]]);
        });
    });
}

RCT_EXPORT_METHOD(closeApplication) {
    exit(0);
}


RCT_EXPORT_METHOD(connectionChange:(NSString *)type
                       isExpensive:(BOOL)isExpensive) {
#if DEBUG
    NSLog(@"ConnectionChange() method called");
#endif
    ConnectionChange((char *) [type UTF8String], isExpensive? 1 : 0);
}

RCT_EXPORT_METHOD(appStateChange:(NSString *)type) {
#if DEBUG
    NSLog(@"AppStateChange() method called");
#endif
    AppStateChange((char *) [type UTF8String]);
}

RCT_EXPORT_METHOD(getDeviceUUID:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"getDeviceUUID() method called");
#endif
    NSString* Identifier = [[[UIDevice currentDevice] identifierForVendor] UUIDString];
    
    callback(@[Identifier]);
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

+ (void)jailEvent:(NSString *)chatId
             data:(NSString *)data
{
    NSData *signalData = [@"{}" dataUsingEncoding:NSUTF8StringEncoding];
    NSDictionary *dict = [NSJSONSerialization JSONObjectWithData:signalData options:NSJSONReadingMutableContainers error:nil];
    [dict setValue:@"jail.signal" forKey:@"type"];
    NSDictionary *event = [[NSDictionary alloc] initWithObjectsAndKeys:chatId, @"chat_id", data, @"data", nil];
    [dict setValue:event forKey:@"event"];
    NSString *signal = [dict bv_jsonStringWithPrettyPrint:NO];
#if DEBUG
    NSLog(@"SignalEventData");
    NSLog(signal);
#endif
    [bridge.eventDispatcher sendAppEventWithName:@"gethEvent"
                                            body:@{@"jsonEvent": signal}];
    
    return;
}

@end
