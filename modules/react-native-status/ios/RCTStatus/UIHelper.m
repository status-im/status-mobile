#import "UIHelper.h"
#import "React/RCTBridge.h"
#import "React/RCTEventDispatcher.h"

@implementation UIHelper

RCT_EXPORT_MODULE();

#pragma mark - only android methods

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


@end
