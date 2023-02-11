#import "RCTStatus.h"
#import "ReactNativeConfig.h"
#import "React/RCTBridge.h"
#import "React/RCTEventDispatcher.h"
#import "Statusgo.h"
#import "SSZipArchive.h"

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

#pragma mark - shouldMoveToInternalStorage

RCT_EXPORT_METHOD(shouldMoveToInternalStorage:(RCTResponseSenderBlock)onResultCallback) {
    // Android only
    onResultCallback(@[[NSNull null]]);
}


#pragma mark - moveToInternalStorage

RCT_EXPORT_METHOD(moveToInternalStorage:(RCTResponseSenderBlock)onResultCallback) {
    // Android only
    onResultCallback(@[[NSNull null]]);
}



#pragma mark - InitKeystore method

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


#pragma mark - SendLogs method

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

#if DEBUG
    NSString *goerliNetworkDirPath = @"ethereum/goerli_rpc_dev";
#else
    NSString *goerliNetworkDirPath = @"ethereum/goerli_rpc";
#endif

    NSURL *networkDir = [rootUrl URLByAppendingPathComponent:networkDirPath];
    NSURL *originalGethLogsFile = [networkDir URLByAppendingPathComponent:@"geth.log"];
    NSURL *gethLogsFile = [logsFolderName URLByAppendingPathComponent:@"mainnet_geth.log"];

    NSURL *goerliNetworkDir = [rootUrl URLByAppendingPathComponent:goerliNetworkDirPath];
    NSURL *goerliGethLogsFile = [goerliNetworkDir URLByAppendingPathComponent:@"geth.log"];
    NSURL *goerliLogsFile = [logsFolderName URLByAppendingPathComponent:@"goerli_geth.log"];

    NSURL *mainGethLogsFile = [rootUrl URLByAppendingPathComponent:@"geth.log"];
    NSURL *mainLogsFile = [logsFolderName URLByAppendingPathComponent:@"geth.log"];

    [dbJson writeToFile:dbFile.path atomically:YES encoding:NSUTF8StringEncoding error:nil];
    [jsLogs writeToFile:jsLogsFile.path atomically:YES encoding:NSUTF8StringEncoding error:nil];

    //NSString* gethLogs = StatusgoExportNodeLogs();
    //[gethLogs writeToFile:gethLogsFile.path atomically:YES encoding:NSUTF8StringEncoding error:nil];
    [fileManager copyItemAtPath:originalGethLogsFile.path toPath:gethLogsFile.path error:nil];
    [fileManager copyItemAtPath:goerliGethLogsFile.path toPath:goerliLogsFile.path error:nil];
    [fileManager copyItemAtPath:mainGethLogsFile.path toPath:mainLogsFile.path error:nil];

    [SSZipArchive createZipFileAtPath:zipFile.path withContentsOfDirectory:logsFolderName.path];
    [fileManager removeItemAtPath:logsFolderName.path error:nil];

    callback(@[zipFile.absoluteString]);
}

RCT_EXPORT_METHOD(exportLogs:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"exportLogs() method called");
#endif
    NSString *result = StatusgoExportNodeLogs();
    callback(@[result]);
}

RCT_EXPORT_METHOD(addPeer:(NSString *)enode
                  callback:(RCTResponseSenderBlock)callback) {
  NSString *result = StatusgoAddPeer(enode);
  callback(@[result]);
#if DEBUG
  NSLog(@"AddPeer() method called");
#endif
}

RCT_EXPORT_METHOD(deleteMultiaccount:(NSString *)keyUID
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"DeleteMultiaccount() method called");
#endif
    NSURL *multiaccountKeystoreDir = [self getKeyStoreDir:keyUID];
    NSString *result = StatusgoDeleteMultiaccount(keyUID, multiaccountKeystoreDir.path);
    callback(@[result]);
}

RCT_EXPORT_METHOD(deleteImportedKey:(NSString *)keyUID
                  address:(NSString *)address
                  password:(NSString *)password
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"DeleteImportedKey() method called");
#endif
    NSURL *multiaccountKeystoreDir = [self getKeyStoreDir:keyUID];
    NSString *result = StatusgoDeleteImportedKey(address, password, multiaccountKeystoreDir.path);
    callback(@[result]);
}

