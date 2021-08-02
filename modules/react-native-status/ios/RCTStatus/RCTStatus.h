#import <sys/utsname.h>
#import <Foundation/Foundation.h>
#import "JSIStatus.hpp"
#import <React/RCTBridge+Private.h>
#import <React/RCTUtils.h>
#import "Statusgo.h"
#import "RCTLog.h"

@interface Status : NSObject <RCTBridgeModule, StatusgoSignalHandler>
@property (nonatomic, assign) BOOL setBridgeOnMainQueue;
- (void)handleSignal:(NSString *)signal;
@end
