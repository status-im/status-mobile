#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>
#import "RCTLog.h"

@interface Status : NSObject <RCTBridgeModule>
+ (void)signalEvent:(const char *) signal;
@end
