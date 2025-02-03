#import "NetworkManager.h"
#import "React/RCTBridge.h"
#import "React/RCTEventDispatcher.h"
#import "Statusgo.h"
#import "Utils.h"
#import "StatusBackendClient.h"
@implementation NetworkManager

RCT_EXPORT_MODULE();

RCT_EXPORT_METHOD(startSearchForLocalPairingPeers:(RCTResponseSenderBlock)callback) {
    [StatusBackendClient executeStatusGoRequestWithCallback:@"StartSearchForLocalPairingPeers" 
                                                     body:@""
                                         statusgoFunction:^NSString *{
        return StatusgoStartSearchForLocalPairingPeers();
    }
                                               callback:callback];
}

RCT_EXPORT_METHOD(getConnectionStringForBootstrappingAnotherDevice:(NSString *)configJSON
        callback:(RCTResponseSenderBlock)callback) {

    NSData *configData = [configJSON dataUsingEncoding:NSUTF8StringEncoding];
    NSError *error;
    NSMutableDictionary *configDict = [NSJSONSerialization JSONObjectWithData:configData options:NSJSONReadingMutableContainers error:&error];
    if (error) {
        NSLog(@"Error parsing JSON: %@", error);
        return;
    }

    NSMutableDictionary *senderConfig = configDict[@"senderConfig"];
    NSString *keyUID = senderConfig[@"keyUID"];
    NSURL *multiaccountKeystoreDir = [Utils getKeyStoreDirForKeyUID:keyUID];
    NSString *keystoreDir = multiaccountKeystoreDir.path;

    [senderConfig setValue:keystoreDir forKey:@"keystorePath"];
    NSString *modifiedConfigJSON = [Utils jsonStringWithPrettyPrint:NO fromDictionary:configDict];

    [StatusBackendClient executeStatusGoRequestWithCallback:@"GetConnectionStringForBootstrappingAnotherDevice"
                                                     body:modifiedConfigJSON
                                         statusgoFunction:^NSString *{
        return StatusgoGetConnectionStringForBootstrappingAnotherDevice(modifiedConfigJSON);
    }
                                               callback:callback];
}

RCT_EXPORT_METHOD(inputConnectionStringForBootstrapping:(NSString *)cs
        configJSON:(NSString *)configJSON
        callback:(RCTResponseSenderBlock)callback) {

    NSData *configData = [configJSON dataUsingEncoding:NSUTF8StringEncoding];
    NSError *jsonError;
    NSDictionary *configDict = [NSJSONSerialization JSONObjectWithData:configData options:0 error:&jsonError];
    if (jsonError) {
        NSLog(@"Error parsing JSON: %@", jsonError);
        return;
    }

    NSDictionary *params = @{
        @"connectionString": cs,
        @"receiverClientConfig": configDict
    };
    NSError *error;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:params options:0 error:&error];
    if (error) {
        NSLog(@"Error creating JSON: %@", error);
        return;
    }
    NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    [StatusBackendClient executeStatusGoRequestWithCallback:@"InputConnectionStringForBootstrappingV2"
                                                     body:jsonString
                                         statusgoFunction:^NSString *{
        return StatusgoInputConnectionStringForBootstrappingV2(jsonString);
    }
                                               callback:callback];
}

RCT_EXPORT_METHOD(sendTransactionWithSignature:(NSString *)txArgsJSON
        signature:(NSString *)signature
        callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"sendTransactionWithSignature() method called");
#endif
    [StatusBackendClient executeStatusGoRequestWithCallback:@"SendTransactionWithSignature"
                                                     body:txArgsJSON
                                         statusgoFunction:^NSString *{
        return StatusgoSendTransactionWithSignature(txArgsJSON, signature);
    }
                                               callback:callback];
}

#pragma mark - SendTransaction

RCT_EXPORT_METHOD(sendTransaction:(NSString *)txArgsJSON
        password:(NSString *)password
        callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"SendTransactionV2() method called");
