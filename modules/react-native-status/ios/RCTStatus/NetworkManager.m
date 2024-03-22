#import "NetworkManager.h"
#import "React/RCTBridge.h"
#import "React/RCTEventDispatcher.h"
#import "Statusgo.h"
#import "Utils.h"

@interface NetworkManager() <NSNetServiceBrowserDelegate>

@property (nonatomic, strong) NSNetServiceBrowser *serviceBrowser;
@property (nonatomic, copy) RCTResponseSenderBlock callback;

@end

@implementation NetworkManager

RCT_EXPORT_MODULE();

- (instancetype)init {
  if ((self = [super init])) {
    self.serviceBrowser = [[NSNetServiceBrowser alloc] init];
    self.serviceBrowser.delegate = self;
  }
  return self;
}

RCT_EXPORT_METHOD(startSearchForLocalPairingPeers:(RCTResponseSenderBlock)callback) {
    NSString *result = StatusgoStartSearchForLocalPairingPeers();
    callback(@[result]);
}

RCT_EXPORT_METHOD(getConnectionStringForBootstrappingAnotherDevice:(NSString *)configJSON
        callback:(RCTResponseSenderBlock)callback) {

    NSData *configData = [configJSON dataUsingEncoding:NSUTF8StringEncoding];
    NSError *error;
    NSMutableDictionary *configDict = [NSJSONSerialization JSONObjectWithData:configData options:NSJSONReadingMutableContainers error:&error];
    NSMutableDictionary *senderConfig = configDict[@"senderConfig"];
    NSString *keyUID = senderConfig[@"keyUID"];
    NSURL *multiaccountKeystoreDir = [Utils getKeyStoreDirForKeyUID:keyUID];
    NSString *keystoreDir = multiaccountKeystoreDir.path;

    [senderConfig setValue:keystoreDir forKey:@"keystorePath"];
    NSString *modifiedConfigJSON = [Utils jsonStringWithPrettyPrint:NO fromDictionary:configDict];

    NSString *result = StatusgoGetConnectionStringForBootstrappingAnotherDevice(modifiedConfigJSON);
    callback(@[result]);
}

RCT_EXPORT_METHOD(inputConnectionStringForBootstrapping:(NSString *)cs
        configJSON:(NSString *)configJSON
        callback:(RCTResponseSenderBlock)callback) {

    NSData *configData = [configJSON dataUsingEncoding:NSUTF8StringEncoding];
    NSError *error;
    NSMutableDictionary *configDict = [NSJSONSerialization JSONObjectWithData:configData options:NSJSONReadingMutableContainers error:&error];
    NSMutableDictionary *receiverConfig = configDict[@"receiverConfig"];
    NSMutableDictionary *nodeConfig = receiverConfig[@"nodeConfig"];
    NSFileManager *fileManager = [NSFileManager defaultManager];
    NSURL *rootUrl =[[fileManager URLsForDirectory:NSLibraryDirectory inDomains:NSUserDomainMask] lastObject];
    NSURL *multiaccountKeystoreDir = [rootUrl URLByAppendingPathComponent:@"keystore"];
    NSString *keystoreDir = multiaccountKeystoreDir.path;
    NSString *rootDataDir = rootUrl.path;

    [receiverConfig setValue:keystoreDir forKey:@"keystorePath"];
    [nodeConfig setValue:rootDataDir forKey:@"rootDataDir"];
    NSString *modifiedConfigJSON = [Utils jsonStringWithPrettyPrint:NO fromDictionary:configDict];
    NSString *result = StatusgoInputConnectionStringForBootstrapping(cs, modifiedConfigJSON);
    callback(@[result]);
}

RCT_EXPORT_METHOD(sendTransactionWithSignature:(NSString *)txArgsJSON
        signature:(NSString *)signature
        callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"sendTransactionWithSignature() method called");
#endif
    NSString *result = StatusgoSendTransactionWithSignature(txArgsJSON, signature);
    callback(@[result]);
}

#pragma mark - SendTransaction

RCT_EXPORT_METHOD(sendTransaction:(NSString *)txArgsJSON
        password:(NSString *)password
        callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"SendTransaction() method called");
#endif
    NSString *result = StatusgoSendTransaction(txArgsJSON, password);
    callback(@[result]);
}

RCT_EXPORT_METHOD(callRPC:(NSString *)payload
        callback:(RCTResponseSenderBlock)callback) {
    dispatch_async( dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        NSString *result = StatusgoCallRPC(payload);
        dispatch_async(dispatch_get_main_queue(), ^{
            callback(@[result]);
        });
    });
}

RCT_EXPORT_METHOD(callPrivateRPC:(NSString *)payload
        callback:(RCTResponseSenderBlock)callback) {
    dispatch_async( dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        NSString *result = StatusgoCallPrivateRPC(payload);
        dispatch_async(dispatch_get_main_queue(), ^{
            callback(@[result]);
        });
    });
}

RCT_EXPORT_METHOD(requestLocalNetworkAccess:(RCTResponseSenderBlock)callback) {
  self.callback = callback;

  // Start browsing for services using TCP on the local network.
  dispatch_async(dispatch_get_main_queue(), ^{
    [self.serviceBrowser searchForServicesOfType:@"_http._tcp." inDomain:@""];
  });
}

// NSNetServiceBrowser delegate method
- (void)netServiceBrowser:(NSNetServiceBrowser *)browser didNotSearch:(NSDictionary<NSString *,NSNumber *> *)errorDict {
  RCTLogInfo(@"Failed to search for services: %@", errorDict);
  if (self.callback) {
    self.callback(@[@"Failed to search for services"]);
  }
}

// NSNetServiceBrowser delegate method
- (void)netServiceBrowser:(NSNetServiceBrowser *)browser didFindService:(NSNetService *)service moreComing:(BOOL)moreComing {
  RCTLogInfo(@"Found service: %@", service);
  // we stop the search after finding the first service or wait until the search is finished.
  [browser stop];

  if (self.callback) {
    self.callback(@[[NSNull null], @"Requested local network access and found service"]);
  }
}

// NSNetServiceBrowser delegate method
- (void)netServiceBrowserDidStopSearch:(NSNetServiceBrowser *)browser {
  RCTLogInfo(@"Stopped searching for services");
}

#pragma mark - Recover

RCT_EXPORT_METHOD(recover:(NSString *)message
        callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"Recover() method called");
#endif
    NSString *result = StatusgoRecover(message);
    callback(@[result]);
}

@end
