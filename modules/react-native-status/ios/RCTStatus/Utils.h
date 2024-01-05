#import <Foundation/Foundation.h>

@interface Utils : NSObject

+ (NSString *)jsonStringWithPrettyPrint:(BOOL)prettyPrint fromDictionary:(NSDictionary *)dictionary;
+ (NSString *)jsonStringWithPrettyPrint:(BOOL)prettyPrint fromArray:(NSArray *)array;
+ (NSURL *)getKeyStoreDirForKeyUID:(NSString *)keyUID;
+ (NSString *)getExportDbFilePath;
+ (NSString *)getKeyUID:(NSString *)jsonString;
+ (void)migrateKeystore:(NSString *)accountData password:(NSString *)password;

@end
