#import "LogManager.h"
#import "React/RCTBridge.h"
#import "React/RCTEventDispatcher.h"
#import "Statusgo.h"
#import "Utils.h"
#import "SSZipArchive.h"
#import "StatusBackendClient.h"

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
    NSURL *rootUrl =[Utils getRootUrl];

    NSURL *zipFile = [rootUrl URLByAppendingPathComponent:@"logs.zip"];
    [fileManager removeItemAtPath:zipFile.path error:nil];

    NSURL *logsFolderName = [rootUrl URLByAppendingPathComponent:@"logs"];

    if (![fileManager fileExistsAtPath:logsFolderName.path])
        [fileManager createDirectoryAtPath:logsFolderName.path withIntermediateDirectories:YES attributes:nil error:&error];

    NSURL *dbFile = [logsFolderName URLByAppendingPathComponent:@"db.json"];
    NSURL *jsLogsFile = [logsFolderName URLByAppendingPathComponent:@"Status.log"];

    NSURL *mainGethLogsFile = [rootUrl URLByAppendingPathComponent:@"geth.log"];
    NSURL *mainLogsFile = [logsFolderName URLByAppendingPathComponent:@"geth.log"];

    NSURL *requestsLogFile = [rootUrl URLByAppendingPathComponent:@"api.log"];

    [dbJson writeToFile:dbFile.path atomically:YES encoding:NSUTF8StringEncoding error:nil];
    [jsLogs writeToFile:jsLogsFile.path atomically:YES encoding:NSUTF8StringEncoding error:nil];

    [fileManager copyItemAtPath:mainGethLogsFile.path toPath:mainLogsFile.path error:nil];
    
    if ([fileManager fileExistsAtPath:requestsLogFile.path]) {
        [fileManager copyItemAtPath:requestsLogFile.path toPath:[logsFolderName URLByAppendingPathComponent:@"api.log"].path error:nil];
    }

    [SSZipArchive createZipFileAtPath:zipFile.path withContentsOfDirectory:logsFolderName.path];
    [fileManager removeItemAtPath:logsFolderName.path error:nil];

    callback(@[zipFile.absoluteString]);
}

RCT_EXPORT_BLOCKING_SYNCHRONOUS_METHOD(logFileDirectory) {
    NSURL *rootUrl = [Utils getRootUrl];
    return rootUrl.path;
}

@end
