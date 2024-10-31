#import <sys/utsname.h>
#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>
#import "Statusgo.h"
#import "RCTLog.h"

@interface Utils : NSObject <RCTBridgeModule>

+ (NSURL *)getRootUrl;
+ (NSString *)jsonStringWithPrettyPrint:(BOOL)prettyPrint fromDictionary:(NSDictionary *)dictionary;
+ (NSString *)jsonStringWithPrettyPrint:(BOOL)prettyPrint fromArray:(NSArray *)array;
+ (NSURL *)getKeyStoreDirForKeyUID:(NSString *)keyUID;
+ (NSString *)getExportDbFilePath;
+ (NSString *)getKeyUID:(NSString *)jsonString;
+ (void)migrateKeystore:(NSString *)accountData password:(NSString *)password;
+ (void)handleStatusGoResponse:(NSString *)response source:(NSString *)source error:(NSError *)error;

@end
