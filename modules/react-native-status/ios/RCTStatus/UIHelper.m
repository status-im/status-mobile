#import "UIHelper.h"
#import "React/RCTBridge.h"
#import "React/RCTEventDispatcher.h"

@implementation UIHelper

RCT_EXPORT_MODULE();

#pragma mark - only android methods

UITextField *textField;
UIView *guardView;

RCT_EXPORT_METHOD(setSoftInputMode: (NSInteger) i) {
#if DEBUG
    NSLog(@"setSoftInputMode() works only on Android");
#endif
}

RCT_EXPORT_METHOD(clearCookies) {
    NSHTTPCookie *cookie;
    NSHTTPCookieStorage *storage = [NSHTTPCookieStorage sharedHTTPCookieStorage];

    for (cookie in [storage cookies]) {
        [storage deleteCookie:cookie];
    }
}

RCT_EXPORT_METHOD(clearStorageAPIs) {
    [[NSURLCache sharedURLCache] removeAllCachedResponses];

    NSString *path = [NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES) lastObject];
    NSArray *array = [[NSFileManager defaultManager] contentsOfDirectoryAtPath:path error:nil];
    for (NSString *string in array) {
        NSLog(@"Removing %@", [path stringByAppendingPathComponent:string]);
        if ([[string pathExtension] isEqualToString:@"localstorage"])
            [[NSFileManager defaultManager] removeItemAtPath:[path stringByAppendingPathComponent:string] error:nil];
    }
}

+ (void) addScreenshotBlock {
    // create new secure text field and append to the window layer to prevent screenshot
    if (textField == nil) {
        UIWindow *window = [UIApplication sharedApplication].keyWindow;

        textField = [[UITextField alloc] initWithFrame:window.frame];
        textField.translatesAutoresizingMaskIntoConstraints = NO;
        
        [textField setTextAlignment:NSTextAlignmentCenter];
        [textField setUserInteractionEnabled: NO];
        
        [window makeKeyAndVisible];
        [window.layer.superlayer addSublayer:textField.layer];

        if (textField.layer.sublayers.firstObject) {
            [textField.layer.sublayers.firstObject addSublayer: window.layer];
        }
    }
    [textField setSecureTextEntry: TRUE];
    [textField setBackgroundColor: [UIColor blackColor]];
    
    // create event listener for screen captures and add a view to prevent the app content from exposed if recorded
    NSNotificationCenter *center = [NSNotificationCenter defaultCenter];
    NSOperationQueue *mainQueue = [NSOperationQueue mainQueue];
    [center removeObserver:self
                      name:UIScreenCapturedDidChangeNotification
                    object:nil];
    [center addObserverForName:UIScreenCapturedDidChangeNotification
                        object:nil
                         queue:mainQueue
                    usingBlock:^(NSNotification *notification) {
            if (UIScreen.mainScreen.captured){
                UIWindow *window = [UIApplication sharedApplication].keyWindow;
                if (guardView == nil) {
                    guardView = [[UIView alloc]initWithFrame:window.frame];
                    guardView.backgroundColor = [UIColor blackColor];
                    guardView.alpha = 0;
                }
                   
                [window addSubview:guardView];
                [window bringSubviewToFront:guardView];
                [UIView animateWithDuration:0.5 animations:^{
                    guardView.alpha = 1;
                }];
            } else {
                [UIView animateWithDuration:0.5 animations:^{
                    guardView.alpha = 0;
                } completion:^(BOOL finished) {
                    [guardView removeFromSuperview];
                }];
            }
       }];
}

+ (void) removeScreenshotBlock {
    UIWindow *window = [UIApplication sharedApplication].keyWindow;
    if (textField != nil) {
        [textField setSecureTextEntry: FALSE];
        [textField setBackgroundColor: [UIColor clearColor]];
        CALayer *textFieldLayer = textField.layer.sublayers.firstObject;
        if ([window.layer.superlayer.sublayers containsObject:textFieldLayer]) {
            [textFieldLayer removeFromSuperlayer];
        }
    }
    
    NSNotificationCenter *center = [NSNotificationCenter defaultCenter];
    [center removeObserver:self
                      name:UIScreenCapturedDidChangeNotification
                    object:nil];
}

@end
