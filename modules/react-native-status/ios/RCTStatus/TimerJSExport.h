#import <Foundation/Foundation.h>
#import <JavaScriptCore/JavaScriptCore.h>

@protocol TimerJSExport <JSExport>

- (NSString *)setTimeout:(JSValue *)args;

- (void)clearInterval:(NSString *)id;

- (NSString *)setInterval:(JSValue *)args;

@end

@interface TimerJS : NSObject <TimerJSExport>
- (void)addToContext:(JSContext *)context;
- (void)stopTimers;
@property (nonatomic) NSMutableDictionary * timers;
@end
