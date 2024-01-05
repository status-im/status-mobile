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
    StatusgoExportUnencryptedDatabase(accountData, password, filePath);

    callback(@[filePath]);
}

RCT_EXPORT_METHOD(importUnencryptedDatabase:(NSString *)accountData
        password:(NSString *)password) {
#if DEBUG
    NSLog(@"importUnencryptedDatabase() method called");
#endif
    "";
}


@end