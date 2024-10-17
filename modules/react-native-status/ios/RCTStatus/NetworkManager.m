#import "NetworkManager.h"
#import "React/RCTBridge.h"
#import "React/RCTEventDispatcher.h"
#import "Statusgo.h"
#import "Utils.h"

@implementation NetworkManager

RCT_EXPORT_MODULE();

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

    NSDictionary *params = @{
        @"connectionString": cs,
        @"receiverClientConfig": configJSON
    };
    NSError *error;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:params options:0 error:&error];
    if (error) {
        NSLog(@"Error creating JSON: %@", error);
        return;
    }
    NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    NSString *result = StatusgoInputConnectionStringForBootstrappingV2(jsonString);
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
    NSLog(@"SendTransactionV2() method called");
#endif
    NSDictionary *params = @{
        @"txArgs": txArgsJSON,
        @"password": password
    };
    NSError *error;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:params options:0 error:&error];
    if (error) {
        NSLog(@"Error creating JSON: %@", error);
        return;
    }
    NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    NSString *result = StatusgoSendTransactionV2(jsonString);
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

#pragma mark - Recover

RCT_EXPORT_METHOD(recover:(NSString *)message
        callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"Recover() method called");
#endif
    NSString *result = StatusgoRecover(message);
    callback(@[result]);
}

RCT_EXPORT_METHOD(getConnectionStringForExportingKeypairsKeystores:(NSString *)configJSON
        callback:(RCTResponseSenderBlock)callback) {

    NSData *configData = [configJSON dataUsingEncoding:NSUTF8StringEncoding];
    NSError *error;
    NSMutableDictionary *configDict = [NSJSONSerialization JSONObjectWithData:configData options:NSJSONReadingMutableContainers error:&error];
    NSMutableDictionary *senderConfig = configDict[@"senderConfig"];
    NSString *keyUID = senderConfig[@"loggedInKeyUid"];
    NSURL *multiaccountKeystoreDir = [Utils getKeyStoreDirForKeyUID:keyUID];
    NSString *keystoreDir = multiaccountKeystoreDir.path;

    [senderConfig setValue:keystoreDir forKey:@"keystorePath"];
    NSString *modifiedConfigJSON = [Utils jsonStringWithPrettyPrint:NO fromDictionary:configDict];

    NSString *result = StatusgoGetConnectionStringForExportingKeypairsKeystores(modifiedConfigJSON);
    callback(@[result]);
}

RCT_EXPORT_METHOD(inputConnectionStringForImportingKeypairsKeystores:(NSString *)cs
        configJSON:(NSString *)configJSON
        callback:(RCTResponseSenderBlock)callback) {

    NSData *configData = [configJSON dataUsingEncoding:NSUTF8StringEncoding];
    NSError *error;
    NSMutableDictionary *configDict = [NSJSONSerialization JSONObjectWithData:configData options:NSJSONReadingMutableContainers error:&error];
    NSMutableDictionary *receiverConfig = configDict[@"receiverConfig"];
    NSFileManager *fileManager = [NSFileManager defaultManager];
    NSURL *rootUrl =[[fileManager URLsForDirectory:NSLibraryDirectory inDomains:NSUserDomainMask] lastObject];
    NSURL *multiaccountKeystoreDir = [rootUrl URLByAppendingPathComponent:@"keystore"];
    NSString *keystoreDir = multiaccountKeystoreDir.path;

    [receiverConfig setValue:keystoreDir forKey:@"keystorePath"];
    NSString *modifiedConfigJSON = [Utils jsonStringWithPrettyPrint:NO fromDictionary:configDict];
    
    NSDictionary *params = @{
        @"connectionString": cs,
        @"keystoreFilesReceiverClientConfig": modifiedConfigJSON
    };
    NSString *paramsJSON = [Utils jsonStringWithPrettyPrint:NO fromDictionary:params];
    
    NSString *result = StatusgoInputConnectionStringForImportingKeypairsKeystoresV2(paramsJSON);
    callback(@[result]);
}

@end
