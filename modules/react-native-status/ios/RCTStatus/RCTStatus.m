#import "RCTStatus.h"
#import "ReactNativeConfig.h"
#import "React/RCTBridge.h"
#import "React/RCTEventDispatcher.h"
#import <SSZipArchive.h>

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

static RCTBridge *bridge;

const char * fromObjCStr(NSString *s) {
  return [s UTF8String];
}

NSString * toObjCStr(char *s) {
  return [NSString stringWithUTF8String:s];
}

void handleSignal(char *signal)
{
    if(!signal){
#if DEBUG
        NSLog(@"SignalEvent nil");
#endif
        return;
    }

#if DEBUG
    NSLog(@"[handleSignal] Received an event from Status-Go: %", signal);
#endif
    [bridge.eventDispatcher sendAppEventWithName:@"gethEvent"
                                            body:@{@"jsonEvent": toObjCStr(signal)}];

    return;
}
@implementation Status

- (instancetype)init {
    self = [super init];
    if (!self) {
        return nil;
    }
    NimMain();
    // Subscribing to the signals from Status-Go
    //StatusgoSetMobileSignalHandler(self);
    setSignalEventCallback(&handleSignal);
    return self;
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
#pragma mark - InitKeystore method
//////////////////////////////////////////////////////////////////// StopNode
RCT_EXPORT_METHOD(initKeystore:(NSString *)keyUID
                      callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"initKeystore() method called");
#endif
    NSFileManager *fileManager = [NSFileManager defaultManager];
    NSURL *rootUrl =[[fileManager
                      URLsForDirectory:NSLibraryDirectory inDomains:NSUserDomainMask]
                     lastObject];

    NSURL *commonKeystoreDir = [rootUrl URLByAppendingPathComponent:@"keystore"];
    NSURL *keystoreDir = [commonKeystoreDir URLByAppendingPathComponent:keyUID];

    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0),
                   ^(void)
                   {
                        char *res = initKeystore(fromObjCStr(keystoreDir.path));
                        NSLog(@"InitKeyStore result %", res);
                        callback(@[]);
                   });
}

////////////////////////////////////////////////////////////////////
#pragma mark - SendLogs method
//////////////////////////////////////////////////////////////////// sendLogs
RCT_EXPORT_METHOD(sendLogs:(NSString *)dbJson
                  jsLogs:(NSString *)jsLogs
                  callback:(RCTResponseSenderBlock)callback) {
    // TODO: Implement SendLogs for iOS
#if DEBUG
    NSLog(@"SendLogs() method called, not implemented");
#endif
    NSFileManager *fileManager = [NSFileManager defaultManager];
    NSError *error = nil;
    NSURL *rootUrl =[[fileManager
                      URLsForDirectory:NSLibraryDirectory inDomains:NSUserDomainMask]
                     lastObject];

    NSURL *zipFile = [rootUrl URLByAppendingPathComponent:@"logs.zip"];
    [fileManager removeItemAtPath:zipFile.path error:nil];

    NSURL *logsFolderName = [rootUrl URLByAppendingPathComponent:@"logs"];

    if (![fileManager fileExistsAtPath:logsFolderName.path])
        [fileManager createDirectoryAtPath:logsFolderName.path withIntermediateDirectories:YES attributes:nil error:&error];

    NSURL *dbFile = [logsFolderName URLByAppendingPathComponent:@"db.json"];
    NSURL *jsLogsFile = [logsFolderName URLByAppendingPathComponent:@"Status.log"];
#if DEBUG
    NSString *networkDirPath = @"ethereum/mainnet_rpc_dev";
#else
    NSString *networkDirPath = @"ethereum/mainnet_rpc";
#endif

    NSURL *networkDir = [rootUrl URLByAppendingPathComponent:networkDirPath];
    NSURL *originalGethLogsFile = [networkDir URLByAppendingPathComponent:@"geth.log"];
    NSURL *gethLogsFile = [logsFolderName URLByAppendingPathComponent:@"geth.log"];

    [dbJson writeToFile:dbFile.path atomically:YES encoding:NSUTF8StringEncoding error:nil];
    [jsLogs writeToFile:jsLogsFile.path atomically:YES encoding:NSUTF8StringEncoding error:nil];

    //NSString* gethLogs = StatusgoExportNodeLogs();
    //[gethLogs writeToFile:gethLogsFile.path atomically:YES encoding:NSUTF8StringEncoding error:nil];
    [fileManager copyItemAtPath:originalGethLogsFile.path toPath:gethLogsFile.path error:nil];

    [SSZipArchive createZipFileAtPath:zipFile.path withContentsOfDirectory:logsFolderName.path];
    [fileManager removeItemAtPath:logsFolderName.path error:nil];

    callback(@[zipFile.path]);
}