#endif
    NSData *txArgsData = [txArgsJSON dataUsingEncoding:NSUTF8StringEncoding];
    NSError *jsonError;
    NSDictionary *txArgsDict = [NSJSONSerialization JSONObjectWithData:txArgsData options:0 error:&jsonError];
    if (jsonError) {
        NSLog(@"Error parsing JSON: %@", jsonError);
        return;
    }

    NSDictionary *params = @{
        @"txArgs": txArgsDict,
        @"password": password
    };
    NSError *error;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:params options:0 error:&error];
    if (error) {
        NSLog(@"Error creating JSON: %@", error);
        return;
    }
    NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    [StatusBackendClient executeStatusGoRequestWithCallback:@"SendTransactionV2"
                                                     body:jsonString
                                         statusgoFunction:^NSString *{
        return StatusgoSendTransactionV2(jsonString);
    }
                                               callback:callback];
}

RCT_EXPORT_METHOD(callRPC:(NSString *)payload
        callback:(RCTResponseSenderBlock)callback) {
    [StatusBackendClient executeStatusGoRequestWithCallback:@"CallRPC"
                                                     body:payload
                                         statusgoFunction:^NSString *{
        return StatusgoCallRPC(payload);
    }
                                               callback:callback];
}

RCT_EXPORT_METHOD(callPrivateRPC:(NSString *)payload
        callback:(RCTResponseSenderBlock)callback) {
    [StatusBackendClient executeStatusGoRequestWithCallback:@"CallPrivateRPC"
                                                     body:payload
                                         statusgoFunction:^NSString *{
        return StatusgoCallPrivateRPC(payload);
    }
                                               callback:callback];
}

#pragma mark - Recover

RCT_EXPORT_METHOD(recover:(NSString *)message
        callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"Recover() method called");
#endif
    [StatusBackendClient executeStatusGoRequestWithCallback:@"Recover"
                                                     body:message
                                         statusgoFunction:^NSString *{
        return StatusgoRecover(message);
    }
                                               callback:callback];
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

    [StatusBackendClient executeStatusGoRequestWithCallback:@"GetConnectionStringForExportingKeypairsKeystores"
                                                     body:modifiedConfigJSON
                                         statusgoFunction:^NSString *{
        return StatusgoGetConnectionStringForExportingKeypairsKeystores(modifiedConfigJSON);
    }
                                               callback:callback];
}

RCT_EXPORT_METHOD(inputConnectionStringForImportingKeypairsKeystores:(NSString *)cs
        configJSON:(NSString *)configJSON
        callback:(RCTResponseSenderBlock)callback) {

    NSData *configData = [configJSON dataUsingEncoding:NSUTF8StringEncoding];
    NSError *error;
    NSMutableDictionary *configDict = [NSJSONSerialization JSONObjectWithData:configData options:NSJSONReadingMutableContainers error:&error];
    NSMutableDictionary *receiverConfig = configDict[@"receiverConfig"];
    NSFileManager *fileManager = [NSFileManager defaultManager];
    NSURL *rootUrl =[Utils getRootUrl];
    NSURL *multiaccountKeystoreDir = [rootUrl URLByAppendingPathComponent:@"keystore"];
    NSString *keystoreDir = multiaccountKeystoreDir.path;

    [receiverConfig setValue:keystoreDir forKey:@"keystorePath"];
    NSString *modifiedConfigJSON = [Utils jsonStringWithPrettyPrint:NO fromDictionary:configDict];
    
    NSDictionary *params = @{
        @"connectionString": cs,
        @"keystoreFilesReceiverClientConfig": modifiedConfigJSON
    };
    NSString *paramsJSON = [Utils jsonStringWithPrettyPrint:NO fromDictionary:params];
    
    [StatusBackendClient executeStatusGoRequestWithCallback:@"InputConnectionStringForImportingKeypairsKeystoresV2"
                                                     body:paramsJSON
                                         statusgoFunction:^NSString *{
        return StatusgoInputConnectionStringForImportingKeypairsKeystoresV2(paramsJSON);
    }
                                               callback:callback];
}

@end
