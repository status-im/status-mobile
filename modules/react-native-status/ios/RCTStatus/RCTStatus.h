#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>
#import "RCTLog.h"

@interface Status : NSObject <RCTBridgeModule>
+ (void)signalEvent:(const char *)signal;
+ (void)jailEvent:(NSString *)chatId
             data:(NSString *)data;
+ (BOOL)JSCEnabled;
@end