RCT_EXPORT_METHOD(multiAccountGenerateAndDeriveAddresses:(NSString *)json
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"MultiAccountGenerateAndDeriveAddresses() method called");
#endif
    NSString *result = StatusgoMultiAccountGenerateAndDeriveAddresses(json);
    callback(@[result]);
}

RCT_EXPORT_METHOD(multiAccountStoreAccount:(NSString *)json
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"MultiAccountStoreAccount() method called");
#endif
    NSString *result = StatusgoMultiAccountStoreAccount(json);
    callback(@[result]);
}

RCT_EXPORT_METHOD(multiAccountLoadAccount:(NSString *)json
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"MultiAccountLoadAccount() method called");
#endif
    NSString *result = StatusgoMultiAccountLoadAccount(json);
    callback(@[result]);
}

RCT_EXPORT_METHOD(multiAccountReset:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"MultiAccountReset() method called");
#endif
    NSString *result = StatusgoMultiAccountReset();
    callback(@[result]);
}

RCT_EXPORT_METHOD(multiAccountStoreDerived:(NSString *)json
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"MultiAccountStoreDerived() method called");
#endif
    NSString *result = StatusgoMultiAccountStoreDerivedAccounts(json);
    callback(@[result]);
}

RCT_EXPORT_METHOD(multiAccountImportPrivateKey:(NSString *)json
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"MultiAccountImportPrivateKey() method called");
#endif
    NSString *result = StatusgoMultiAccountImportPrivateKey(json);
    callback(@[result]);
}

RCT_EXPORT_METHOD(hashTransaction:(NSString *)txArgsJSON
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"HashTransaction() method called");
#endif
    NSString *result = StatusgoHashTransaction(txArgsJSON);
    callback(@[result]);
}

RCT_EXPORT_METHOD(hashMessage:(NSString *)message
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"hashMessage() method called");
#endif
    NSString *result = StatusgoHashMessage(message);
    callback(@[result]);
}

RCT_EXPORT_METHOD(getConnectionStringForBootstrappingAnotherDevice:(NSString *)configJSON
                  callback:(RCTResponseSenderBlock)callback) {

    NSData *configData = [configJSON dataUsingEncoding:NSUTF8StringEncoding];
    NSDictionary *configDict = [NSJSONSerialization JSONObjectWithData:configData options:NSJSONReadingMutableContainers error:nil];
    NSString *keyUID = [configDict objectForKey:@"keyUID"];
    NSURL *multiaccountKeystoreDir = [self getKeyStoreDir:keyUID];
    NSString *keystoreDir = multiaccountKeystoreDir.path;

    [configDict setValue:keystoreDir forKey:@"keystorePath"];
    NSString *modifiedConfigJSON = [configDict bv_jsonStringWithPrettyPrint:NO];

    NSString *result = StatusgoGetConnectionStringForBootstrappingAnotherDevice(modifiedConfigJSON);
    callback(@[result]);
}

RCT_EXPORT_METHOD(inputConnectionStringForBootstrapping:(NSString *)cs
                  configJSON:(NSString *)configJSON
                  callback:(RCTResponseSenderBlock)callback) {

    NSData *configData = [configJSON dataUsingEncoding:NSUTF8StringEncoding];
    NSDictionary *configDict = [NSJSONSerialization JSONObjectWithData:configData options:NSJSONReadingMutableContainers error:nil];
    NSFileManager *fileManager = [NSFileManager defaultManager];
    NSURL *rootUrl =[[fileManager URLsForDirectory:NSLibraryDirectory inDomains:NSUserDomainMask] lastObject];
    NSURL *multiaccountKeystoreDir = [rootUrl URLByAppendingPathComponent:@"keystore"];
    NSString *keystoreDir = multiaccountKeystoreDir.path;

    [configDict setValue:keystoreDir forKey:@"keystorePath"];
    NSString *modifiedConfigJSON = [configDict bv_jsonStringWithPrettyPrint:NO];

    NSString *result = StatusgoInputConnectionStringForBootstrapping(cs,modifiedConfigJSON);
    callback(@[result]);
}

RCT_EXPORT_METHOD(multiformatSerializePublicKey:(NSString *)multiCodecKey
                  base58btc:(NSString *)base58btc
                  callback:(RCTResponseSenderBlock)callback) {
    NSString *result = StatusgoMultiformatSerializePublicKey(multiCodecKey,base58btc);
    callback(@[result]);
}

