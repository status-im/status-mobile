#import "AccountManager.h"
#import "React/RCTBridge.h"
#import "React/RCTEventDispatcher.h"
#import "Statusgo.h"
#import "Utils.h"
#import "StatusBackendClient.h"

@implementation AccountManager

RCT_EXPORT_MODULE();

RCT_EXPORT_METHOD(createAccountAndLogin:(NSString *)request) {
#if DEBUG
    NSLog(@"createAccountAndLogin() method called");
#endif
    [StatusBackendClient executeStatusGoRequest:@"CreateAccountAndLogin" 
                                         body:request 
                            statusgoFunction:^NSString *{
        return StatusgoCreateAccountAndLogin(request);
    }];
}

RCT_EXPORT_METHOD(restoreAccountAndLogin:(NSString *)request) {
#if DEBUG
    NSLog(@"restoreAccountAndLogin() method called");
#endif
    [StatusBackendClient executeStatusGoRequest:@"RestoreAccountAndLogin" 
                                         body:request 
                            statusgoFunction:^NSString *{
        return StatusgoRestoreAccountAndLogin(request);
    }];
}

-(NSString *) prepareDirAndUpdateConfig:(NSString *)config
                             withKeyUID:(NSString *)keyUID {
    NSFileManager *fileManager = [NSFileManager defaultManager];
    NSError *error = nil;
    NSURL *rootUrl =[Utils getRootUrl];
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
    NSDictionary *params = @{
        @"keyUID": keyUID,
        @"keyStoreDir": multiaccountKeystoreDir.path
    };
    NSError *error;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:params options:0 error:&error];
    if (error) {
        NSLog(@"Error creating JSON: %@", error);
        return;
    }
    NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    
    [StatusBackendClient executeStatusGoRequestWithCallback:@"DeleteMultiaccountV2"
                                                     body:jsonString
                                        statusgoFunction:^NSString *{
        return StatusgoDeleteMultiaccountV2(jsonString);
    }
                                                 callback:callback];
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
    NSLog(@"loginAccount() method called");
#endif
    [StatusBackendClient executeStatusGoRequest:@"LoginAccount" 
                                         body:request 
                            statusgoFunction:^NSString *{
        return StatusgoLoginAccount(request);
    }];
}

RCT_EXPORT_METHOD(verify:(NSString *)address
        password:(NSString *)password
        callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"VerifyAccountPasswordV2() method called");
#endif
    NSURL *rootUrl = [Utils getRootUrl];
    NSString *keystorePath = [rootUrl.path stringByAppendingPathComponent:@"keystore"];
    
    NSDictionary *params = @{
        @"keyStoreDir": keystorePath,
        @"address": address,
        @"password": password
    };
    NSError *error;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:params options:0 error:&error];
    if (error) {
        NSLog(@"Error creating JSON: %@", error);
        return;
    }
    NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    
    [StatusBackendClient executeStatusGoRequestWithCallback:@"VerifyAccountPasswordV2"
                                                     body:jsonString
                                        statusgoFunction:^NSString *{
        return StatusgoVerifyAccountPasswordV2(jsonString);
    }
                                                 callback:callback];
}

RCT_EXPORT_METHOD(verifyDatabasePassword:(NSString *)keyUID
        password:(NSString *)password
        callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"VerifyDatabasePasswordV2() method called");
#endif
    NSDictionary *params = @{
        @"keyUID": keyUID,
        @"password": password
    };
    NSError *error;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:params options:0 error:&error];
    if (error) {
        NSLog(@"Error creating JSON: %@", error);
        return;
    }
    NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    
    [StatusBackendClient executeStatusGoRequestWithCallback:@"VerifyDatabasePasswordV2"
                                                     body:jsonString
                                        statusgoFunction:^NSString *{
        return StatusgoVerifyDatabasePasswordV2(jsonString);
    }
                                                 callback:callback];
}

RCT_EXPORT_METHOD(initializeApplication:(NSString *)request
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"initializeApplication() method called");
#endif
    [StatusBackendClient executeStatusGoRequestWithCallback:@"InitializeApplication"
                                                     body:request
                                        statusgoFunction:^NSString *{
        return StatusgoInitializeApplication(request);
    }
                                                 callback:callback];
}

RCT_EXPORT_METHOD(acceptTerms:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"acceptTerms() method called");
#endif
    [StatusBackendClient executeStatusGoRequestWithCallback:@"AcceptTerms"
                                                     body:@""
                                        statusgoFunction:^NSString *{
        return StatusgoAcceptTerms();
    }
                                                 callback:callback];
}

RCT_EXPORT_METHOD(openAccounts:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"OpenAccounts() method called");
#endif
    NSURL *rootUrl =[Utils getRootUrl];
    NSString *result = StatusgoOpenAccounts(rootUrl.path);
    callback(@[result]);
}

