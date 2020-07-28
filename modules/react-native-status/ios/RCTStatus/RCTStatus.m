#import "RCTStatus.h"
#import "ReactNativeConfig.h"
#import "React/RCTBridge.h"
#import "React/RCTEventDispatcher.h"
#import "Statusgo/Statusgo.h"
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

@implementation Status

- (instancetype)init {
    self = [super init];
    if (!self) {
        return nil;
    }
    // Subscribing to the signals from Status-Go
    StatusgoSetMobileSignalHandler(self);
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

- (void)handleSignal:(NSString *)signal
{
    if(!signal){
#if DEBUG
        NSLog(@"SignalEvent nil");
#endif
        return;
    }

#if DEBUG
    NSLog(@"[handleSignal] Received an event from Status-Go: %@", signal);
#endif
    [bridge.eventDispatcher sendAppEventWithName:@"gethEvent"
                                            body:@{@"jsonEvent": signal}];

    return;
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
                        NSString *res = StatusgoInitKeystore(keystoreDir.path);
                        NSLog(@"InitKeyStore result %@", res);
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
    NSString *result = StatusgoExportNodeLogs();
    callback(@[result]);
}

//////////////////////////////////////////////////////////////////// addPeer
RCT_EXPORT_METHOD(addPeer:(NSString *)enode
                  callback:(RCTResponseSenderBlock)callback) {
  NSString *result = StatusgoAddPeer(enode);
  callback(@[result]);
#if DEBUG
  NSLog(@"AddPeer() method called");
#endif
}

//////////////////////////////////////////////////////////////////// getNodesFromContract
RCT_EXPORT_METHOD(getNodesFromContract:(NSString *)url
                               address:(NSString *) address
                              callback:(RCTResponseSenderBlock)callback) {
  NSString* result = StatusgoGetNodesFromContract(url, address);
  callback(@[result]);
#if DEBUG
  NSLog(@"GetNodesFromContract() method called");
#endif
}

//////////////////////////////////////////////////////////////////// chaosModeUpdate
RCT_EXPORT_METHOD(chaosModeUpdate:(BOOL)on
                  callback:(RCTResponseSenderBlock)callback) {
  NSString* result = StatusgoChaosModeUpdate(on);
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
    NSString *result = StatusgoDeleteMultiaccount(keyUID, multiaccountKeystoreDir.path);
    callback(@[result]);
}

//////////////////////////////////////////////////////////////////// multiAccountGenerateAndDeriveAddresses
RCT_EXPORT_METHOD(multiAccountGenerateAndDeriveAddresses:(NSString *)json
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"MultiAccountGenerateAndDeriveAddresses() method called");
#endif
    NSString *result = StatusgoMultiAccountGenerateAndDeriveAddresses(json);
    callback(@[result]);
}

//////////////////////////////////////////////////////////////////// MultiAccountStoreAccount
RCT_EXPORT_METHOD(multiAccountStoreAccount:(NSString *)json
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"MultiAccountStoreAccount() method called");
#endif
    NSString *result = StatusgoMultiAccountStoreAccount(json);
    callback(@[result]);
}

//////////////////////////////////////////////////////////////////// MultiAccountLoadAccount
RCT_EXPORT_METHOD(multiAccountLoadAccount:(NSString *)json
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"MultiAccountLoadAccount() method called");
#endif
    NSString *result = StatusgoMultiAccountLoadAccount(json);
    callback(@[result]);
}

//////////////////////////////////////////////////////////////////// MultiAccountReset
RCT_EXPORT_METHOD(multiAccountReset:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"MultiAccountReset() method called");
#endif
    NSString *result = StatusgoMultiAccountReset();
    callback(@[result]);
}

//////////////////////////////////////////////////////////////////// multiAccountStoreDerived
RCT_EXPORT_METHOD(multiAccountStoreDerived:(NSString *)json
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"MultiAccountStoreDerived() method called");
#endif
    NSString *result = StatusgoMultiAccountStoreDerivedAccounts(json);
    callback(@[result]);
}

//////////////////////////////////////////////////////////////////// multiAccountImportPrivateKey
RCT_EXPORT_METHOD(multiAccountImportPrivateKey:(NSString *)json
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"MultiAccountImportPrivateKey() method called");
#endif
    NSString *result = StatusgoMultiAccountImportPrivateKey(json);
    callback(@[result]);
}