//////////////////////////////////////////////////////////////////// addPeer
RCT_EXPORT_METHOD(exportLogs:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"exportLogs() method called");
#endif
    NSString *result = toObjCStr(exportNodeLogs());
    callback(@[result]);
}

//////////////////////////////////////////////////////////////////// addPeer
RCT_EXPORT_METHOD(addPeer:(NSString *)enode
                  callback:(RCTResponseSenderBlock)callback) {
  NSString *result = toObjCStr(addPeer(fromObjCStr(enode)));
  callback(@[result]);
#if DEBUG
  NSLog(@"AddPeer() method called");
#endif
}

//////////////////////////////////////////////////////////////////// getNodesFromContract
RCT_EXPORT_METHOD(getNodesFromContract:(NSString *)url
                               address:(NSString *) address
                              callback:(RCTResponseSenderBlock)callback) {
  NSString* result = toObjCStr(getNodesFromContract(fromObjCStr(url), fromObjCStr(address)));
  callback(@[result]);
#if DEBUG
  NSLog(@"GetNodesFromContract() method called");
#endif
}

//////////////////////////////////////////////////////////////////// chaosModeUpdate
RCT_EXPORT_METHOD(chaosModeUpdate:(BOOL)on
                  callback:(RCTResponseSenderBlock)callback) {
  NSString* result = toObjCStr(chaosModeUpdate(on));
  callback(@[result]);
#if DEBUG
  NSLog(@"ChaosModeUpdate() method called");
#endif
}

//////////////////////////////////////////////////////////////////// multiAccountImportPrivateKey
RCT_EXPORT_METHOD(deleteMultiaccount:(NSString *)keyUID
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"MultiAccountImportPrivateKey() method called");
#endif
    NSURL *multiaccountKeystoreDir = [self getKeyStoreDir:keyUID];
    NSString *result = toObjCStr(deleteMultiAccount(fromObjCStr(keyUID), fromObjCStr(multiaccountKeystoreDir.path)));
    callback(@[result]);
}

//////////////////////////////////////////////////////////////////// multiAccountGenerateAndDeriveAddresses
RCT_EXPORT_METHOD(multiAccountGenerateAndDeriveAddresses:(NSString *)json
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"MultiAccountGenerateAndDeriveAddresses() method called");
#endif
    NSString *result = toObjCStr(multiAccountGenerateAndDeriveAddresses(fromObjCStr(json)));
    callback(@[result]);
}

//////////////////////////////////////////////////////////////////// MultiAccountStoreAccount
RCT_EXPORT_METHOD(multiAccountStoreAccount:(NSString *)json
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"MultiAccountStoreAccount() method called");
#endif
    NSString *result = toObjCStr(multiAccountStoreAccount(fromObjCStr(json)));
    callback(@[result]);
}

//////////////////////////////////////////////////////////////////// MultiAccountLoadAccount
RCT_EXPORT_METHOD(multiAccountLoadAccount:(NSString *)json
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"MultiAccountLoadAccount() method called");
#endif
    NSString *result = toObjCStr(multiAccountLoadAccount(fromObjCStr(json)));
    callback(@[result]);
}

//////////////////////////////////////////////////////////////////// MultiAccountReset
RCT_EXPORT_METHOD(multiAccountReset:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"MultiAccountReset() method called");
#endif
    NSString *result = toObjCStr(multiAccountReset());
    callback(@[result]);
}

//////////////////////////////////////////////////////////////////// multiAccountStoreDerived
RCT_EXPORT_METHOD(multiAccountStoreDerived:(NSString *)json
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"MultiAccountStoreDerived() method called");
#endif
    NSString *result = toObjCStr(multiAccountStoreDerivedAccounts(fromObjCStr(json)));
    callback(@[result]);
}

//////////////////////////////////////////////////////////////////// multiAccountImportPrivateKey
RCT_EXPORT_METHOD(multiAccountImportPrivateKey:(NSString *)json
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"MultiAccountImportPrivateKey() method called");
#endif
    NSString *result = toObjCStr(multiAccountImportPrivateKey(fromObjCStr(json)));
    callback(@[result]);
}

