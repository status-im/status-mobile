#import "DatabaseManager.h"
#import "React/RCTBridge.h"
#import "React/RCTEventDispatcher.h"
#import "Statusgo.h"
#import "Utils.h"

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
    
    StatusgoExportUnencryptedDatabaseV2(jsonString);
    
    callback(@[filePath]);
}

RCT_EXPORT_METHOD(importUnencryptedDatabase:(NSString *)accountData
        password:(NSString *)password) {
#if DEBUG
    NSLog(@"importUnencryptedDatabase() method called");
#endif
    NSDictionary *params = @{
        @"account": [NSJSONSerialization JSONObjectWithData:[accountData dataUsingEncoding:NSUTF8StringEncoding] options:0 error:nil],
        @"password": password
    };
    
    NSError *error;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:params options:0 error:&error];
    
    if (error) {
        NSLog(@"Error creating JSON: %@", [error localizedDescription]);
        return;
    }
    
    NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    
    StatusgoImportUnencryptedDatabaseV2(jsonString);
}


@end
