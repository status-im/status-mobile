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