//////////////////////////////////////////////////////////////////// hashTransaction
RCT_EXPORT_METHOD(hashTransaction:(NSString *)txArgsJSON
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"HashTransaction() method called");
#endif
    NSString *result = toObjCStr(hashTransaction(fromObjCStr(txArgsJSON)));
    callback(@[result]);
}

//////////////////////////////////////////////////////////////////// hashMessage
RCT_EXPORT_METHOD(hashMessage:(NSString *)message
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"hashMessage() method called");
#endif
    NSString *result = toObjCStr(hashMessage(fromObjCStr(message)));
    callback(@[result]);
}

//////////////////////////////////////////////////////////////////// hashTypedData
RCT_EXPORT_METHOD(hashTypedData:(NSString *)data
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"hashTypedData() method called");
#endif
    NSString *result = toObjCStr(hashTypedData(fromObjCStr(data)));
    callback(@[result]);
}

//////////////////////////////////////////////////////////////////// sendTransactionWithSignature
RCT_EXPORT_METHOD(sendTransactionWithSignature:(NSString *)txArgsJSON
                  signature:(NSString *)signature
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"sendTransactionWithSignature() method called");
#endif
    NSString *result = toObjCStr(sendTransactionWithSignature(fromObjCStr(txArgsJSON), fromObjCStr(signature)));
    callback(@[result]);
}

//////////////////////////////////////////////////////////////////// multiAccountImportMnemonic
RCT_EXPORT_METHOD(multiAccountImportMnemonic:(NSString *)json
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"MultiAccountImportMnemonic() method called");
#endif
    NSString *result = toObjCStr(multiAccountImportMnemonic(fromObjCStr(json)));
    callback(@[result]);
}

//////////////////////////////////////////////////////////////////// multiAccountDeriveAddresses
RCT_EXPORT_METHOD(multiAccountDeriveAddresses:(NSString *)json
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"MultiAccountDeriveAddresses() method called");
#endif
    NSString *result = toObjCStr(multiAccountDeriveAddresses(fromObjCStr(json)));
    callback(@[result]);
}

-(NSString *) getKeyUID:(NSString *)jsonString {
    NSData *data = [jsonString dataUsingEncoding:NSUTF8StringEncoding];
    NSDictionary *json = [NSJSONSerialization
                          JSONObjectWithData:data
                          options:NSJSONReadingMutableContainers
                          error:nil];

    return [json valueForKey:@"key-uid"];
}

