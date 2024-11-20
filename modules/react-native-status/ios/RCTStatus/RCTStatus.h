#import <sys/utsname.h>
#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>
#import "Statusgo.h"
#import "RCTLog.h"

@interface Status : NSObject <RCTBridgeModule, StatusgoSignalHandler>
+ (Status *)sharedInstance;
- (void)handleSignal:(NSString *)signal;
@end
