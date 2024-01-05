#import "Utils.h"

@implementation Utils

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

@end
