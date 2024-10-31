#import "DatabaseManager.h"
#import "React/RCTBridge.h"
#import "React/RCTEventDispatcher.h"
#import "Statusgo.h"
#import "Utils.h"
#import "StatusBackendClient.h"
@implementation DatabaseManager

RCT_EXPORT_MODULE();

RCT_EXPORT_METHOD(exportUnencryptedDatabase:(NSString *)accountData
        password:(NSString *)password
        callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"exportUnencryptedDatabase() method called");
#endif

    NSString *filePath = [Utils getExportDbFilePath];
    
    NSDictionary *params = @{
        @"account": [NSJSONSerialization JSONObjectWithData:[accountData dataUsingEncoding:NSUTF8StringEncoding] options:0 error:nil],
        @"password": password,
        @"databasePath": filePath
    };
    
    NSError *error;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:params options:0 error:&error];
    
    if (error) {
        NSLog(@"Error creating JSON: %@", [error localizedDescription]);
        callback(@[filePath]);
        return;
    }
    
    NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    
    [StatusBackendClient executeStatusGoRequestWithCallback:@"ExportUnencryptedDatabaseV2"
                                                     body:jsonString
                                        statusgoFunction:^NSString *{
        return StatusgoExportUnencryptedDatabaseV2(jsonString);
    }
                                                 callback:nil];
    
    callback(@[filePath]);
}

RCT_EXPORT_METHOD(importUnencryptedDatabase:(NSString *)accountData
        password:(NSString *)password) {
#if DEBUG
    NSLog(@"importUnencryptedDatabase() method called");
#endif
    
    NSString *filePath = [Utils getExportDbFilePath];
    [Utils migrateKeystore:accountData password:password];
    
    NSDictionary *params = @{
        @"account": [NSJSONSerialization JSONObjectWithData:[accountData dataUsingEncoding:NSUTF8StringEncoding] options:0 error:nil],
        @"password": password,
        @"databasePath": filePath
    };
    
    NSError *error;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:params options:0 error:&error];
    
    if (error) {
        NSLog(@"Error creating JSON: %@", [error localizedDescription]);
        return;
    }
    
    NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    
    [StatusBackendClient executeStatusGoRequest:@"ImportUnencryptedDatabaseV2"
                                         body:jsonString
                             statusgoFunction:^NSString *{
        return StatusgoImportUnencryptedDatabaseV2(jsonString);
    }];
}


@end