//////////////////////////////////////////////////////////////////// prepareDirAndUpdateConfig
-(NSString *) prepareDirAndUpdateConfig:(NSString *)config
                             withKeyUID:(NSString *)keyUID {
    NSFileManager *fileManager = [NSFileManager defaultManager];
    NSError *error = nil;
    NSURL *rootUrl =[[fileManager
                      URLsForDirectory:NSLibraryDirectory inDomains:NSUserDomainMask]
                     lastObject];
    NSURL *absTestnetFolderName = [rootUrl URLByAppendingPathComponent:@"ethereum/testnet"];

    if (![fileManager fileExistsAtPath:absTestnetFolderName.path])
        [fileManager createDirectoryAtPath:absTestnetFolderName.path withIntermediateDirectories:YES attributes:nil error:&error];

    NSURL *flagFolderUrl = [rootUrl URLByAppendingPathComponent:@"ropsten_flag"];

    if(![fileManager fileExistsAtPath:flagFolderUrl.path]){
        NSLog(@"remove lightchaindata");
        NSURL *absLightChainDataUrl = [absTestnetFolderName URLByAppendingPathComponent:@"StatusIM/lightchaindata"];
        if([fileManager fileExistsAtPath:absLightChainDataUrl.path]) {
            [fileManager removeItemAtPath:absLightChainDataUrl.path
                                    error:nil];
        }
        [fileManager createDirectoryAtPath:flagFolderUrl.path
               withIntermediateDirectories:NO
                                attributes:nil
                                     error:&error];
    }

    NSLog(@"after remove lightchaindata");

    NSString *keystore = @"keystore";
    NSURL *absTestnetKeystoreUrl = [absTestnetFolderName URLByAppendingPathComponent:keystore];
    NSURL *absKeystoreUrl = [rootUrl URLByAppendingPathComponent:keystore];
    if([fileManager fileExistsAtPath:absTestnetKeystoreUrl.path]){
        NSLog(@"copy keystore");
        [fileManager copyItemAtPath:absTestnetKeystoreUrl.path toPath:absKeystoreUrl.path error:nil];
        [fileManager removeItemAtPath:absTestnetKeystoreUrl.path error:nil];
    }

    NSLog(@"after lightChainData");

    NSLog(@"preconfig: %@", config);
    NSData *configData = [config dataUsingEncoding:NSUTF8StringEncoding];
    NSDictionary *configJSON = [NSJSONSerialization JSONObjectWithData:configData options:NSJSONReadingMutableContainers error:nil];
    NSString *relativeDataDir = [configJSON objectForKey:@"DataDir"];
    NSString *absDataDir = [rootUrl.path stringByAppendingString:relativeDataDir];
    NSURL *absDataDirUrl = [NSURL fileURLWithPath:absDataDir];
    NSURL *dataDirUrl = [NSURL fileURLWithPath:relativeDataDir];
    NSURL *logUrl = [dataDirUrl URLByAppendingPathComponent:@"geth.log"];
    NSString *keystoreDir = [@"/keystore/" stringByAppendingString:keyUID];
    [configJSON setValue:keystoreDir forKey:@"KeyStoreDir"];
    [configJSON setValue:@"" forKey:@"LogDir"];
    [configJSON setValue:logUrl.path forKey:@"LogFile"];

    NSString *resultingConfig = [configJSON bv_jsonStringWithPrettyPrint:NO];
    NSLog(@"node config %@", resultingConfig);

    if(![fileManager fileExistsAtPath:absDataDir]) {
        [fileManager createDirectoryAtPath:absDataDir
               withIntermediateDirectories:YES attributes:nil error:nil];
    }

    NSLog(@"logUrlPath %@ rootDir %@", logUrl.path, rootUrl.path);
    NSURL *absLogUrl = [absDataDirUrl URLByAppendingPathComponent:@"geth.log"];
    if(![fileManager fileExistsAtPath:absLogUrl.path]) {
        NSMutableDictionary *dict = [[NSMutableDictionary alloc] init];
        [dict setObject:[NSNumber numberWithInt:511] forKey:NSFilePosixPermissions];
        [fileManager createFileAtPath:absLogUrl.path contents:nil attributes:dict];
    }

    return resultingConfig;

}


//////////////////////////////////////////////////////////////////// prepareDirAndUpdateConfig
RCT_EXPORT_METHOD(prepareDirAndUpdateConfig:(NSString *)keyUID
                                     config:(NSString *)config
                                   callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"PrepareDirAndUpdateConfig() method called");
#endif
    NSString *updatedConfig = [self prepareDirAndUpdateConfig:config
                                                   withKeyUID:keyUID];
    callback(@[updatedConfig]);
}

//////////////////////////////////////////////////////////////////// saveAccountAndLogin
RCT_EXPORT_METHOD(saveAccountAndLogin:(NSString *)multiaccountData
                  password:(NSString *)password
                  settings:(NSString *)settings
                  config:(NSString *)config
                  accountsData:(NSString *)accountsData) {
#if DEBUG
    NSLog(@"SaveAccountAndLogin() method called");
#endif
    NSString *keyUID = [self getKeyUID:multiaccountData];
    NSString *finalConfig = [self prepareDirAndUpdateConfig:config
                                                 withKeyUID:keyUID];
    NSString *result = toObjCStr(saveAccountAndLogin(fromObjCStr(multiaccountData), fromObjCStr(password), fromObjCStr(settings), fromObjCStr(finalConfig), fromObjCStr(accountsData)));
    NSLog(@"%@", result);
}

//////////////////////////////////////////////////////////////////// saveAccountAndLoginWithKeycard
RCT_EXPORT_METHOD(saveAccountAndLoginWithKeycard:(NSString *)multiaccountData
                  password:(NSString *)password
                  settings:(NSString *)settings
                  config:(NSString *)config
                  accountsData:(NSString *)accountsData
                  chatKey:(NSString *)chatKey) {
#if DEBUG
    NSLog(@"SaveAccountAndLoginWithKeycard() method called");
#endif
    NSString *keyUID = [self getKeyUID:multiaccountData];
    NSString *finalConfig = [self prepareDirAndUpdateConfig:config
                                                 withKeyUID:keyUID];
    NSString *result = toObjCStr(saveAccountAndLoginWithKeycard(fromObjCStr(multiaccountData), fromObjCStr(password), fromObjCStr(settings), fromObjCStr(finalConfig), fromObjCStr(accountsData), fromObjCStr(chatKey)));
    NSLog(@"%@", result);
}

