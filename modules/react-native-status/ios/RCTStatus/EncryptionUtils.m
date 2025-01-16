#import "EncryptionUtils.h"
#import "React/RCTBridge.h"
#import "React/RCTEventDispatcher.h"
#import "Statusgo.h"
#import "Utils.h"
#import "StatusBackendClient.h"

@implementation EncryptionUtils

RCT_EXPORT_MODULE();

#pragma mark - InitKeystore method

RCT_EXPORT_METHOD(initKeystore:(NSString *)keyUID
        callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"initKeystore() method called");
#endif
    NSURL *multiaccountKeystoreDir = [Utils getKeyStoreDirForKeyUID:keyUID];
    
    [StatusBackendClient executeStatusGoRequestWithCallback:@"InitKeystore"
                                                     body:multiaccountKeystoreDir.path
                                         statusgoFunction:^NSString *{
        return StatusgoInitKeystore(multiaccountKeystoreDir.path);
    }
                                                 callback:callback];
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
    
    [StatusBackendClient executeStatusGoRequestWithCallback:@"ChangeDatabasePasswordV2"
                                                     body:jsonString
                                         statusgoFunction:^NSString *{
        return StatusgoChangeDatabasePasswordV2(jsonString);
    }
                                                 callback:callback];
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
    
    // First initialize keystore
    [StatusBackendClient executeStatusGoRequest:@"InitKeystore"
                                         body:multiaccountKeystoreDir.path
                             statusgoFunction:^NSString *{
        return StatusgoInitKeystore(multiaccountKeystoreDir.path);
    }];
    
    // Prepare parameters for conversion
    NSDictionary *params = @{
        @"keyUID": keyUID,
        @"account": [NSJSONSerialization JSONObjectWithData:[accountData dataUsingEncoding:NSUTF8StringEncoding] 
                                                  options:0 
                                                    error:nil],
        @"settings": [NSJSONSerialization JSONObjectWithData:[settings dataUsingEncoding:NSUTF8StringEncoding] 
                                                   options:0 
                                                     error:nil],
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
    
    [StatusBackendClient executeStatusGoRequestWithCallback:@"ConvertToKeycardAccountV2"
                                                     body:jsonString
                                         statusgoFunction:^NSString *{
        return StatusgoConvertToKeycardAccountV2(jsonString);
    }
                                                 callback:callback];
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
    
    return [StatusBackendClient executeStatusGoRequestWithResult:@"EncodeTransferV2"
                                                          body:jsonString
                                              statusgoFunction:^NSString *{
        return StatusgoEncodeTransferV2(jsonString);
    }];
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
    
    return [StatusBackendClient executeStatusGoRequestWithResult:@"EncodeFunctionCallV2"
                                                          body:jsonString
                                              statusgoFunction:^NSString *{
        return StatusgoEncodeFunctionCallV2(jsonString);
    }];
}

RCT_EXPORT_BLOCKING_SYNCHRONOUS_METHOD(decodeParameters:(NSString *)decodeParamJSON) {
    return [StatusBackendClient executeStatusGoRequestWithResult:@"DecodeParameters"
                                                          body:decodeParamJSON
                                              statusgoFunction:^NSString *{
        return StatusgoDecodeParameters(decodeParamJSON);
    }];
}

RCT_EXPORT_BLOCKING_SYNCHRONOUS_METHOD(hexToNumber:(NSString *)hex) {
    return [StatusBackendClient executeStatusGoRequestWithResult:@"HexToNumber"
                                                          body:hex
                                              statusgoFunction:^NSString *{
        return StatusgoHexToNumber(hex);
    }];
}

RCT_EXPORT_BLOCKING_SYNCHRONOUS_METHOD(numberToHex:(NSString *)numString) {
    return [StatusBackendClient executeStatusGoRequestWithResult:@"NumberToHex"
                                                          body:numString
                                              statusgoFunction:^NSString *{
        return StatusgoNumberToHex(numString);
    }];
}

RCT_EXPORT_BLOCKING_SYNCHRONOUS_METHOD(sha3:(NSString *)str) {
    return [StatusBackendClient executeStatusGoRequestWithResult:@"Sha3"
                                                          body:str
                                              statusgoFunction:^NSString *{
        return StatusgoSha3(str);
    }];
}

RCT_EXPORT_BLOCKING_SYNCHRONOUS_METHOD(utf8ToHex:(NSString *)str) {
    return [StatusBackendClient executeStatusGoRequestWithResult:@"Utf8ToHex"
                                                          body:str
                                              statusgoFunction:^NSString *{
        return StatusgoUtf8ToHex(str);
    }];
}

RCT_EXPORT_BLOCKING_SYNCHRONOUS_METHOD(hexToUtf8:(NSString *)str) {
    return [StatusBackendClient executeStatusGoRequestWithResult:@"HexToUtf8"
                                                          body:str
                                              statusgoFunction:^NSString *{
        return StatusgoHexToUtf8(str);
    }];
}

RCT_EXPORT_BLOCKING_SYNCHRONOUS_METHOD(serializeLegacyKey:(NSString *)str) {
    return [StatusBackendClient executeStatusGoRequestWithResult:@"SerializeLegacyKey"
                                                          body:str
                                              statusgoFunction:^NSString *{
        return StatusgoSerializeLegacyKey(str);
    }];
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
    NSLog(@"hashTransaction() method called");
#endif
    [StatusBackendClient executeStatusGoRequestWithCallback:@"HashTransaction"
                                                     body:txArgsJSON
                                         statusgoFunction:^NSString *{
        return StatusgoHashTransaction(txArgsJSON);
    }
                                                 callback:callback];
}

RCT_EXPORT_METHOD(hashMessage:(NSString *)message
        callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"hashMessage() method called");
#endif
    [StatusBackendClient executeStatusGoRequestWithCallback:@"HashMessage"
                                                     body:message
                                         statusgoFunction:^NSString *{
        return StatusgoHashMessage(message);
    }
                                                 callback:callback];
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
    
    [StatusBackendClient executeStatusGoRequestWithCallback:@"MultiformatDeserializePublicKeyV2"
                                                     body:jsonString
                                         statusgoFunction:^NSString *{
        return StatusgoMultiformatDeserializePublicKeyV2(jsonString);
    }
                                                 callback:callback];
}

RCT_EXPORT_METHOD(deserializeAndCompressKey:(NSString *)desktopKey
        callback:(RCTResponseSenderBlock)callback) {
    [StatusBackendClient executeStatusGoRequestWithCallback:@"DeserializeAndCompressKey"
                                                     body:desktopKey
                                         statusgoFunction:^NSString *{
        return StatusgoDeserializeAndCompressKey(desktopKey);
    }
                                                 callback:callback];
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

RCT_EXPORT_METHOD(signMessage:(NSString *)rpcParams
                  callback:(RCTResponseSenderBlock)callback) {
    [StatusBackendClient executeStatusGoRequestWithCallback:@"SignMessage"
                                                     body:rpcParams
                                         statusgoFunction:^NSString *{
        return StatusgoSignMessage(rpcParams);
    }
                                                 callback:callback];
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
