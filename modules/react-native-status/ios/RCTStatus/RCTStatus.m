#import "RCTStatus.h"
#import "RCTBridge.h"
#import "RCTEventDispatcher.h"
#import <Statusgo/Statusgo.h>

static bool isStatusInitialized;
static RCTBridge *bridge;
@implementation Status{
}

-(RCTBridge *)bridge
{
    return bridge;
}

-(void)setBridge:(RCTBridge *)newBridge
{
    bridge = newBridge;
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
    dispatch_async( dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        char * result = Call((char *) [chatId UTF8String], (char *) [path UTF8String], (char *) [params UTF8String]);
        dispatch_async( dispatch_get_main_queue(), ^{
            callback(@[[NSString stringWithUTF8String: result]]);
        });
    });
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

        NSString *peer1 = @"enode://fc3065bb80bfced98a01441718e2b70a0353f023b9da3d57beb8f96a827402d23702b3a461e1c1b6c7a208cb09cc0aea9b7c42bf953bb8f732529c198b158db4@95.85.40.211:30303";
        NSString *peer2 = @"enode://3b020a1fd6ab980a5670975e8a7361af1732fa3fa1819b751a94b6a4265e8c52b02c608c0de1347784b834b298280b018bcf6547f47bbba63612cba0e4707ec1@139.59.212.114:30303";
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0),
                       ^(void) {
            StartNode((char *) [folderName.path UTF8String]);
        });
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(5 * NSEC_PER_SEC)),
                       dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0),
                       ^(void) {
                           AddPeer((char *) [peer1 UTF8String]);
                           AddPeer((char *) [peer2 UTF8String]);
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
#pragma mark - Complete Transaction
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
#pragma mark - Discard Transaction
//////////////////////////////////////////////////////////////////// completeTransaction
RCT_EXPORT_METHOD(discardTransaction:(NSString *)id) {
#if DEBUG
    NSLog(@"DiscardTransaction() method called");
#endif
    DiscardTransaction((char *) [id UTF8String]);
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

+ (void)signalEvent:(char *) signal
{
    NSString *sig = [NSString stringWithUTF8String:signal];
#if DEBUG
    NSLog(@"SignalEvent");
    NSLog(sig);
#endif
    [bridge.eventDispatcher sendAppEventWithName:@"gethEvent"
                                            body:@{@"jsonEvent": sig}];
}

@end