- (NSURL *) getKeyStoreDir:(NSString *)keyUID {
    NSFileManager *fileManager = [NSFileManager defaultManager];
    NSURL *rootUrl =[[fileManager
                      URLsForDirectory:NSLibraryDirectory inDomains:NSUserDomainMask]
                     lastObject];

    NSURL *oldKeystoreDir = [rootUrl URLByAppendingPathComponent:@"keystore"];
    NSURL *multiaccountKeystoreDir = [oldKeystoreDir URLByAppendingPathComponent:keyUID];

    return multiaccountKeystoreDir;
}

- (void) migrateKeystore:(NSString *)accountData
                password:(NSString *)password {
    NSFileManager *fileManager = [NSFileManager defaultManager];
    NSURL *rootUrl =[[fileManager
                      URLsForDirectory:NSLibraryDirectory inDomains:NSUserDomainMask]
                     lastObject];

    NSString *keyUID = [self getKeyUID:accountData];
    NSURL *oldKeystoreDir = [rootUrl URLByAppendingPathComponent:@"keystore"];
    NSURL *multiaccountKeystoreDir = [self getKeyStoreDir:keyUID];

    NSArray *keys = [fileManager contentsOfDirectoryAtPath:multiaccountKeystoreDir.path error:nil];
    if (keys.count == 0) {
        NSString *migrationResult = toObjCStr(migrateKeyStoreDir(fromObjCStr(accountData), fromObjCStr(password), 
                  fromObjCStr(oldKeystoreDir.path), fromObjCStr(multiaccountKeystoreDir.path)));
        NSLog(@"keystore migration result %@", migrationResult);
        NSString *initKeystoreResult = toObjCStr(initKeystore(fromObjCStr(multiaccountKeystoreDir.path)));
        NSLog(@"InitKeyStore result %@", initKeystoreResult);
    }
}

//////////////////////////////////////////////////////////////////// login
RCT_EXPORT_METHOD(login:(NSString *)accountData
                  password:(NSString *)password) {
#if DEBUG
    NSLog(@"Login() method called");
#endif
    [self migrateKeystore:accountData password:password];
    NSString *result = toObjCStr(login(fromObjCStr(accountData), fromObjCStr(password)));
    NSLog(@"%@", result);
}

//////////////////////////////////////////////////////////////////// loginWithKeycard
RCT_EXPORT_METHOD(loginWithKeycard:(NSString *)accountData
                  password:(NSString *)password
                  chatKey:(NSString *)chatKey) {
#if DEBUG
    NSLog(@"LoginWithKeycard() method called");
#endif
    [self migrateKeystore:accountData password:password];
    NSString *result = toObjCStr(loginWithKeycard(fromObjCStr(accountData), fromObjCStr(password), fromObjCStr(chatKey)));

    NSLog(@"%@", result);
}

//////////////////////////////////////////////////////////////////// logout
RCT_EXPORT_METHOD(logout) {
#if DEBUG
    NSLog(@"Logout() method called");
#endif
    NSString *result = toObjCStr(logout());

    NSLog(@"%@", result);
}

//////////////////////////////////////////////////////////////////// openAccounts
RCT_EXPORT_METHOD(openAccounts:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"OpenAccounts() method called");
#endif
    NSFileManager *fileManager = [NSFileManager defaultManager];
    NSURL *rootUrl =[[fileManager
                      URLsForDirectory:NSLibraryDirectory inDomains:NSUserDomainMask]
                     lastObject];

    NSString *result = toObjCStr(openAccounts(fromObjCStr(rootUrl.path)));
    callback(@[result]);
}

