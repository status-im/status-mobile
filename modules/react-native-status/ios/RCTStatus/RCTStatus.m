#import "RCTStatus.h"

#import <Statusgo/Statusgo.h>

@implementation Status
@synthesize bridge = _bridge;

RCT_EXPORT_MODULE();

RCT_EXPORT_METHOD(initJail: (NSString *) js
                  callback:(RCTResponseSenderBlock)callback) {
    initJail((char *) [js UTF8String]);
    callback(@[[NSNull null]]);
}

RCT_EXPORT_METHOD(parseJail:(NSString *)chatId
                  js:(NSString *)js
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"parseJail() method called");
#endif
    char * result = parse((char *) [chatId UTF8String], (char *) [js UTF8String]);
    callback(@[[NSNull null], [NSString stringWithUTF8String: result]]);
}

RCT_EXPORT_METHOD(callJail:(NSString *)chatId
                  path:(NSString *)path
                  params:(NSString *)params
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"callJail() method called");
#endif
    char * result = call((char *) [chatId UTF8String], (char *) [path UTF8String], (char *) [params UTF8String]);
    callback(@[[NSNull null], [NSString stringWithUTF8String: result]]);
}

RCT_EXPORT_METHOD(setAdjustResize) {
#if DEBUG
    NSLog(@"setAdjustResize() works only on Android");
#endif
}

RCT_EXPORT_METHOD(setAdjustPan) {
#if DEBUG
    NSLog(@"setAdjustPan() works only on Android");
#endif
}

RCT_EXPORT_METHOD(setSoftInputMode: (NSInteger) i) {
#if DEBUG
    NSLog(@"setSoftInputMode() works only on Android");
#endif
}

@end
