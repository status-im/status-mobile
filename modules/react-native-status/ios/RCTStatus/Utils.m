#import "Utils.h"
#import "React/RCTBridge.h"
#import "React/RCTEventDispatcher.h"
#import "Statusgo.h"
#import "Utils.h"

@implementation Utils

RCT_EXPORT_MODULE();

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
    NSURL *rootUrl = [[fileManager URLsForDirectory:NSLibraryDirectory inDomains:NSUserDomainMask] lastObject];

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
    NSURL *rootUrl =[[fileManager
            URLsForDirectory:NSLibraryDirectory inDomains:NSUserDomainMask]
            lastObject];

    NSURL *keyUID = [self getKeyStoreDirForKeyUID:accountData];
    NSURL *oldKeystoreDir = [rootUrl URLByAppendingPathComponent:@"keystore"];
    NSURL *multiaccountKeystoreDir = [self getKeyStoreDirForKeyUID:keyUID.path];

    NSArray *keys = [fileManager contentsOfDirectoryAtPath:multiaccountKeystoreDir.path error:nil];
    if (keys.count == 0) {
        NSString *migrationResult = StatusgoMigrateKeyStoreDir(accountData, password, oldKeystoreDir.path, multiaccountKeystoreDir.path);
        NSLog(@"keystore migration result %@", migrationResult);

        NSString *initKeystoreResult = StatusgoInitKeystore(multiaccountKeystoreDir.path);
        NSLog(@"InitKeyStore result %@", initKeystoreResult);
    }
}

RCT_EXPORT_BLOCKING_SYNCHRONOUS_METHOD(backupDisabledDataDir) {
    NSFileManager *fileManager = [NSFileManager defaultManager];
    NSURL *rootUrl =[[fileManager
            URLsForDirectory:NSLibraryDirectory inDomains:NSUserDomainMask]
            lastObject];
    return rootUrl.path;
}

RCT_EXPORT_BLOCKING_SYNCHRONOUS_METHOD(keystoreDir) {
    NSFileManager *fileManager = [NSFileManager defaultManager];
    NSURL *rootUrl =[[fileManager
            URLsForDirectory:NSLibraryDirectory inDomains:NSUserDomainMask]
            lastObject];

    NSURL *commonKeystoreDir = [rootUrl URLByAppendingPathComponent:@"keystore"];

    return commonKeystoreDir.path;
}

RCT_EXPORT_METHOD(validateMnemonic:(NSString *)seed
    callback:(RCTResponseSenderBlock)callback) {
    #if DEBUG
        NSLog(@"validateMnemonic() method called");
    #endif
    NSString *result = StatusgoValidateMnemonic(seed);
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

@end
