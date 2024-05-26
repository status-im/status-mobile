#import "RCTStatus.h"
#import "React/RCTBridge.h"
#import "React/RCTEventDispatcher.h"
#import "Statusgo.h"

#import "Utils.h"

static RCTBridge *bridge;

@implementation Status

- (instancetype)init {
    self = [super init];
    if (!self) {
        return nil;
    }
    // Subscribing to the signals from Status-Go
    StatusgoSetMobileSignalHandler(self);
    return self;
}

-(RCTBridge *)bridge
{
    return bridge;
}

-(void)setBridge:(RCTBridge *)newBridge
{
    bridge = newBridge;
}

- (void)handleSignal:(NSString *)signal
{
    if(!signal){
#if DEBUG
        NSLog(@"SignalEvent nil");
#endif
        return;
    }

    [bridge.eventDispatcher sendAppEventWithName:@"gethEvent"
                                            body:@{@"jsonEvent": signal}];

    return;
}

RCT_EXPORT_MODULE();

#pragma mark - shouldMoveToInternalStorage

RCT_EXPORT_METHOD(shouldMoveToInternalStorage:(RCTResponseSenderBlock)onResultCallback) {
    // Android only
    onResultCallback(@[[NSNull null]]);
}

#pragma mark - moveToInternalStorage

RCT_EXPORT_METHOD(moveToInternalStorage:(RCTResponseSenderBlock)onResultCallback) {
    // Android only
    onResultCallback(@[[NSNull null]]);
}

RCT_EXPORT_METHOD(exportLogs:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"exportLogs() method called");
#endif
    NSString *result = StatusgoExportNodeLogs();
    callback(@[result]);
}

RCT_EXPORT_METHOD(deleteImportedKey:(NSString *)keyUID
                  address:(NSString *)address
                  password:(NSString *)password
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"DeleteImportedKey() method called");
#endif
    NSURL *multiaccountKeystoreDir = [Utils getKeyStoreDirForKeyUID:keyUID];
    NSString *result = StatusgoDeleteImportedKey(address, password, multiaccountKeystoreDir.path);
    callback(@[result]);
}

RCT_EXPORT_METHOD(multiAccountGenerateAndDeriveAddresses:(NSString *)json
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"MultiAccountGenerateAndDeriveAddresses() method called");
#endif
    NSString *result = StatusgoMultiAccountGenerateAndDeriveAddresses(json);
    callback(@[result]);
}

RCT_EXPORT_METHOD(multiAccountStoreAccount:(NSString *)json
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"MultiAccountStoreAccount() method called");
#endif
    NSString *result = StatusgoMultiAccountStoreAccount(json);
    callback(@[result]);
}

RCT_EXPORT_METHOD(multiAccountLoadAccount:(NSString *)json
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"MultiAccountLoadAccount() method called");
#endif
    NSString *result = StatusgoMultiAccountLoadAccount(json);
    callback(@[result]);
}

RCT_EXPORT_METHOD(multiAccountStoreDerived:(NSString *)json
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"MultiAccountStoreDerived() method called");
#endif
    NSString *result = StatusgoMultiAccountStoreDerivedAccounts(json);
    callback(@[result]);
}

RCT_EXPORT_METHOD(multiAccountImportPrivateKey:(NSString *)json
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"MultiAccountImportPrivateKey() method called");
#endif
    NSString *result = StatusgoMultiAccountImportPrivateKey(json);
    callback(@[result]);
}

RCT_EXPORT_METHOD(multiAccountImportMnemonic:(NSString *)json
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"MultiAccountImportMnemonic() method called");
#endif
    NSString *result = StatusgoMultiAccountImportMnemonic(json);
    callback(@[result]);
}

RCT_EXPORT_METHOD(multiAccountDeriveAddresses:(NSString *)json
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"MultiAccountDeriveAddresses() method called");
#endif
    NSString *result = StatusgoMultiAccountDeriveAddresses(json);
    callback(@[result]);
}

#pragma mark - GetNodeConfig

RCT_EXPORT_METHOD(getNodeConfig:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"GetNodeConfig() method called");
#endif
    NSString *result = StatusgoGetNodeConfig();
    callback(@[result]);
}

RCT_EXPORT_BLOCKING_SYNCHRONOUS_METHOD(fleets) {
  return StatusgoFleets();
}

RCT_EXPORT_METHOD(closeApplication) {
    exit(0);
}

RCT_EXPORT_METHOD(connectionChange:(NSString *)type
                       isExpensive:(BOOL)isExpensive) {
#if DEBUG
    NSLog(@"ConnectionChange() method called");
#endif
    StatusgoConnectionChange(type, isExpensive ? 1 : 0);
}

RCT_EXPORT_METHOD(appStateChange:(NSString *)type) {
#if DEBUG
    NSLog(@"AppStateChange() method called");
#endif
    StatusgoAppStateChange(type);
}

RCT_EXPORT_METHOD(startLocalNotifications) {
#if DEBUG
    NSLog(@"StartLocalNotifications() method called");
#endif
StatusgoStartLocalNotifications();
}

#pragma mark - deviceinfo

- (bool) is24Hour
{
    NSString *format = [NSDateFormatter dateFormatFromTemplate:@"j" options:0 locale:[NSLocale currentLocale]];
    return ([format rangeOfString:@"a"].location == NSNotFound);
}

- (NSString *)getBuildId {
    return @"not available";
}

- (NSString*) deviceId
{
    struct utsname systemInfo;

    uname(&systemInfo);

    NSString* deviceId = [NSString stringWithCString:systemInfo.machine
                                            encoding:NSUTF8StringEncoding];

    if ([deviceId isEqualToString:@"i386"] || [deviceId isEqualToString:@"x86_64"] ) {
        deviceId = [NSString stringWithFormat:@"%s", getenv("SIMULATOR_MODEL_IDENTIFIER")];
    }

    return deviceId;
}

- (NSString*) deviceName
{
    return [[UIDevice currentDevice] name];
}

- (NSDictionary *)constantsToExport
{
    return @{
             @"is24Hour": @(self.is24Hour),
             @"model": self.deviceName ?: [NSNull null],
             @"brand": @"Apple",
             @"buildId": [self getBuildId],
             @"deviceId": self.deviceId ?: [NSNull null],
             };
}

+ (BOOL)requiresMainQueueSetup
{
    return NO;
}

@end
