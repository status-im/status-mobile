#import "StatusBackendClient.h"
#import "RCTStatus.h"
#import "Utils.h"

@implementation StatusBackendClient {
    NSURLSessionWebSocketTask *_webSocket;
    BOOL _serverEnabled;
    NSString *_statusGoEndpoint;
    NSString *_signalEndpoint;
    NSString *_rootDataDir;
}

RCT_EXPORT_MODULE();

+ (BOOL)requiresMainQueueSetup {
    return YES;
}

+ (instancetype)allocWithZone:(NSZone *)zone {
    static StatusBackendClient *sharedInstance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedInstance = [super allocWithZone:zone];
    });
    return sharedInstance;
}

+ (instancetype)sharedInstance {
    return [[self alloc] init];
}

- (instancetype)init {
    static StatusBackendClient *sharedInstance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedInstance = [super init];
        if (sharedInstance) {
            sharedInstance->_serverEnabled = NO;
            sharedInstance->_statusGoEndpoint = nil;
            sharedInstance->_signalEndpoint = nil;
            sharedInstance->_rootDataDir = nil;
            sharedInstance->_webSocket = nil;
        }
    });
    return sharedInstance;
}

- (id)copyWithZone:(NSZone *)zone {
    return self;
}

- (BOOL)serverEnabled {
    return _serverEnabled;
}

- (NSString *)statusGoEndpoint {
    return _statusGoEndpoint;
}

- (NSString *)signalEndpoint {
    return _signalEndpoint;
}

- (NSString *)rootDataDir {
    return _rootDataDir;
}

- (void)connectWebSocket {
    if (!self.serverEnabled || !self.signalEndpoint) {
        return;
    }
    
    NSString *fullUrl = [NSString stringWithFormat:@"%@", self.signalEndpoint];
    NSURL *url = [NSURL URLWithString:fullUrl];
    NSURLSession *session = [NSURLSession sessionWithConfiguration:[NSURLSessionConfiguration defaultSessionConfiguration]];
    self.webSocket = [session webSocketTaskWithURL:url];
    
    [self.webSocket resume];
    [self receiveMessage];
}

- (void)receiveMessage {
    __weak typeof(self) weakSelf = self;
    [self.webSocket receiveMessageWithCompletionHandler:^(NSURLSessionWebSocketMessage *message, NSError *error) {
        __strong typeof(weakSelf) strongSelf = weakSelf;
        if (!strongSelf || !strongSelf.webSocket) {
            return;
        }

        if (error) {
            NSLog(@"WebSocket error: %@", error);
            // Attempt to reconnect after error
            dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                [strongSelf connectWebSocket];
            });
            return;
        }
        
        if (message) {
            if (message.type == NSURLSessionWebSocketMessageTypeString && message.string) {
                [[Status sharedInstance] handleSignal:message.string];
            }
            // Continue receiving messages only if the connection is still active
            if (strongSelf.webSocket) {
                [strongSelf receiveMessage];
            }
        }
    }];
}

- (void)disconnectWebSocket {
    if (self.webSocket) {
        [self.webSocket cancel];
        self.webSocket = nil;
    }
}

- (void)request:(NSString *)endpoint
                  body:(NSString *)body
             callback:(void (^)(NSString *response, NSError *error))callback {
    NSString *fullUrlString = [NSString stringWithFormat:@"%@%@", self.statusGoEndpoint, endpoint];
    NSURL *url = [NSURL URLWithString:fullUrlString];
    NSMutableURLRequest *request = [[NSMutableURLRequest alloc] initWithURL:url];
    request.HTTPMethod = @"POST";
    [request setValue:@"application/json; charset=utf-8" forHTTPHeaderField:@"Content-Type"];
    
    NSData *bodyData = [body dataUsingEncoding:NSUTF8StringEncoding];
    request.HTTPBody = bodyData;
    
    NSURLSessionConfiguration *config = [NSURLSessionConfiguration defaultSessionConfiguration];
    NSURLSession *session = [NSURLSession sessionWithConfiguration:config];
    
    NSURLSessionDataTask *task = [session dataTaskWithRequest:request
                                            completionHandler:^(NSData *data, NSURLResponse *response, NSError *error) {
        if (error) {
            NSLog(@"request error: %@", error);
            callback(nil, error);
            return;
        }
        
        NSString *responseString = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
        callback(responseString, nil);
    }];
    
    [task resume];
}

