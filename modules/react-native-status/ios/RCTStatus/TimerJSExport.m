#import "TimerJSExport.h"

@implementation TimerJS

- (NSString *)setTimeout:(JSValue *)args {
    JSValue *callback = [args callWithArguments:@[@0]];
    double ms = [[args callWithArguments:@[@1]] toDouble];
    return [self createTimer:callback inteval:ms repeats:NO];
}

- (void)clearInterval:(NSString *)id {
    NSTimer *timer = [_timers objectForKey:id];
    if(timer != nil) {
        [timer invalidate];
    }
}


- (NSString *)setInterval:(JSValue *)args {
    JSValue *callback = [args callWithArguments:@[@0]];
    double ms = [[args callWithArguments:@[@1]] toDouble];
    return [self createTimer:callback inteval:ms repeats:YES];
}

- (NSString *)createTimer:(JSValue *)callback
                  inteval:(double)ms
                  repeats:(BOOL)repeats {
    if (_timers == nil) {
        _timers = [NSMutableDictionary dictionaryWithCapacity:1];
    }
    
    double interval = ms/1000.0;
    NSString *uuid = [[NSUUID UUID] UUIDString];
    
    dispatch_async( dispatch_get_main_queue(), ^{
        NSTimer *timer = [NSTimer scheduledTimerWithTimeInterval:interval
                                                         repeats:repeats
                                                           block:^(NSTimer * _Nonnull timer) {
                                                               [callback callWithArguments:nil];                                                           }];
        [_timers setObject:timer forKey:uuid];
    });
    
    return uuid;
}

- (void)addToContext:(JSContext *)context {
    [context setObject:self forKeyedSubscript:@"jsTimer"];
}

- (void)stopTimers {
    NSArray *keys = [_timers allKeys];
    for (NSString *key in keys) {
        NSTimer *timer = [_timers valueForKey:key];
        [timer invalidate];
        
        [_timers removeObjectForKey:key];
    }
}

@end
