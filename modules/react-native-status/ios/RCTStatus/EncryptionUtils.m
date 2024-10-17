#import "EncryptionUtils.h"
#import "React/RCTBridge.h"
#import "React/RCTEventDispatcher.h"
#import "Statusgo.h"
#import "Utils.h"

@implementation EncryptionUtils

RCT_EXPORT_MODULE();

#pragma mark - InitKeystore method

RCT_EXPORT_METHOD(initKeystore:(NSString *)keyUID
        callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"initKeystore() method called");
#endif
    NSFileManager *fileManager = [NSFileManager defaultManager];
    NSURL *rootUrl =[[fileManager
            URLsForDirectory:NSLibraryDirectory inDomains:NSUserDomainMask]
            lastObject];

    NSURL *commonKeystoreDir = [rootUrl URLByAppendingPathComponent:@"keystore"];
    NSURL *keystoreDir = [commonKeystoreDir URLByAppendingPathComponent:keyUID];

    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0),
                   ^(void)
                   {
                       NSString *res = StatusgoInitKeystore(keystoreDir.path);
                       NSLog(@"InitKeyStore result %@", res);
                       callback(@[]);
                   });
}

RCT_EXPORT_METHOD(reEncryptDbAndKeystore:(NSString *)keyUID
        currentPassword:(NSString *)currentPassword
        newPassword:(NSString *)newPassword
        callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"reEncryptDbAndKeystore() method called");
#endif
    // Construct params into JSON string
    NSDictionary *params = @{
        @"keyUID": keyUID,
        @"oldPassword": currentPassword,
        @"newPassword": newPassword
    };
    NSError *error;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:params options:0 error:&error];
    if (error) {
        NSLog(@"Error creating JSON: %@", error);
        return;
    }
    NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    
    // Call ChangeDatabasePasswordV2 with JSON string param
    NSString *result = StatusgoChangeDatabasePasswordV2(jsonString);
    callback(@[result]);
}

RCT_EXPORT_METHOD(convertToKeycardAccount:(NSString *)keyUID
        accountData:(NSString *)accountData
        settings:(NSString *)settings
        keycardUID:(NSString *)keycardUID
        currentPassword:(NSString *)currentPassword
        newPassword:(NSString *)newPassword
        callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"convertToKeycardAccount() method called");
#endif
    NSURL *multiaccountKeystoreDir = [Utils getKeyStoreDirForKeyUID:keyUID];
    StatusgoInitKeystore(multiaccountKeystoreDir.path);
    
    NSDictionary *params = @{
        @"keyUID": keyUID,
        @"account": [NSJSONSerialization JSONObjectWithData:[accountData dataUsingEncoding:NSUTF8StringEncoding] options:0 error:nil],
        @"settings": [NSJSONSerialization JSONObjectWithData:[settings dataUsingEncoding:NSUTF8StringEncoding] options:0 error:nil],
        @"keycardUID": keycardUID,
        @"oldPassword": currentPassword,
        @"newPassword": newPassword
    };
    
    NSError *error;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:params options:0 error:&error];
    
    if (error) {
        NSLog(@"Error creating JSON: %@", [error localizedDescription]);
        return;
    }
    
    NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    
    NSString *result = StatusgoConvertToKeycardAccountV2(jsonString);
    callback(@[result]);
}

RCT_EXPORT_BLOCKING_SYNCHRONOUS_METHOD(encodeTransfer:(NSString *)to
        value:(NSString *)value) {
    NSDictionary *params = @{
        @"to": to,
        @"value": value
    };
    NSError *error;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:params options:0 error:&error];
    if (error) {
        NSLog(@"Error creating JSON: %@", [error localizedDescription]);
        return nil;
    }
    NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    return StatusgoEncodeTransferV2(jsonString);
}

RCT_EXPORT_BLOCKING_SYNCHRONOUS_METHOD(encodeFunctionCall:(NSString *)method
        paramsJSON:(NSString *)paramsJSON) {
    NSDictionary *params = @{
        @"method": method,
        @"paramsJSON": paramsJSON
    };
    NSError *error;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:params options:0 error:&error];
    if (error) {
        NSLog(@"Error creating JSON: %@", [error localizedDescription]);
        return nil;
    }
    NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    return StatusgoEncodeFunctionCallV2(jsonString);
}

