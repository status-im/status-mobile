#import <sys/utsname.h>
#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import <React/RCTBridgeModule.h>
#import "RCTLog.h"

@interface MailManager : NSObject <RCTBridgeModule, MFMailComposeViewControllerDelegate>

@end