RCT_EXPORT_METHOD(multiformatDeserializePublicKey:(NSString *)multiCodecKey
                  base58btc:(NSString *)base58btc
                  callback:(RCTResponseSenderBlock)callback) {
    NSString *result = StatusgoMultiformatDeserializePublicKey(multiCodecKey,base58btc);
    callback(@[result]);
}

RCT_EXPORT_METHOD(decompressPublicKey:(NSString *)multiCodecKey
                  callback:(RCTResponseSenderBlock)callback) {
    NSString *result = StatusgoDecompressPublicKey(multiCodecKey);
    callback(@[result]);
}

RCT_EXPORT_METHOD(compressPublicKey:(NSString *)multiCodecKey
                  callback:(RCTResponseSenderBlock)callback) {
    NSString *result = StatusgoCompressPublicKey(multiCodecKey);
    callback(@[result]);
}

RCT_EXPORT_METHOD(deserializeAndCompressKey:(NSString *)desktopKey
                  callback:(RCTResponseSenderBlock)callback) {
    NSString *result = StatusgoDeserializeAndCompressKey(desktopKey);
    callback(@[result]);
}

RCT_EXPORT_METHOD(hashTypedData:(NSString *)data
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"hashTypedData() method called");
#endif
    NSString *result = StatusgoHashTypedData(data);
    callback(@[result]);
}

RCT_EXPORT_METHOD(hashTypedDataV4:(NSString *)data
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"hashTypedDataV4() method called");
#endif
    NSString *result = StatusgoHashTypedDataV4(data);
    callback(@[result]);
}

RCT_EXPORT_METHOD(sendTransactionWithSignature:(NSString *)txArgsJSON
                  signature:(NSString *)signature
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"sendTransactionWithSignature() method called");
#endif
    NSString *result = StatusgoSendTransactionWithSignature(txArgsJSON, signature);
    callback(@[result]);
}

RCT_EXPORT_METHOD(multiAccountImportMnemonic:(NSString *)json
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"MultiAccountImportMnemonic() method called");
#endif
    NSString *result = StatusgoMultiAccountImportMnemonic(json);
    callback(@[result]);
}

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
    NSString *keystoreDir = [@"/keystore/" stringByAppendingString:keyUID];
    [configJSON setValue:keystoreDir forKey:@"KeyStoreDir"];
    [configJSON setValue:@"" forKey:@"LogDir"];
    [configJSON setValue:@"geth.log" forKey:@"LogFile"];

    NSString *resultingConfig = [configJSON bv_jsonStringWithPrettyPrint:NO];
    NSLog(@"node config %@", resultingConfig);

    if(![fileManager fileExistsAtPath:absDataDir]) {
        [fileManager createDirectoryAtPath:absDataDir
               withIntermediateDirectories:YES attributes:nil error:nil];
    }

    NSLog(@"logUrlPath %@ rootDir %@", @"geth.log", rootUrl.path);
    NSURL *absLogUrl = [absDataDirUrl URLByAppendingPathComponent:@"geth.log"];
    if(![fileManager fileExistsAtPath:absLogUrl.path]) {
        NSMutableDictionary *dict = [[NSMutableDictionary alloc] init];
        [dict setObject:[NSNumber numberWithInt:511] forKey:NSFilePosixPermissions];
        [fileManager createFileAtPath:absLogUrl.path contents:nil attributes:dict];
    }

    return resultingConfig;

}


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

RCT_EXPORT_METHOD(saveAccountAndLogin:(NSString *)multiaccountData
                  password:(NSString *)password
                  settings:(NSString *)settings
                  config:(NSString *)config
                  accountsData:(NSString *)accountsData) {
#if DEBUG
    NSLog(@"SaveAccountAndLogin() method called");
#endif
    [self getExportDbFilePath];
    NSString *keyUID = [self getKeyUID:multiaccountData];
    NSString *finalConfig = [self prepareDirAndUpdateConfig:config
                                                 withKeyUID:keyUID];
    NSString *result = StatusgoSaveAccountAndLogin(multiaccountData, password, settings, finalConfig, accountsData);
    NSLog(@"%@", result);
}