//////////////////////////////////////////////////////////////////// hashTransaction
RCT_EXPORT_METHOD(hashTransaction:(NSString *)txArgsJSON
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"HashTransaction() method called");
#endif
    NSString *result = StatusgoHashTransaction(txArgsJSON);
    callback(@[result]);
}

//////////////////////////////////////////////////////////////////// multiAccountImportMnemonic
RCT_EXPORT_METHOD(multiAccountImportMnemonic:(NSString *)json
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"MultiAccountImportMnemonic() method called");
#endif
    NSString *result = StatusgoMultiAccountImportMnemonic(json);
    callback(@[result]);
}

//////////////////////////////////////////////////////////////////// multiAccountDeriveAddresses
RCT_EXPORT_METHOD(multiAccountDeriveAddresses:(NSString *)json
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"MultiAccountDeriveAddresses() method called");
#endif
    NSString *result = StatusgoMultiAccountDeriveAddresses(json);
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
    NSString *result = StatusgoSaveAccountAndLogin(multiaccountData, password, settings, finalConfig, accountsData);
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
    NSString *result = StatusgoSaveAccountAndLoginWithKeycard(multiaccountData, password, settings, finalConfig, accountsData, chatKey);
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
        NSString *migrationResult = StatusgoMigrateKeyStoreDir(accountData, password, oldKeystoreDir.path, multiaccountKeystoreDir.path);
        NSLog(@"keystore migration result %@", migrationResult);
        
        NSString *initKeystoreResult = StatusgoInitKeystore(multiaccountKeystoreDir.path);
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
    NSString *result = StatusgoLogin(accountData, password);
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
    
    NSString *result = StatusgoLoginWithKeycard(accountData, password, chatKey);

    NSLog(@"%@", result);
}

//////////////////////////////////////////////////////////////////// logout
RCT_EXPORT_METHOD(logout) {
#if DEBUG
    NSLog(@"Logout() method called");
#endif
    NSString *result = StatusgoLogout();

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

    NSString *result = StatusgoOpenAccounts(rootUrl.path);
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

    NSString *result = StatusgoVerifyAccountPassword(absKeystoreUrl.path, address, password);
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
    NSString *result = StatusgoSendTransaction(txArgsJSON, password);
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
    NSString *result = StatusgoSignMessage(message);
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
    NSString *result = StatusgoSignTypedData(data, account, password);
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
    NSString *result = StatusgoSignGroupMembership(content);
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
    NSString *result = StatusgoExtractGroupMembershipSignatures(content);
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
        NSString *result = StatusgoCallRPC(payload);
        dispatch_async(dispatch_get_main_queue(), ^{
            callback(@[result]);
        });
    });
}

RCT_EXPORT_BLOCKING_SYNCHRONOUS_METHOD(generateAlias:(NSString *)publicKey) {
  return StatusgoGenerateAlias(publicKey);
}

RCT_EXPORT_METHOD(generateAliasAsync:(NSString *)publicKey
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"generateAliasAsync() method called");
#endif
    NSString *result = StatusgoGenerateAlias(publicKey);
    callback(@[result]);
}

RCT_EXPORT_BLOCKING_SYNCHRONOUS_METHOD(identicon:(NSString *)publicKey) {
  return StatusgoIdenticon(publicKey);
}

RCT_EXPORT_METHOD(validateMnemonic:(NSString *)seed
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"validateMnemonic() method called");
#endif
    NSString *result = StatusgoValidateMnemonic(seed);
    callback(@[result]);
}

RCT_EXPORT_METHOD(identiconAsync:(NSString *)publicKey
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"identiconAsync() method called");
#endif
    NSString *result = StatusgoIdenticon(publicKey);
    callback(@[result]);
}

RCT_EXPORT_METHOD(generateAliasAndIdenticonAsync:(NSString *)publicKey
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"generateAliasAndIdenticonAsync() method called");
#endif
    NSString *identiconResult = StatusgoIdenticon(publicKey);
    NSString *aliasResult = StatusgoGenerateAlias(publicKey);
    callback(@[aliasResult, identiconResult]);
}

RCT_EXPORT_METHOD(callPrivateRPC:(NSString *)payload
                  callback:(RCTResponseSenderBlock)callback) {
    dispatch_async( dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        NSString *result = StatusgoCallPrivateRPC(payload);
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
    StatusgoConnectionChange(type, isExpensive ? 1 : 0);
}

RCT_EXPORT_METHOD(appStateChange:(NSString *)type) {
#if DEBUG
    NSLog(@"AppStateChange() method called");
#endif
    StatusgoAppStateChange(type);
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
