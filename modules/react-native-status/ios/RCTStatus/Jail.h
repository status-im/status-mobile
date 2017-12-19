#import <Foundation/Foundation.h>
#import <JavaScriptCore/JavaScriptCore.h>
#import <Statusgo/Statusgo.h>
#import "TimerJSExport.h"

@interface Jail : NSObject
- (void)initJail:(NSString *)js;

- (NSDictionary *)parseJail:(NSString *)chatId
                   withCode:(NSString *)js;

- (NSDictionary *)call:(NSString *)chatId
                  path:(NSString *)path
                params:(NSString *)params;

- (void)reset;

@property (nonatomic) NSMutableDictionary * cells;
@property (nonatomic) NSString * initialJs;
@property (nonatomic) TimerJS * timer;

@end

@interface Cell : NSObject
@property (nonatomic)JSContext * context;
@property (nonatomic)TimerJS * timer;
@end

@protocol HandlersJSExport <JSExport>

- (void)log:(JSValue *)data;
- (NSString *)send:(JSValue *)payload;
- (void)sendAsync:(JSValue *)payload;
- (BOOL)isConnected;
- (void)sendSignal:(JSValue *)data;

@end

@interface HandlersJs : NSObject <HandlersJSExport>
@property (nonatomic) NSString * chatId;
@end