RCT_EXPORT_METHOD(saveAccountAndLoginWithKeycard:(NSString *)multiaccountData
                  password:(NSString *)password
                  settings:(NSString *)settings
                  config:(NSString *)config
                  accountsData:(NSString *)accountsData
                  chatKey:(NSString *)chatKey) {
#if DEBUG
    NSLog(@"SaveAccountAndLoginWithKeycard() method called");
#endif
    [self getExportDbFilePath];
    NSString *keyUID = [self getKeyUID:multiaccountData];
    NSString *finalConfig = [self prepareDirAndUpdateConfig:config
                                                 withKeyUID:keyUID];
    NSString *result = StatusgoSaveAccountAndLoginWithKeycard(multiaccountData, password, settings, finalConfig, accountsData, chatKey);
    NSLog(@"%@", result);
}

- (NSString *) getExportDbFilePath {
    NSString *filePath = [NSTemporaryDirectory() stringByAppendingPathComponent:@"export.db"];
    NSFileManager *fileManager = [NSFileManager defaultManager];

    if ([fileManager fileExistsAtPath:filePath]) {
        [fileManager removeItemAtPath:filePath error:nil];
    }

    return filePath;
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

RCT_EXPORT_METHOD(login:(NSString *)accountData
                  password:(NSString *)password) {
#if DEBUG
    NSLog(@"Login() method called");
#endif
    [self getExportDbFilePath];
    [self migrateKeystore:accountData password:password];
    NSString *result = StatusgoLogin(accountData, password);
    NSLog(@"%@", result);
}

RCT_EXPORT_METHOD(loginWithConfig:(NSString *)accountData
                  password:(NSString *)password
                  configJSON:(NSString *)configJSON) {
#if DEBUG
    NSLog(@"LoginWithConfig() method called");
#endif
    [self getExportDbFilePath];
    [self migrateKeystore:accountData password:password];
    NSString *result = StatusgoLoginWithConfig(accountData, password, configJSON);
    NSLog(@"%@", result);
}

RCT_EXPORT_METHOD(loginWithKeycard:(NSString *)accountData
                  password:(NSString *)password
                  chatKey:(NSString *)chatKey) {
#if DEBUG
    NSLog(@"LoginWithKeycard() method called");
#endif
    [self getExportDbFilePath];
    [self migrateKeystore:accountData password:password];

    NSString *result = StatusgoLoginWithKeycard(accountData, password, chatKey);

    NSLog(@"%@", result);
}

RCT_EXPORT_METHOD(logout) {
#if DEBUG
    NSLog(@"Logout() method called");
#endif
    NSString *result = StatusgoLogout();

    NSLog(@"%@", result);
}

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

RCT_EXPORT_METHOD(verifyDatabasePassword:(NSString *)keyUID
                  password:(NSString *)password
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"VerifyDatabasePassword() method called");
#endif
    NSString *result = StatusgoVerifyDatabasePassword(keyUID, password);
    callback(@[result]);
}

RCT_EXPORT_METHOD(reEncryptDbAndKeystore:(NSString *)keyUID
                  currentPassword:(NSString *)currentPassword
                  newPassword:(NSString *)newPassword
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"reEncryptDbAndKeystore() method called");
#endif
    // changes password and re-encrypts keystore
    NSString *result = StatusgoChangeDatabasePassword(keyUID, currentPassword, newPassword);
    callback(@[result]);
}

RCT_EXPORT_METHOD(convertToKeycardAccount:(NSString *)keyUID
                  accountData:(NSString *)accountData
                  settings:(NSString *)settings
                  currentPassword:(NSString *)currentPassword
                  newPassword:(NSString *)newPassword
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"convertToKeycardAccount() method called");
#endif
    NSURL *multiaccountKeystoreDir = [self getKeyStoreDir:keyUID];
    StatusgoInitKeystore(multiaccountKeystoreDir.path);
    NSString *result = StatusgoConvertToKeycardAccount(accountData, settings, currentPassword, newPassword);
    callback(@[result]);
}


#pragma mark - SendTransaction

RCT_EXPORT_METHOD(sendTransaction:(NSString *)txArgsJSON
                  password:(NSString *)password
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"SendTransaction() method called");
#endif
    NSString *result = StatusgoSendTransaction(txArgsJSON, password);
    callback(@[result]);
}


#pragma mark - SignMessage

RCT_EXPORT_METHOD(signMessage:(NSString *)message
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"SignMessage() method called");
#endif
    NSString *result = StatusgoSignMessage(message);
    callback(@[result]);
}


