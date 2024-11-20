#import "Utils.h"
#import "React/RCTBridge.h"
#import "React/RCTEventDispatcher.h"
#import "Statusgo.h"
#import "Utils.h"
#import "StatusBackendClient.h"

@implementation Utils

RCT_EXPORT_MODULE();

#pragma mark - Private Methods

+ (NSURL *)getRootUrl {
    NSFileManager *fileManager = [NSFileManager defaultManager];
    NSURL *rootUrl;
    
    StatusBackendClient *client = [StatusBackendClient sharedInstance];
    if (client.serverEnabled && client.rootDataDir) {
        rootUrl = [NSURL fileURLWithPath:client.rootDataDir];
    } else {
        rootUrl = [[fileManager URLsForDirectory:NSLibraryDirectory inDomains:NSUserDomainMask] lastObject];
    }
    
    return rootUrl;
}

+ (NSString *)jsonStringWithPrettyPrint:(BOOL)prettyPrint fromDictionary:(NSDictionary *)dictionary {
    NSError *error;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:dictionary
                                                       options:(NSJSONWritingOptions)(prettyPrint ? NSJSONWritingPrettyPrinted : 0)
                                                         error:&error];

    if (!jsonData) {
        NSLog(@"jsonStringWithPrettyPrint: error: %@", error.localizedDescription);
        return @"{}";
    } else {
        return [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    }
}

+ (NSString *)jsonStringWithPrettyPrint:(BOOL)prettyPrint fromArray:(NSArray *)array {
    NSError *error;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:array
                                                       options:(NSJSONWritingOptions)(prettyPrint ? NSJSONWritingPrettyPrinted : 0)
                                                         error:&error];

    if (!jsonData) {
        NSLog(@"jsonStringWithPrettyPrint: error: %@", error.localizedDescription);
        return @"[]";
    } else {
        return [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    }
}

+ (NSURL *)getKeyStoreDirForKeyUID:(NSString *)keyUID {
    NSFileManager *fileManager = [NSFileManager defaultManager];
    NSURL *rootUrl = [self getRootUrl];

    NSURL *oldKeystoreDir = [rootUrl URLByAppendingPathComponent:@"keystore"];
    NSURL *multiaccountKeystoreDir = [oldKeystoreDir URLByAppendingPathComponent:keyUID];

    return multiaccountKeystoreDir;
}

+ (NSString *) getKeyUID:(NSString *)jsonString {
    NSData *data = [jsonString dataUsingEncoding:NSUTF8StringEncoding];
    NSDictionary *json = [NSJSONSerialization
            JSONObjectWithData:data
                       options:NSJSONReadingMutableContainers
                         error:nil];

    return [json valueForKey:@"key-uid"];
}

+ (NSString *) getExportDbFilePath {
    StatusBackendClient *client = [StatusBackendClient sharedInstance];
    if (client.serverEnabled && client.rootDataDir) {
        return [client.rootDataDir stringByAppendingPathComponent:@"export.db"];
    }

    NSString *filePath = [NSTemporaryDirectory() stringByAppendingPathComponent:@"export.db"];
    NSFileManager *fileManager = [NSFileManager defaultManager];

    if ([fileManager fileExistsAtPath:filePath]) {
        [fileManager removeItemAtPath:filePath error:nil];
    }

    return filePath;
}

+ (void) migrateKeystore:(NSString *)accountData
                password:(NSString *)password {
    NSFileManager *fileManager = [NSFileManager defaultManager];
    NSURL *rootUrl =[self getRootUrl];

    NSData *jsonData = [accountData dataUsingEncoding:NSUTF8StringEncoding];
    NSError *error;
    NSDictionary *accountJson = [NSJSONSerialization JSONObjectWithData:jsonData options:0 error:&error];
    if (error) {
        NSLog(@"Error parsing accountData: %@", error);
        return;
    }

    NSString *keyUID = [self getKeyUID:accountData];
    NSURL *oldKeystoreDir = [rootUrl URLByAppendingPathComponent:@"keystore"];
    NSURL *multiaccountKeystoreDir = [self getKeyStoreDirForKeyUID:keyUID];

    NSArray *keys = [fileManager contentsOfDirectoryAtPath:multiaccountKeystoreDir.path error:nil];
    if (keys.count == 0) {
        NSDictionary *params = @{
            @"account": accountJson,
            @"password": password,
            @"oldDir": oldKeystoreDir.path,
            @"newDir": multiaccountKeystoreDir.path
        };
        NSData *paramsJsonData = [NSJSONSerialization dataWithJSONObject:params options:0 error:&error];
        if (error) {
            NSLog(@"Error creating params JSON: %@", error);
            return;
        }
        NSString *jsonString = [[NSString alloc] initWithData:paramsJsonData encoding:NSUTF8StringEncoding];
        
        NSString *migrationResult = StatusgoMigrateKeyStoreDirV2(jsonString);
        NSLog(@"keystore migration result %@", migrationResult);

        NSString *initKeystoreResult = StatusgoInitKeystore(multiaccountKeystoreDir.path);
        NSLog(@"InitKeyStore result %@", initKeystoreResult);
    }
}

RCT_EXPORT_BLOCKING_SYNCHRONOUS_METHOD(backupDisabledDataDir) {
    StatusBackendClient *client = [StatusBackendClient sharedInstance];
    if (client.serverEnabled && client.rootDataDir) {
        return client.rootDataDir;
    }
    
    NSFileManager *fileManager = [NSFileManager defaultManager];
    NSURL *rootUrl = [Utils getRootUrl];
    return rootUrl.path;
}

RCT_EXPORT_BLOCKING_SYNCHRONOUS_METHOD(keystoreDir) {
    NSFileManager *fileManager = [NSFileManager defaultManager];
    NSURL *rootUrl =[Utils getRootUrl];

    NSURL *commonKeystoreDir = [rootUrl URLByAppendingPathComponent:@"keystore"];

    return commonKeystoreDir.path;
}

RCT_EXPORT_METHOD(validateMnemonic:(NSString *)seed
    callback:(RCTResponseSenderBlock)callback) {
    #if DEBUG
        NSLog(@"validateMnemonicV2() method called");
    #endif
    NSDictionary *params = @{@"mnemonic": seed};
    NSError *error;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:params options:0 error:&error];
    if (error) {
        NSLog(@"Error creating JSON: %@", error);
        return;
    }
    NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    NSString *result = StatusgoValidateMnemonicV2(jsonString);
    callback(@[result]);
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

RCT_EXPORT_BLOCKING_SYNCHRONOUS_METHOD(validateConnectionString:(NSString *)cs) {
    return StatusgoValidateConnectionString(cs);
}

+ (void)handleStatusGoResponse:(NSString *)response source:(NSString *)source error:(NSError *)error {
    if (error) {
        NSLog(@"%@ failed: %@", source, error);
        return;
    }
    
    if ([response hasPrefix:@"{\"error\":\"\""]) {
        NSLog(@"%@ success: %@", source, response);
    } else {
        NSLog(@"%@ failed: %@", source, response);
    }
}

@end
