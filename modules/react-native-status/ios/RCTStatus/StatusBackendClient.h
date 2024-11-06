#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>

@interface StatusBackendClient : NSObject <RCTBridgeModule>

@property (nonatomic) BOOL serverEnabled;
@property (nonatomic, strong) NSString *statusGoEndpoint;
@property (nonatomic, strong) NSString *signalEndpoint;
@property (nonatomic, strong) NSString *rootDataDir;
@property (nonatomic, strong) NSURLSessionWebSocketTask *webSocket;

// Add sharedInstance class method declaration
+ (instancetype)sharedInstance;
- (void)request:(NSString *)endpoint
                  body:(NSString *)body
             callback:(void (^)(NSString *response, NSError *error))callback;

+ (void)executeStatusGoRequest:(NSString *)endpoint
                         body:(NSString *)body
             statusgoFunction:(NSString * (^)(void))statusgoFunction;

+ (void)executeStatusGoRequestWithCallback:(NSString *)endpoint
                                    body:(NSString *)body
                        statusgoFunction:(NSString * (^)(void))statusgoFunction
                              callback:(RCTResponseSenderBlock)callback;

+ (NSString *)executeStatusGoRequestWithResult:(NSString *)endpoint
                                        body:(NSString *)body
                            statusgoFunction:(NSString * (^)(void))statusgoFunction;

@end 
