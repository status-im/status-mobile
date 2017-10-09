#import "TimerJSExport.h"

@implementation TimerJS

- (void)setTimeout:(JSValue *)callback
           inteval:(double)ms
{
    [self createTimer:callback inteval:ms repeats:NO];
}

- (void)clearInterval:(NSString *)id
{
    NSTimer *timer = [_timers objectForKey:id];
    if(timer != nil) {
        [timer invalidate];
    }
}


- (NSString *)setInterval:(JSValue *)callback
                  inteval:(double)ms
{
    return [self createTimer:callback inteval:ms repeats:YES];
}

- (NSString *)createTimer:(JSValue *)callback
                  inteval:(double)ms
                  repeats:(BOOL)repeats
{
    if (_timers == nil) {
        _timers = [NSMutableDictionary dictionaryWithCapacity:1];
    }
    
    double interval = ms/1000.0;
    NSString *uuid = [[NSUUID UUID] UUIDString];
    
    dispatch_async( dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        NSTimer *timer = [NSTimer scheduledTimerWithTimeInterval:interval
                                                         repeats:repeats
                                                           block:^(NSTimer * _Nonnull timer) {
                                                               [callback callWithArguments:nil];
                                                           }];
        [_timers setObject:timer forKey:uuid];
    });
    
    return uuid;
}

- (void)addToContext:(JSContext *)context
{
    [context setObject:self forKeyedSubscript:@"jsTimer"];
}

@end