//////////////////////////////////////////////////////////////////// verityAccountPassword
RCT_EXPORT_METHOD(verify:(NSString *)address
                  password:(NSString *)password
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"VerifyAccountPassword() method called");
#endif
    NSFileManager *fileManager = [NSFileManager defaultManager];
    NSURL *rootUrl =[[fileManager
                      URLsForDirectory:NSLibraryDirectory inDomains:NSUserDomainMask]
                     lastObject];
    NSURL *absKeystoreUrl = [rootUrl URLByAppendingPathComponent:@"keystore"];

    NSString *result = toObjCStr(verifyAccountPassword(fromObjCStr(absKeystoreUrl.path), fromObjCStr(address), fromObjCStr(password)));
    callback(@[result]);
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
    NSString *result = toObjCStr(sendTransaction(fromObjCStr(txArgsJSON), fromObjCStr(password)));
    callback(@[result]);
}

////////////////////////////////////////////////////////////////////
#pragma mark - SignMessage
//////////////////////////////////////////////////////////////////// signMessage
RCT_EXPORT_METHOD(signMessage:(NSString *)message
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"SignMessage() method called");
#endif
    NSString *result = toObjCStr(signMessage(fromObjCStr(message)));
    callback(@[result]);
}

////////////////////////////////////////////////////////////////////
#pragma mark - SignTypedData
//////////////////////////////////////////////////////////////////// signTypedData
RCT_EXPORT_METHOD(signTypedData:(NSString *)data
                  account:(NSString *)account
                  password:(NSString *)password
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"SignTypedData() method called");
#endif
    NSString *result = toObjCStr(signTypedData(fromObjCStr(data), fromObjCStr(account), fromObjCStr(password)));
    callback(@[result]);
}

////////////////////////////////////////////////////////////////////
#pragma mark - SignGroupMembership
//////////////////////////////////////////////////////////////////// signGroupMembership
RCT_EXPORT_METHOD(signGroupMembership:(NSString *)content
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"SignGroupMembership() method called");
#endif
    NSString *result = toObjCStr(signGroupMembership(fromObjCStr(content)));
    callback(@[result]);
}

////////////////////////////////////////////////////////////////////
#pragma mark - ExtractGroupMembershipSignatures
//////////////////////////////////////////////////////////////////// extractGroupMembershipSignatures
RCT_EXPORT_METHOD(extractGroupMembershipSignatures:(NSString *)content
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"ExtractGroupMembershipSignatures() method called");
#endif
    NSString *result = toObjCStr(extractGroupMembershipSignatures(fromObjCStr(content)));
    callback(@[result]);
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
        NSString *result = toObjCStr(callRPC(fromObjCStr(payload)));
        dispatch_async(dispatch_get_main_queue(), ^{
            callback(@[result]);
        });
    });
}

RCT_EXPORT_BLOCKING_SYNCHRONOUS_METHOD(generateAlias:(NSString *)publicKey) {
  const char * publicKeyStr = fromObjCStr(publicKey);

  NSString * result = toObjCStr(generateAlias(publicKeyStr));
  return result;
}

RCT_EXPORT_METHOD(generateAliasAsync:(NSString *)publicKey
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"generateAliasAsync() method called");
#endif

    const char * publicKeyStr = fromObjCStr(publicKey);
    NSString *result = toObjCStr(generateAlias(publicKeyStr));
    callback(@[result]);
}

RCT_EXPORT_BLOCKING_SYNCHRONOUS_METHOD(identicon:(NSString *)publicKey) {
  if (publicKey == NULL) {
    return toObjCStr("");
  }
  const char * publicKeyStr = fromObjCStr(publicKey);
  NSString * result = toObjCStr(identicon(publicKeyStr));
  return result;
}

RCT_EXPORT_METHOD(validateMnemonic:(NSString *)seed
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"validateMnemonic() method called");
#endif
    NSString *result = toObjCStr(validateMnemonic(fromObjCStr(seed)));
    callback(@[result]);
}

RCT_EXPORT_METHOD(identiconAsync:(NSString *)publicKey
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"identiconAsync() method called");
#endif

    const char * publicKeyStr = fromObjCStr(publicKey);
    NSString *result = toObjCStr(identicon(publicKeyStr));
    callback(@[result]);
}

RCT_EXPORT_METHOD(generateAliasAndIdenticonAsync:(NSString *)publicKey
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"generateAliasAndIdenticonAsync() method called");
#endif
    const char * publicKeyStr = fromObjCStr(publicKey);
    NSString *identiconResult = toObjCStr(identicon(publicKeyStr));

    NSString *aliasResult = toObjCStr(generateAlias(publicKeyStr));
    callback(@[aliasResult, identiconResult]);
}

