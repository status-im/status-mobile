#import <Foundation/Foundation.h>

@interface Utils : NSObject

+ (NSString *)jsonStringWithPrettyPrint:(BOOL)prettyPrint fromDictionary:(NSDictionary *)dictionary;
+ (NSString *)jsonStringWithPrettyPrint:(BOOL)prettyPrint fromArray:(NSArray *)array;
+ (NSURL *)getKeyStoreDirForKeyUID:(NSString *)keyUID;

@end