RCT_EXPORT_BLOCKING_SYNCHRONOUS_METHOD(decodeParameters:(NSString *)decodeParamJSON) {
    return StatusgoDecodeParameters(decodeParamJSON);
}

RCT_EXPORT_BLOCKING_SYNCHRONOUS_METHOD(hexToNumber:(NSString *)hex) {
    return StatusgoHexToNumber(hex);
}

RCT_EXPORT_BLOCKING_SYNCHRONOUS_METHOD(numberToHex:(NSString *)numString) {
    return StatusgoNumberToHex(numString);
}

RCT_EXPORT_BLOCKING_SYNCHRONOUS_METHOD(sha3:(NSString *)str) {
    return StatusgoSha3(str);
}

RCT_EXPORT_BLOCKING_SYNCHRONOUS_METHOD(utf8ToHex:(NSString *)str) {
    return StatusgoUtf8ToHex(str);
}

RCT_EXPORT_BLOCKING_SYNCHRONOUS_METHOD(hexToUtf8:(NSString *)str) {
    return StatusgoHexToUtf8(str);
}

RCT_EXPORT_BLOCKING_SYNCHRONOUS_METHOD(serializeLegacyKey:(NSString *)str) {
    return StatusgoSerializeLegacyKey(str);
}

RCT_EXPORT_METHOD(setBlankPreviewFlag:(BOOL *)newValue)
{
    NSUserDefaults *userDefaults = [NSUserDefaults standardUserDefaults];

    [userDefaults setBool:newValue forKey:@"BLANK_PREVIEW"];

    [userDefaults synchronize];
}

RCT_EXPORT_METHOD(hashTransaction:(NSString *)txArgsJSON
        callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"HashTransaction() method called");
#endif
    NSString *result = StatusgoHashTransaction(txArgsJSON);
    callback(@[result]);
}

RCT_EXPORT_METHOD(hashMessage:(NSString *)message
        callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"hashMessage() method called");
#endif
    NSString *result = StatusgoHashMessage(message);
    callback(@[result]);
}

RCT_EXPORT_METHOD(localPairingPreflightOutboundCheck:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"LocalPairingPreflightOutboundCheck() method called");
#endif
    NSString *result = StatusgoLocalPairingPreflightOutboundCheck();
    callback(@[result]);
}

RCT_EXPORT_METHOD(multiformatDeserializePublicKey:(NSString *)multiCodecKey
        base58btc:(NSString *)base58btc
        callback:(RCTResponseSenderBlock)callback) {
    NSDictionary *params = @{
        @"key": multiCodecKey,
        @"outBase": base58btc
    };
    NSError *error;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:params options:0 error:&error];
    if (error) {
        NSLog(@"Error creating JSON: %@", error);
        return;
    }
    NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    NSString *result = StatusgoMultiformatDeserializePublicKeyV2(jsonString);
    callback(@[result]);
}

RCT_EXPORT_METHOD(deserializeAndCompressKey:(NSString *)desktopKey
        callback:(RCTResponseSenderBlock)callback) {
    NSString *result = StatusgoDeserializeAndCompressKey(desktopKey);
    callback(@[result]);
}

RCT_EXPORT_METHOD(hashTypedData:(NSString *)data
        callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"hashTypedData() method called");
#endif
    NSString *result = StatusgoHashTypedData(data);
    callback(@[result]);
}

RCT_EXPORT_METHOD(hashTypedDataV4:(NSString *)data
        callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"hashTypedDataV4() method called");
#endif
    NSString *result = StatusgoHashTypedDataV4(data);
    callback(@[result]);
}

#pragma mark - SignMessage

RCT_EXPORT_METHOD(signMessage:(NSString *)message
        callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"SignMessage() method called");
#endif
    NSString *result = StatusgoSignMessage(message);
    callback(@[result]);
}

#pragma mark - SignTypedData

RCT_EXPORT_METHOD(signTypedData:(NSString *)data
        account:(NSString *)account
        password:(NSString *)password
        callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"SignTypedData() method called");
#endif
    NSString *result = StatusgoSignTypedData(data, account, password);
    callback(@[result]);
}

#pragma mark - SignTypedDataV4

RCT_EXPORT_METHOD(signTypedDataV4:(NSString *)data
        account:(NSString *)account
        password:(NSString *)password
        callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"SignTypedDataV4() method called");
#endif
    NSString *result = StatusgoSignTypedDataV4(data, account, password);
    callback(@[result]);
}

@end
