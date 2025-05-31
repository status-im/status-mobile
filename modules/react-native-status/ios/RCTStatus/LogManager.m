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
                  usePublicLogDir:(BOOL)usePublicLogDir
                  callback:(RCTResponseSenderBlock)callback) {
#if DEBUG
    NSLog(@"SendLogs() method called");
#endif
    NSFileManager *fileManager = [NSFileManager defaultManager];
    NSURL *rootUrl = [Utils getRootUrl];
    
    // Prepare output paths
    NSURL *zipFilePath = [rootUrl URLByAppendingPathComponent:@"logs.zip"];
    NSURL *logsFolderPath = [rootUrl URLByAppendingPathComponent:@"logs_"];
    
    [fileManager removeItemAtPath:zipFilePath.path error:nil];
    
    [self ensureDirectoryExists:logsFolderPath withFileManager:fileManager];
    
    [self saveProvidedLogs:dbJson jsLogs:jsLogs toFolder:logsFolderPath];
    
    [self copyExistingLogsToFolder:logsFolderPath withFileManager:fileManager];
    
    // Create zip archive and clean up
    [SSZipArchive createZipFileAtPath:zipFilePath.path withContentsOfDirectory:logsFolderPath.path];
    [fileManager removeItemAtPath:logsFolderPath.path error:nil];
    
    callback(@[zipFilePath.absoluteString]);
}

- (void)ensureDirectoryExists:(NSURL *)directoryPath withFileManager:(NSFileManager *)fileManager {
    if (![fileManager fileExistsAtPath:directoryPath.path]) {
        NSError *error = nil;
        [fileManager createDirectoryAtPath:directoryPath.path 
                withIntermediateDirectories:YES 
                                 attributes:nil 
                                      error:&error];
    }
}

- (void)saveProvidedLogs:(NSString *)dbJson jsLogs:(NSString *)jsLogs toFolder:(NSURL *)logsFolderPath {
    NSURL *dbFile = [logsFolderPath URLByAppendingPathComponent:@"db.json"];
    NSURL *jsLogsFile = [logsFolderPath URLByAppendingPathComponent:@"Status.log"];
    
    [dbJson writeToFile:dbFile.path atomically:YES encoding:NSUTF8StringEncoding error:nil];
    [jsLogs writeToFile:jsLogsFile.path atomically:YES encoding:NSUTF8StringEncoding error:nil];
}

- (void)copyExistingLogsToFolder:(NSURL *)logsFolderPath withFileManager:(NSFileManager *)fileManager {
    NSString *logDirectory = [self logFileDirectory:NO];
    
    // Get all files in the log directory
    NSError *error = nil;
    NSArray *fileList = [fileManager contentsOfDirectoryAtPath:logDirectory error:&error];
    if (error) {
        NSLog(@"Error reading log directory: %@", error);
        return;
    }
    
    for (NSString *fileName in fileList) {
        NSString *sourcePath = [logDirectory stringByAppendingPathComponent:fileName];
        BOOL isDirectory;
        
        // Only copy files, skip directories
        [fileManager fileExistsAtPath:sourcePath isDirectory:&isDirectory];
        if (!isDirectory) {
            NSString *destinationPath = [logsFolderPath.path stringByAppendingPathComponent:fileName];
            [fileManager copyItemAtPath:sourcePath toPath:destinationPath error:nil];
        }
    }
}

RCT_EXPORT_BLOCKING_SYNCHRONOUS_METHOD(logFileDirectory:(BOOL)usePublicLogDir) {
    NSURL *rootUrl = [Utils getRootUrl];
    NSURL *logsUrl = [rootUrl URLByAppendingPathComponent:@"logs"];
    return logsUrl.path;
}

RCT_EXPORT_BLOCKING_SYNCHRONOUS_METHOD(setProfileLogLevel:(NSString *)setProfileLogLevelRequest) {
    return [StatusBackendClient executeStatusGoRequestWithResult:@"SetProfileLogLevel"
                                                          body:setProfileLogLevelRequest
                                              statusgoFunction:^NSString *{
        return StatusgoSetProfileLogLevel(setProfileLogLevelRequest);
    }];
}

RCT_EXPORT_BLOCKING_SYNCHRONOUS_METHOD(setProfileLogEnabled:(NSString *)setProfileLogEnabledRequest) {
    return [StatusBackendClient executeStatusGoRequestWithResult:@"SetProfileLogEnabled"
                                                          body:setProfileLogEnabledRequest
                                              statusgoFunction:^NSString *{
        return StatusgoSetProfileLogEnabled(setProfileLogEnabledRequest);
    }];
}

RCT_EXPORT_METHOD(setPreLoginLogLevel:(NSString *)setPreLoginLogLevelRequest) {
    [StatusBackendClient executeStatusGoRequest:@"SetPreLoginLogLevel"
                                                          body:setPreLoginLogLevelRequest
                                              statusgoFunction:^NSString *{
        return StatusgoSetPreLoginLogLevel(setPreLoginLogLevelRequest);
    }];
}

RCT_EXPORT_METHOD(setPreLoginLogEnabled:(NSString *)setPreLoginLogEnabledRequest) {
    [StatusBackendClient executeStatusGoRequest:@"SetPreLoginLogEnabled"
                                                          body:setPreLoginLogEnabledRequest
                                              statusgoFunction:^NSString *{
        return StatusgoSetPreLoginLogEnabled(setPreLoginLogEnabledRequest);
    }];
}

@end