RCT_EXPORT_METHOD(configStatusBackendServer:(BOOL)serverEnabled
                  statusGoEndpoint:(NSString *)statusGoEndpoint
                  signalEndpoint:(NSString *)signalEndpoint
                  rootDataDir:(NSString *)rootDataDir) {
    [self configureWithEnabled:serverEnabled
               statusGoEndpoint:statusGoEndpoint
                signalEndpoint:signalEndpoint
                  rootDataDir:rootDataDir];
}

- (void)configureWithEnabled:(BOOL)serverEnabled
              statusGoEndpoint:(NSString *)statusGoEndpoint
               signalEndpoint:(NSString *)signalEndpoint
                 rootDataDir:(NSString *)rootDataDir {
    _serverEnabled = serverEnabled;
    
    if (serverEnabled) {
        _statusGoEndpoint = statusGoEndpoint;
        _signalEndpoint = signalEndpoint;
        _rootDataDir = rootDataDir;
        [self connectWebSocket];
    } else {
        [self disconnectWebSocket];
        _statusGoEndpoint = nil;
        _signalEndpoint = nil;
        _rootDataDir = nil;
    }
}

+ (void)executeStatusGoRequest:(NSString *)endpoint
                         body:(NSString *)body
             statusgoFunction:(NSString * (^)(void))statusgoFunction {
    StatusBackendClient *client = [StatusBackendClient sharedInstance];
    if (client.serverEnabled) {
        [client request:endpoint body:body callback:^(NSString *response, NSError *error) {
            [Utils handleStatusGoResponse:response source:endpoint error:error];
        }];
    } else {
        NSString *result = statusgoFunction();
        [Utils handleStatusGoResponse:result source:endpoint error:nil];
    }
}

+ (void)executeStatusGoRequestWithCallback:(NSString *)endpoint
                                    body:(NSString *)body
                        statusgoFunction:(NSString * (^)(void))statusgoFunction
                              callback:(RCTResponseSenderBlock)callback {
    StatusBackendClient *client = [StatusBackendClient sharedInstance];
    if (client.serverEnabled) {
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
            [client request:endpoint body:body callback:^(NSString *response, NSError *error) {
                if (error) {
                    NSLog(@"request to %@ failed: %@", endpoint, error);
                    if (callback) {
                        callback(@[@(NO)]);
                    }
                } else {
                    if (callback) {
                        callback(@[response]);
                    }
                }
            }];
        });
    } else {
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
            NSString *result = statusgoFunction();
            if (callback) {
                callback(@[result]);
            }
        });
    }
}

+ (NSString *)executeStatusGoRequestWithResult:(NSString *)endpoint
                                        body:(NSString *)body
                            statusgoFunction:(NSString * (^)(void))statusgoFunction {
    StatusBackendClient *client = [StatusBackendClient sharedInstance];
    if (client.serverEnabled) {
        dispatch_semaphore_t semaphore = dispatch_semaphore_create(0);
        __block NSString *resultString = @"";
        
        [client request:endpoint body:body callback:^(NSString *response, NSError *error) {
            if (error) {
                NSLog(@"request to %@ failed: %@", endpoint, error);
                resultString = @"";
            } else {
                resultString = response;
            }
            dispatch_semaphore_signal(semaphore);
        }];
        
        dispatch_semaphore_wait(semaphore, DISPATCH_TIME_FOREVER);
        return resultString;
    } else {
        return statusgoFunction();
    }
}

@end 
