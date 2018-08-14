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

////////////////////////////////////////////////////////////////////
#pragma mark - startNode
//////////////////////////////////////////////////////////////////// startNode
RCT_EXPORT_METHOD(startNode:(NSString *)configString
                      fleet:(NSString *)fleet) {
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
    NSArray *bootnodes = [configJSON valueForKeyPath:@"ClusterConfig.BootNodes"];
    NSString *networkDir = [rootUrl.path stringByAppendingString:dataDir];
    NSString *devCluster = [ReactNativeConfig envFor:@"ETHEREUM_DEV_CLUSTER"];
    NSString *logLevel = [[ReactNativeConfig envFor:@"LOG_LEVEL_STATUS_GO"] uppercaseString];
    char *configChars = GenerateConfig((char *)[networkDir UTF8String], (char *)[fleet UTF8String], networkId);
    NSString *config = [NSString stringWithUTF8String: configChars];
    configData = [config dataUsingEncoding:NSUTF8StringEncoding];
    NSDictionary *resultingConfigJson = [NSJSONSerialization JSONObjectWithData:configData options:NSJSONReadingMutableContainers error:nil];
    NSURL *networkDirUrl = [NSURL fileURLWithPath:networkDir];
    NSURL *logUrl = [networkDirUrl URLByAppendingPathComponent:@"geth.log"];
    [resultingConfigJson setValue:newKeystoreUrl.path forKey:@"KeyStoreDir"];
    [resultingConfigJson setValue:[NSNumber numberWithBool:[logLevel length] != 0] forKey:@"LogEnabled"];
    [resultingConfigJson setValue:([logLevel length] == 0 ? [NSNull null] : logUrl.path) forKey:@"LogFile"];
    [resultingConfigJson setValue:([logLevel length] == 0 ? [NSString stringWithUTF8String: "ERROR"] : logLevel) forKey:@"LogLevel"];

    [resultingConfigJson setValue:[NSNumber numberWithBool:YES] forKeyPath:@"WhisperConfig.LightClient"];

    if(upstreamURL != nil) {
        [resultingConfigJson setValue:[NSNumber numberWithBool:YES] forKeyPath:@"UpstreamConfig.Enabled"];
        [resultingConfigJson setValue:upstreamURL forKeyPath:@"UpstreamConfig.URL"];
    }

    if(bootnodes != nil) {
        [resultingConfigJson setValue:[NSNumber numberWithBool:YES] forKeyPath:@"ClusterConfig.Enabled"];
        [resultingConfigJson setValue:bootnodes forKeyPath:@"ClusterConfig.BootNodes"];
    }

    if([fleet length] > 0) {
        [resultingConfigJson setValue:fleet forKeyPath:@"ClusterConfig.Fleet"];
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
    char * result = Login((char *) [address UTF8String], (char *) [password UTF8String]);
    callback(@[[NSString stringWithUTF8String: result]]);
}

////////////////////////////////////////////////////////////////////
#pragma mark - SendTransaction
//////////////////////////////////////////////////////////////////// sendTransaction
RCT_EXPORT_METHOD(sendTransaction:(NSString *)txArgsJSON
                  password:(NSString *)password
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"SendTransaction() method called");
#endif
    char * result = SendTransaction((char *) [txArgsJSON UTF8String], (char *) [password UTF8String]);
    callback(@[[NSString stringWithUTF8String: result]]);
}

////////////////////////////////////////////////////////////////////
#pragma mark - SignMessage
//////////////////////////////////////////////////////////////////// signMessage
RCT_EXPORT_METHOD(signMessage:(NSString *)message
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"SignMessage() method called");
#endif
    char * result = SignMessage((char *) [message UTF8String]);
    callback(@[[NSString stringWithUTF8String: result]]);
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

RCT_EXPORT_METHOD(callRPC:(NSString *)payload
                  callback:(RCTResponseSenderBlock)callback) {
    dispatch_async( dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        char * result = CallRPC((char *) [payload UTF8String]);
        dispatch_async( dispatch_get_main_queue(), ^{
            callback(@[[NSString stringWithUTF8String: result]]);
        });
    });
}

RCT_EXPORT_METHOD(callPrivateRPC:(NSString *)payload
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

- (bool) is24Hour
{
    NSString *format = [NSDateFormatter dateFormatFromTemplate:@"j" options:0 locale:[NSLocale currentLocale]];
    return ([format rangeOfString:@"a"].location == NSNotFound);
}

- (NSDictionary *)constantsToExport
{
    return @{
             @"is24Hour": @(self.is24Hour),
             };
}

@end
