#import <Foundation/Foundation.h>
#import <JavaScriptCore/JavaScriptCore.h>

@protocol TimerJSExport <JSExport>

- (void)setTimeout:(JSValue *)callback
           inteval:(double)ms;

- (void)clearInterval:(NSString *)id;


- (NSString *)setInterval:(JSValue *)callback
                  inteval:(double)ms;

@end

@interface TimerJS : NSObject <TimerJSExport>
- (void)addToContext:(JSContext *)context;
@property (nonatomic) NSMutableDictionary * timers;
@end