RCT_EXPORT_METHOD(callPrivateRPC:(NSString *)payload
                  callback:(RCTResponseSenderBlock)callback) {
    dispatch_async( dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        NSString *result = toObjCStr(callPrivateRPC(fromObjCStr(payload)));
        dispatch_async(dispatch_get_main_queue(), ^{
            callback(@[result]);
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
    connectionChange(fromObjCStr(type), isExpensive ? 1 : 0);
}

RCT_EXPORT_METHOD(appStateChange:(NSString *)type) {
#if DEBUG
    NSLog(@"AppStateChange() method called");
#endif
    appStateChange(fromObjCStr(type));
}

RCT_EXPORT_METHOD(stopWallet) {
#if DEBUG
    NSLog(@"StopWallet() method called");
#endif
    stopWallet();
}

RCT_EXPORT_METHOD(startWallet:(BOOL)watchNewBlocks) {
#if DEBUG
    NSLog(@"StartWallet() method called");
#endif
    startWallet(watchNewBlocks);
}

RCT_EXPORT_METHOD(stopLocalNotifications) {
#if DEBUG
    NSLog(@"StopLocalNotifications() method called");
#endif
    stopLocalNotifications();
}

RCT_EXPORT_METHOD(startLocalNotifications) {
#if DEBUG
    NSLog(@"StartLocalNotifications() method called");
#endif
    startLocalNotifications();
}

RCT_EXPORT_METHOD(exportUnencryptedDatabase:(NSString *)accountData
                  password:(NSString *)password) {
#if DEBUG
    NSLog(@"exportUnencryptedDatabase() method called");
#endif
    "";
}

RCT_EXPORT_METHOD(importUnencryptedDatabase:(NSString *)accountData
                  password:(NSString *)password) {
#if DEBUG
    NSLog(@"importUnencryptedDatabase() method called");
#endif
    "";
}

RCT_EXPORT_METHOD(setBlankPreviewFlag:(BOOL *)newValue)
{
  NSUserDefaults *userDefaults = [NSUserDefaults standardUserDefaults];

  [userDefaults setBool:newValue forKey:@"BLANK_PREVIEW"];

  [userDefaults synchronize];
}

RCT_EXPORT_METHOD(activateKeepAwake)
{
    dispatch_async(dispatch_get_main_queue(), ^{
        [[UIApplication sharedApplication] setIdleTimerDisabled:YES];
    });
}

RCT_EXPORT_METHOD(deactivateKeepAwake)
{
    dispatch_async(dispatch_get_main_queue(), ^{
        [[UIApplication sharedApplication] setIdleTimerDisabled:NO];
    });
}

//// deviceinfo

- (bool) is24Hour
{
    NSString *format = [NSDateFormatter dateFormatFromTemplate:@"j" options:0 locale:[NSLocale currentLocale]];
    return ([format rangeOfString:@"a"].location == NSNotFound);
}

- (NSString *)getBuildId {
    return @"not available";
}

- (NSString*) deviceId
{
    struct utsname systemInfo;

    uname(&systemInfo);

    NSString* deviceId = [NSString stringWithCString:systemInfo.machine
                                            encoding:NSUTF8StringEncoding];

    if ([deviceId isEqualToString:@"i386"] || [deviceId isEqualToString:@"x86_64"] ) {
        deviceId = [NSString stringWithFormat:@"%s", getenv("SIMULATOR_MODEL_IDENTIFIER")];
    }

    return deviceId;
}

- (NSString*) deviceName
{

    NSString* deviceName = nil;

    if ([self.deviceId rangeOfString:@"iPod"].location != NSNotFound) {
        deviceName = @"iPod Touch";
    }
    else if([self.deviceId rangeOfString:@"iPad"].location != NSNotFound) {
        deviceName = @"iPad";
    }
    else if([self.deviceId rangeOfString:@"iPhone"].location != NSNotFound){
        deviceName = @"iPhone";
    }
    else if([self.deviceId rangeOfString:@"AppleTV"].location != NSNotFound){
        deviceName = @"Apple TV";
    }

    return deviceName;
}

- (NSDictionary *)constantsToExport
{
    return @{
             @"is24Hour": @(self.is24Hour),
             @"model": self.deviceName ?: [NSNull null],
             @"brand": @"Apple",
             @"buildId": [self getBuildId],
             @"deviceId": self.deviceId ?: [NSNull null],
             };
}

+ (BOOL)requiresMainQueueSetup
{
    return NO;
}

@end