#pragma mark - Recover

RCT_EXPORT_METHOD(recover:(NSString *)message
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"Recover() method called");
#endif
    NSString *result = StatusgoRecover(message);
    callback(@[result]);
}


#pragma mark - SignTypedData

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


#pragma mark - SignTypedDataV4

RCT_EXPORT_METHOD(signTypedDataV4:(NSString *)data
                  account:(NSString *)account
                  password:(NSString *)password
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"SignTypedDataV4() method called");
#endif
    NSString *result = StatusgoSignTypedDataV4(data, account, password);
    callback(@[result]);
}


#pragma mark - SignGroupMembership

RCT_EXPORT_METHOD(signGroupMembership:(NSString *)content
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"SignGroupMembership() method called");
#endif
    NSString *result = StatusgoSignGroupMembership(content);
    callback(@[result]);
}


#pragma mark - ExtractGroupMembershipSignatures

RCT_EXPORT_METHOD(extractGroupMembershipSignatures:(NSString *)content
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"ExtractGroupMembershipSignatures() method called");
#endif
    NSString *result = StatusgoExtractGroupMembershipSignatures(content);
    callback(@[result]);
}


#pragma mark - GetNodeConfig

RCT_EXPORT_METHOD(getNodeConfig:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"GetNodeConfig() method called");
#endif
    NSString *result = StatusgoGetNodeConfig();
    callback(@[result]);
}



#pragma mark - only android methods

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

RCT_EXPORT_BLOCKING_SYNCHRONOUS_METHOD(encodeTransfer:(NSString *)to
                                       value:(NSString *)value) {
  return StatusgoEncodeTransfer(to,value);
}

RCT_EXPORT_BLOCKING_SYNCHRONOUS_METHOD(encodeFunctionCall:(NSString *)method
                                       paramsJSON:(NSString *)paramsJSON) {
  return StatusgoEncodeFunctionCall(method,paramsJSON);
}

RCT_EXPORT_BLOCKING_SYNCHRONOUS_METHOD(decodeParameters:(NSString *)decodeParamJSON) {
  return StatusgoDecodeParameters(decodeParamJSON);
}

RCT_EXPORT_BLOCKING_SYNCHRONOUS_METHOD(hexToNumber:(NSString *)hex) {
  return StatusgoHexToNumber(hex);
}

RCT_EXPORT_BLOCKING_SYNCHRONOUS_METHOD(numberToHex:(NSString *)numString) {
  return StatusgoNumberToHex(numString);
}

RCT_EXPORT_BLOCKING_SYNCHRONOUS_METHOD(sha3:(NSString *)str) {
  return StatusgoSha3(str);
}

RCT_EXPORT_BLOCKING_SYNCHRONOUS_METHOD(utf8ToHex:(NSString *)str) {
  return StatusgoUtf8ToHex(str);
}

RCT_EXPORT_BLOCKING_SYNCHRONOUS_METHOD(hexToUtf8:(NSString *)str) {
  return StatusgoHexToUtf8(str);
}

RCT_EXPORT_BLOCKING_SYNCHRONOUS_METHOD(checkAddressChecksum:(NSString *)address) {
  return StatusgoCheckAddressChecksum(address);
}

RCT_EXPORT_BLOCKING_SYNCHRONOUS_METHOD(isAddress:(NSString *)address) {
  return StatusgoIsAddress(address);
}

RCT_EXPORT_BLOCKING_SYNCHRONOUS_METHOD(toChecksumAddress:(NSString *)address) {
  return StatusgoToChecksumAddress(address);
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

RCT_EXPORT_METHOD(stopLocalNotifications) {
#if DEBUG
    NSLog(@"StopLocalNotifications() method called");
#endif
StatusgoStopLocalNotifications();
}

RCT_EXPORT_METHOD(startLocalNotifications) {
#if DEBUG
    NSLog(@"StartLocalNotifications() method called");
#endif
StatusgoStartLocalNotifications();
}

RCT_EXPORT_METHOD(exportUnencryptedDatabase:(NSString *)accountData
                  password:(NSString *)password
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"exportUnencryptedDatabase() method called");
#endif

    NSString *filePath = [self getExportDbFilePath];
    StatusgoExportUnencryptedDatabase(accountData, password, filePath);

    callback(@[filePath]);
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

#pragma mark - deviceinfo

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
