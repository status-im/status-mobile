#import <Foundation/Foundation.h>
#import "RCTBridgeModule.h"
#import "RCTLog.h"
#import <JavaScriptCore/JavaScriptCore.h>
#import "Jail.h"

@interface Status : NSObject <RCTBridgeModule>
+ (void)signalEvent:(const char *)signal;
+ (void)jailEvent:(NSString *)chatId
             data:(NSString *)data;
+ (BOOL)JSCEnabled;
@property (nonatomic) Jail * jail;
@end
