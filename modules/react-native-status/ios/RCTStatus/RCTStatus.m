#import "RCTStatus.h"

#import <Statusgo/Statusgo.h>

@implementation Status{
    bool isStatusInitialized;
}


RCT_EXPORT_MODULE();

////////////////////////////////////////////////////////////////////
#pragma mark - Jails functions
//////////////////////////////////////////////////////////////////// initJail
RCT_EXPORT_METHOD(initJail: (NSString *) js
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"initJail() method called");
#endif
    initJail((char *) [js UTF8String]);
    callback(@[[NSNull null]]);
}

//////////////////////////////////////////////////////////////////// parseJail
RCT_EXPORT_METHOD(parseJail:(NSString *)chatId
                  js:(NSString *)js
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"parseJail() method called");
#endif
    char * result = parse((char *) [chatId UTF8String], (char *) [js UTF8String]);
    callback(@[[NSString stringWithUTF8String: result]]);
}

//////////////////////////////////////////////////////////////////// callJail
RCT_EXPORT_METHOD(callJail:(NSString *)chatId
                  path:(NSString *)path
                  params:(NSString *)params
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"callJail() method called");
#endif
    char * result = call((char *) [chatId UTF8String], (char *) [path UTF8String], (char *) [params UTF8String]);
    callback(@[[NSString stringWithUTF8String: result]]);
}

////////////////////////////////////////////////////////////////////
#pragma mark - startNode
//////////////////////////////////////////////////////////////////// startNode
RCT_EXPORT_METHOD(startNode:(RCTResponseSenderBlock)onResultCallback
                  callback:(RCTResponseSenderBlock)onAlreadyRunningCallback) {
#if DEBUG
    NSLog(@"startNode() method called");
#endif
    if (!isStatusInitialized){
        isStatusInitialized = true;
        
        NSError *error = nil;
        NSURL *folderName =[[[[NSFileManager defaultManager]
                              URLsForDirectory:NSDocumentDirectory inDomains:NSUserDomainMask]
                             lastObject]
                            URLByAppendingPathComponent:@"ethereum"];
        
        if (![[NSFileManager defaultManager] fileExistsAtPath:folderName.path])
            [[NSFileManager defaultManager] createDirectoryAtPath:folderName.path withIntermediateDirectories:NO attributes:nil error:&error];
        
        if (error){
            NSLog(@"error %@", error);
        }else
            NSLog(@"folderName: %@", folderName);
        
        NSString *peer = @"enode://409772c7dea96fa59a912186ad5bcdb5e51b80556b3fe447d940f99d9eaadb51d4f0ffedb68efad232b52475dd7bd59b51cee99968b3cc79e2d5684b33c4090c@139.162.166.59:30303";
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0),
                       ^(void) {
            StartNode((char *) [folderName.path UTF8String]);
        });
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(5 * NSEC_PER_SEC)),
                       dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0),
                       ^(void) {
                           addPeer((char *) [peer UTF8String]);
        });
        onResultCallback(@[[NSNull null]]);
        return;
    }
    onAlreadyRunningCallback(@[[NSNull null]]);
}

RCT_EXPORT_METHOD(stopNode:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"stopNode() method called");
#endif
    // TODO: stop node
    
    callback(@[[NSNull null]]);
}

////////////////////////////////////////////////////////////////////
#pragma mark - Accounts method
//////////////////////////////////////////////////////////////////// createAccount
RCT_EXPORT_METHOD(createAccount:(NSString *)password
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"createAccount() method called");
#endif
    char * result = CreateAccount((char *) [password UTF8String]);
    callback(@[[NSString stringWithUTF8String: result]]);
}

//////////////////////////////////////////////////////////////////// recoverAccount
RCT_EXPORT_METHOD(recoverAccount:(NSString *)passphrase
                  password:(NSString *)password
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"recoverAccount() method called");
#endif
    char * result = RecoverAccount((char *) [password UTF8String], (char *) [passphrase UTF8String]);
    callback(@[[NSString stringWithUTF8String: result]]);
}

//////////////////////////////////////////////////////////////////// login
RCT_EXPORT_METHOD(login:(NSString *)address
                  password:(NSString *)password
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"login() method called");
#endif
    char * result = Login((char *) [address UTF8String], (char *) [password UTF8String]);
    callback(@[[NSString stringWithUTF8String: result]]);
}

////////////////////////////////////////////////////////////////////
#pragma mark - Transaction
//////////////////////////////////////////////////////////////////// completeTransaction
RCT_EXPORT_METHOD(completeTransaction:(NSString *)hash
                  password:(NSString *)password
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"completeTransaction() method called");
#endif
    char * result = CompleteTransaction((char *) [hash UTF8String], (char *) [password UTF8String]);
    callback(@[[NSString stringWithUTF8String: result]]);
}

////////////////////////////////////////////////////////////////////
#pragma mark - only android methods
////////////////////////////////////////////////////////////////////
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
