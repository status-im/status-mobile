#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>
#import "Statusgo/Statusgo.h"
#import "RCTLog.h"

@interface Status : NSObject <RCTBridgeModule, StatusgoSignalHandler>
- (void)handleSignal:(NSString *)signal;
@end