RCT_EXPORT_METHOD(logout) {
#if DEBUG
    NSLog(@"Logout() method called");
#endif
    [StatusBackendClient executeStatusGoRequest:@"Logout" 
                                         body:@"" 
                            statusgoFunction:^NSString *{
        return StatusgoLogout();
    }];
}

RCT_EXPORT_METHOD(multiAccountStoreAccount:(NSString *)json
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"MultiAccountStoreAccount() method called");
#endif
    [StatusBackendClient executeStatusGoRequestWithCallback:@"MultiAccountStoreAccount"
                                                     body:json
                                        statusgoFunction:^NSString *{
        return StatusgoMultiAccountStoreAccount(json);
    }
                                                 callback:callback];
}

RCT_EXPORT_METHOD(multiAccountLoadAccount:(NSString *)json
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"MultiAccountLoadAccount() method called");
#endif
    [StatusBackendClient executeStatusGoRequestWithCallback:@"MultiAccountLoadAccount"
                                                     body:json
                                        statusgoFunction:^NSString *{
        return StatusgoMultiAccountLoadAccount(json);
    }
                                                 callback:callback];
}

RCT_EXPORT_METHOD(multiAccountDeriveAddresses:(NSString *)json
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"multiAccountDeriveAddresses() method called");
#endif
    [StatusBackendClient executeStatusGoRequestWithCallback:@"MultiAccountDeriveAddresses"
                                                     body:json
                                        statusgoFunction:^NSString *{
        return StatusgoMultiAccountDeriveAddresses(json);
    }
                                                 callback:callback];
}

RCT_EXPORT_METHOD(multiAccountGenerateAndDeriveAddresses:(NSString *)json
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"MultiAccountGenerateAndDeriveAddresses() method called");
#endif
    [StatusBackendClient executeStatusGoRequestWithCallback:@"MultiAccountGenerateAndDeriveAddresses"
                                                     body:json
                                        statusgoFunction:^NSString *{
        return StatusgoMultiAccountGenerateAndDeriveAddresses(json);
    }
                                                 callback:callback];
}

RCT_EXPORT_METHOD(multiAccountStoreDerived:(NSString *)json
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"MultiAccountStoreDerived() method called");
#endif
    [StatusBackendClient executeStatusGoRequestWithCallback:@"MultiAccountStoreDerivedAccounts"
                                                     body:json
                                        statusgoFunction:^NSString *{
        return StatusgoMultiAccountStoreDerivedAccounts(json);
    }
                                                 callback:callback];
}

RCT_EXPORT_METHOD(multiAccountImportMnemonic:(NSString *)json
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"MultiAccountImportMnemonic() method called");
#endif
    [StatusBackendClient executeStatusGoRequestWithCallback:@"MultiAccountImportMnemonic"
                                                     body:json
                                        statusgoFunction:^NSString *{
        return StatusgoMultiAccountImportMnemonic(json);
    }
                                                 callback:callback];
}

RCT_EXPORT_METHOD(multiAccountImportPrivateKey:(NSString *)json
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"MultiAccountImportPrivateKey() method called");
#endif
    [StatusBackendClient executeStatusGoRequestWithCallback:@"MultiAccountImportPrivateKey"
                                                     body:json
                                        statusgoFunction:^NSString *{
        return StatusgoMultiAccountImportPrivateKey(json);
    }
                                                 callback:callback];
}

RCT_EXPORT_METHOD(getRandomMnemonic:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"GetRandomMnemonic() method called");
#endif
    [StatusBackendClient executeStatusGoRequestWithCallback:@"GetRandomMnemonic"
                                                     body:@""
                                        statusgoFunction:^NSString *{
        return StatusgoGetRandomMnemonic();
    }
                                                 callback:callback];
}

RCT_EXPORT_METHOD(createAccountFromMnemonicAndDeriveAccountsForPaths:(NSString *)mnemonic 
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"createAccountFromMnemonicAndDeriveAccountsForPaths() method called");
#endif
    [StatusBackendClient executeStatusGoRequestWithCallback:@"CreateAccountFromMnemonicAndDeriveAccountsForPaths"
                                                     body:mnemonic
                                        statusgoFunction:^NSString *{
        return StatusgoCreateAccountFromMnemonicAndDeriveAccountsForPaths(mnemonic);
    }
                                                 callback:callback];
}

RCT_EXPORT_METHOD(createAccountFromPrivateKey:(NSString *)json
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"createAccountFromPrivateKey() method called");
#endif
    [StatusBackendClient executeStatusGoRequestWithCallback:@"CreateAccountFromPrivateKey"
                                                     body:json
                                        statusgoFunction:^NSString *{
        return StatusgoCreateAccountFromPrivateKey(json);
    }
                                                 callback:callback];
}

@end
