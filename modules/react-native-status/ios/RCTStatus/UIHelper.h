#import <sys/utsname.h>
#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>
#import "RCTLog.h"

@interface UIHelper : NSObject <RCTBridgeModule>

+ (void)addScreenshotBlock;
+ (void)removeScreenshotBlock;

@end
