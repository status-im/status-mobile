#import "RCTStatus.h"

#import <Statusgo/Statusgo.h>

static bool isStatusInitialized;

@implementation Status{
}


RCT_EXPORT_MODULE();

////////////////////////////////////////////////////////////////////
#pragma mark - Jails functions
//////////////////////////////////////////////////////////////////// initJail
RCT_EXPORT_METHOD(initJail: (NSString *) js
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"InitJail() method called");
#endif
    InitJail((char *) [js UTF8String]);
    callback(@[[NSNull null]]);
}

//////////////////////////////////////////////////////////////////// parseJail
RCT_EXPORT_METHOD(parseJail:(NSString *)chatId
                  js:(NSString *)js
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"ParseJail() method called");
#endif
    char * result = Parse((char *) [chatId UTF8String], (char *) [js UTF8String]);
    callback(@[[NSString stringWithUTF8String: result]]);
}

//////////////////////////////////////////////////////////////////// callJail
RCT_EXPORT_METHOD(callJail:(NSString *)chatId
                  path:(NSString *)path
                  params:(NSString *)params
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"CallJail() method called");
#endif
    char * result = Call((char *) [chatId UTF8String], (char *) [path UTF8String], (char *) [params UTF8String]);
    callback(@[[NSString stringWithUTF8String: result]]);
}

////////////////////////////////////////////////////////////////////
#pragma mark - startNode
//////////////////////////////////////////////////////////////////// startNode
RCT_EXPORT_METHOD(startNode:(RCTResponseSenderBlock)onResultCallback) {
#if DEBUG
    NSLog(@"StartNode() method called");
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
        
        NSString *peer = @"enode://4e2bb6b09aa34375ae2df23fa063edfe7aaec952dba972449158ae0980a4abd375aca3c06a519d4f562ff298565afd288a0ed165944974b2557e6ff2c31424de@138.68.73.175:30303";
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0),
                       ^(void) {
            StartNode((char *) [folderName.path UTF8String]);
        });
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(5 * NSEC_PER_SEC)),
                       dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0),
                       ^(void) {
                           AddPeer((char *) [peer UTF8String]);
        });
        onResultCallback(@[[NSNull null]]);
        return;
    }
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
    NSLog(@"CreateAccount() method called");
#endif
    char * result = CreateAccount((char *) [password UTF8String]);
    callback(@[[NSString stringWithUTF8String: result]]);
}

//////////////////////////////////////////////////////////////////// recoverAccount
RCT_EXPORT_METHOD(recoverAccount:(NSString *)passphrase
                  password:(NSString *)password
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"RecoverAccount() method called");
#endif
    char * result = RecoverAccount((char *) [password UTF8String], (char *) [passphrase UTF8String]);
    callback(@[[NSString stringWithUTF8String: result]]);
}

//////////////////////////////////////////////////////////////////// login
RCT_EXPORT_METHOD(login:(NSString *)address
                  password:(NSString *)password
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"Login() method called");
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
    NSLog(@"CompleteTransaction() method called");
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
