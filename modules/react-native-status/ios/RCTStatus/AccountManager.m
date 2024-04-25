#import "AccountManager.h"
#import "React/RCTBridge.h"
#import "React/RCTEventDispatcher.h"
#import "Statusgo.h"
#import "Utils.h"

@implementation AccountManager

RCT_EXPORT_MODULE();

RCT_EXPORT_METHOD(createAccountAndLogin:(NSString *)request) {
#if DEBUG
    NSLog(@"createAccountAndLogin() method called");
#endif
    StatusgoCreateAccountAndLogin(request);
}

RCT_EXPORT_METHOD(restoreAccountAndLogin:(NSString *)request) {
#if DEBUG
    NSLog(@"restoreAccountAndLogin() method called");
#endif
    StatusgoRestoreAccountAndLogin(request);
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
    if (relativeDataDir == nil) {
        relativeDataDir = @"";
    }
    NSString *absDataDir = [rootUrl.path stringByAppendingString:relativeDataDir];
    NSURL *absDataDirUrl = [NSURL fileURLWithPath:absDataDir];
    NSString *keystoreDir = [@"/keystore/" stringByAppendingString:keyUID];
    [configJSON setValue:keystoreDir forKey:@"KeyStoreDir"];
    [configJSON setValue:@"" forKey:@"LogDir"];
    [configJSON setValue:@"geth.log" forKey:@"LogFile"];
    NSString *resultingConfig = [Utils jsonStringWithPrettyPrint:NO fromDictionary:configJSON];

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

RCT_EXPORT_METHOD(deleteMultiaccount:(NSString *)keyUID
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"DeleteMultiaccount() method called");
#endif
    NSURL *multiaccountKeystoreDir = [Utils getKeyStoreDirForKeyUID:keyUID];
    NSString *result = StatusgoDeleteMultiaccount(keyUID, multiaccountKeystoreDir.path);
    callback(@[result]);
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

RCT_EXPORT_METHOD(saveAccountAndLoginWithKeycard:(NSString *)multiaccountData
        password:(NSString *)password
        settings:(NSString *)settings
        config:(NSString *)config
        accountsData:(NSString *)accountsData
        chatKey:(NSString *)chatKey) {
#if DEBUG
    NSLog(@"SaveAccountAndLoginWithKeycard() method called");
#endif
    [Utils getExportDbFilePath];
    NSString *keyUID = [Utils getKeyUID:multiaccountData];
    NSString *finalConfig = [self prepareDirAndUpdateConfig:config
                                                 withKeyUID:keyUID];
    NSString *result = StatusgoSaveAccountAndLoginWithKeycard(multiaccountData, password, settings, finalConfig, accountsData, chatKey);
    NSLog(@"%@", result);
}

RCT_EXPORT_METHOD(loginWithKeycard:(NSString *)accountData
        password:(NSString *)password
        chatKey:(NSString *)chatKey
        nodeConfigJSON:(NSString *)nodeConfigJSON) {
#if DEBUG
    NSLog(@"LoginWithKeycard() method called");
#endif
    [Utils getExportDbFilePath];
    [Utils migrateKeystore:accountData password:password];

    NSString *result = StatusgoLoginWithKeycard(accountData, password, chatKey, nodeConfigJSON);

    NSLog(@"%@", result);
}

RCT_EXPORT_METHOD(loginWithConfig:(NSString *)accountData
        password:(NSString *)password
        configJSON:(NSString *)configJSON) {
#if DEBUG
    NSLog(@"LoginWithConfig() method called");
#endif
    [Utils getExportDbFilePath];
    [Utils migrateKeystore:accountData password:password];
    NSString *result = StatusgoLoginWithConfig(accountData, password, configJSON);
    NSLog(@"%@", result);
}

RCT_EXPORT_METHOD(loginAccount:(NSString *)request) {
#if DEBUG
    NSLog(@"LoginAccount() method called");
#endif
    NSString *result = StatusgoLoginAccount(request);
    NSLog(@"%@", result);
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

RCT_EXPORT_METHOD(logout) {
#if DEBUG
        NSLog(@"Logout() method called");
#endif
        NSString *result = StatusgoLogout();

        NSLog(@"%@", result);
}

RCT_EXPORT_METHOD(getRandomMnemonic:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"GetRandomMnemonic() method called");
#endif
    NSString *result = StatusgoGetRandomMnemonic();
    callback(@[result]);
}

RCT_EXPORT_METHOD(createAccountFromMnemonicAndDeriveAccountsForPaths:(NSString *)mnemonic callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"createAccountFromMnemonicAndDeriveAccountsForPaths() method called");
#endif
    NSString *result = StatusgoCreateAccountFromMnemonicAndDeriveAccountsForPaths(mnemonic);
    callback(@[result]);
}

@end
