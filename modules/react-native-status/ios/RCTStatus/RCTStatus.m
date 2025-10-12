#import "RCTStatus.h"
#import "React/RCTBridge.h"
#import "React/RCTEventDispatcher.h"
#import "Statusgo.h"
#import "StatusBackendClient.h"
#import "Utils.h"

static RCTBridge *bridge;

@implementation Status

+ (Status *)sharedInstance {
    static Status *sharedInstance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedInstance = [[self alloc] init];
    });
    return sharedInstance;
}

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
    NSLog(@"DeleteImportedKeyV2() method called");
#endif
    NSURL *multiaccountKeystoreDir = [Utils getKeyStoreDirForKeyUID:keyUID];
    NSString *jsonParams = [NSString stringWithFormat:@"{\"address\":\"%@\",\"password\":\"%@\",\"keyStoreDir\":\"%@\"}", 
                            address, password, multiaccountKeystoreDir.path];
    [StatusBackendClient executeStatusGoRequestWithCallback:@"DeleteImportedKeyV2"
                                                     body:jsonParams
                                         statusgoFunction:^NSString *{
        return StatusgoDeleteImportedKeyV2(jsonParams);
    }
                                               callback:callback];
}

#pragma mark - GetNodeConfig

RCT_EXPORT_METHOD(getNodeConfig:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"GetNodeConfig() method called");
#endif
    [StatusBackendClient executeStatusGoRequestWithCallback:@"GetNodeConfig"
                                                     body:@""
                                         statusgoFunction:^NSString *{
        return StatusgoGetNodeConfig();
    }
                                               callback:callback];
}

- (NSString *)setBackupPath:(NSString *)backupPath {
    NSDictionary *payload = @{
        @"jsonrpc": @"2.0",
        @"id": @1,
        @"method": @"settings_saveSetting",
        @"params": @[@"backup-path", backupPath]
    };

    NSError *error;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:payload options:0 error:&error];
    if (error) {
        NSLog(@"Error creating JSON for setBackupPath: %@", error);
        return nil;
    }

    NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    return StatusgoCallPrivateRPC(jsonString);
}

RCT_EXPORT_METHOD(performLocalBackup:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"performLocalBackup() method called");
#endif

    @try {
        // Get backup directory path
        NSString *backupPath = [Utils getBackupDirectory];
        NSLog(@"Setting backup path: %@", backupPath);

        // Set backup path in status-go settings
        NSString *settingsResult = [self setBackupPath:backupPath];
        NSLog(@"Set backup path result: %@", settingsResult);

        // Perform the actual backup
        [StatusBackendClient executeStatusGoRequestWithCallback:@"PerformLocalBackup"
                                                         body:@""
                                             statusgoFunction:^NSString *{
            return StatusgoPerformLocalBackup();
        }
                                                   callback:callback];
    } @catch (NSException *exception) {
        NSLog(@"Error in performLocalBackup: %@", exception);
        NSDictionary *errorResult = @{@"error": exception.reason ?: @"Unknown error"};
        NSData *jsonData = [NSJSONSerialization dataWithJSONObject:errorResult options:0 error:nil];
        NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
        callback(@[jsonString]);
    }
}

RCT_EXPORT_METHOD(intendedPanic:(NSString *)message) {
#if DEBUG
    NSLog(@"IntendedPanic() method called");
#endif
    [StatusBackendClient executeStatusGoRequest:@"IntendedPanic"
                                           body:message
                               statusgoFunction:^NSString *{
        return StatusgoIntendedPanic(message);
    }];
}

RCT_EXPORT_BLOCKING_SYNCHRONOUS_METHOD(fleets) {
    return [StatusBackendClient executeStatusGoRequestWithResult:@"Fleets"
                                                         body:@""
                                             statusgoFunction:^NSString *{
        return StatusgoFleets();
    }];
}

RCT_EXPORT_METHOD(closeApplication) {
    exit(0);
}

RCT_EXPORT_METHOD(connectionChange:(NSString *)type
                       isExpensive:(BOOL)isExpensive) {
#if DEBUG
    NSLog(@"ConnectionChange() method called");
#endif
    NSDictionary *params = @{
        @"type": type,
        @"expensive": @(isExpensive)
    };
    NSError *error;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:params 
                                                       options:0
                                                         error:&error];
    if (error) {
        NSLog(@"Error creating JSON: %@", error);
        return;
    }
    
    if (jsonData) {
        NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
        [StatusBackendClient executeStatusGoRequest:@"ConnectionChangeV2"
                                             body:jsonString
                                 statusgoFunction:^NSString *{
            return StatusgoConnectionChangeV2(jsonString);
        }];
    } else {
        NSLog(@"Failed to create JSON data");
    }
}

RCT_EXPORT_METHOD(appStateChange:(NSString *)state) {
#if DEBUG
    NSLog(@"AppStateChange() method called");
#endif
    NSDictionary *params = @{@"state": state};
    NSError *error;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:params 
                                                       options:0
                                                         error:&error];
    if (error) {
        NSLog(@"Error creating JSON: %@", error);
        return;
    }
    
    if (jsonData) {
        NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
        [StatusBackendClient executeStatusGoRequest:@"AppStateChangeV2"
                                                         body:jsonString
                                             statusgoFunction:^NSString *{
            return StatusgoAppStateChangeV2(jsonString);
        }];
    } else {
        NSLog(@"Failed to create JSON data");
    }
}

RCT_EXPORT_METHOD(addCentralizedMetric:(NSString *)request
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"addCentralizedMetric() method called");
#endif
    [StatusBackendClient executeStatusGoRequestWithCallback:@"AddCentralizedMetric"
                                                     body:request
                                         statusgoFunction:^NSString *{
        return StatusgoAddCentralizedMetric(request);
    }
                                               callback:callback];
}

RCT_EXPORT_METHOD(toggleCentralizedMetrics:(NSString *)request
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"toggleCentralizedMetrics() method called");
#endif
    [StatusBackendClient executeStatusGoRequestWithCallback:@"ToggleCentralizedMetrics"
                                                     body:request
                                         statusgoFunction:^NSString *{
        return StatusgoToggleCentralizedMetrics(request);
    }
                                               callback:callback];
}

RCT_EXPORT_METHOD(startLocalNotifications) {
#if DEBUG
    NSLog(@"StartLocalNotifications() method called");
#endif
    [StatusBackendClient executeStatusGoRequest:@"StartLocalNotifications"
                                         body:@""
                             statusgoFunction:^NSString *{
        return StatusgoStartLocalNotifications();
    }];
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
