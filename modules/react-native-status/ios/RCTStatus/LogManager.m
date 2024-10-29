#import "LogManager.h"
#import "React/RCTBridge.h"
#import "React/RCTEventDispatcher.h"
#import "Statusgo.h"
#import "Utils.h"
#import "SSZipArchive.h"

@implementation LogManager

RCT_EXPORT_MODULE();

#pragma mark - SendLogs method

RCT_EXPORT_METHOD(sendLogs:(NSString *)dbJson
                  jsLogs:(NSString *)jsLogs
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"SendLogs() method called");
#endif
    NSFileManager *fileManager = [NSFileManager defaultManager];
    NSError *error = nil;
    NSURL *rootUrl =[[fileManager
                      URLsForDirectory:NSLibraryDirectory inDomains:NSUserDomainMask]
                     lastObject];

    NSURL *zipFile = [rootUrl URLByAppendingPathComponent:@"logs.zip"];
    [fileManager removeItemAtPath:zipFile.path error:nil];

    NSURL *logsFolderName = [rootUrl URLByAppendingPathComponent:@"logs"];

    if (![fileManager fileExistsAtPath:logsFolderName.path])
        [fileManager createDirectoryAtPath:logsFolderName.path withIntermediateDirectories:YES attributes:nil error:&error];

    NSURL *dbFile = [logsFolderName URLByAppendingPathComponent:@"db.json"];
    NSURL *jsLogsFile = [logsFolderName URLByAppendingPathComponent:@"Status.log"];

    NSURL *mainGethLogsFile = [rootUrl URLByAppendingPathComponent:@"geth.log"];
    NSURL *mainLogsFile = [logsFolderName URLByAppendingPathComponent:@"geth.log"];

    NSURL *requestsLogFile = [rootUrl URLByAppendingPathComponent:@"requests.log"];

    [dbJson writeToFile:dbFile.path atomically:YES encoding:NSUTF8StringEncoding error:nil];
    [jsLogs writeToFile:jsLogsFile.path atomically:YES encoding:NSUTF8StringEncoding error:nil];

    [fileManager copyItemAtPath:mainGethLogsFile.path toPath:mainLogsFile.path error:nil];
    
    if ([fileManager fileExistsAtPath:requestsLogFile.path]) {
        [fileManager copyItemAtPath:requestsLogFile.path toPath:[logsFolderName URLByAppendingPathComponent:@"requests.log"].path error:nil];
    }

    [SSZipArchive createZipFileAtPath:zipFile.path withContentsOfDirectory:logsFolderName.path];
    [fileManager removeItemAtPath:logsFolderName.path error:nil];

    callback(@[zipFile.absoluteString]);
}

RCT_EXPORT_METHOD(initLogging:(BOOL)enabled
                  mobileSystem:(BOOL)mobileSystem
                  logLevel:(NSString *)logLevel
                  logRequestGo:(BOOL)logRequestGo
                  callback:(RCTResponseSenderBlock)callback)
{
    NSString *logDirectory = [self logFileDirectory];
    NSString *logFilePath = [logDirectory stringByAppendingPathComponent:@"geth.log"];
    NSString *logRequestFilePath = [logDirectory stringByAppendingPathComponent:@"requests.log"];

    NSMutableDictionary *jsonConfig = [NSMutableDictionary dictionary];
    jsonConfig[@"Enabled"] = @(enabled);
    jsonConfig[@"MobileSystem"] = @(mobileSystem);
    jsonConfig[@"Level"] = logLevel;
    jsonConfig[@"File"] = logFilePath;
    jsonConfig[@"LogRequestGo"] = @(logRequestGo);
    jsonConfig[@"LogRequestFile"] = logRequestFilePath;
    NSError *error = nil;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:jsonConfig options:0 error:&error];

    if (error) {
        // Handle JSON serialization error
        callback(@[error.localizedDescription]);
        return;
    }

    NSString *config = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];

    // Call your native logging initialization method here
    NSString *initResult = StatusgoInitLogging(config);

    callback(@[initResult]);
}

RCT_EXPORT_BLOCKING_SYNCHRONOUS_METHOD(logFileDirectory) {
    NSFileManager *fileManager = [NSFileManager defaultManager];
    NSURL *rootUrl =[[fileManager
                      URLsForDirectory:NSLibraryDirectory inDomains:NSUserDomainMask]
                     lastObject];
    return rootUrl.path;
}


@end
