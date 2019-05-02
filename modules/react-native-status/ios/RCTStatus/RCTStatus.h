#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>
#import "Statusgo/Statusgo.h"
#import "RCTLog.h"
#import <UIKit/UIKit.h>
#import <MessageUI/MessageUI.h>

@interface Status : UIViewController <RCTBridgeModule, StatusgoSignalHandler, MFMailComposeViewControllerDelegate>
- (void)handleSignal:(NSString *)signal;
@end
