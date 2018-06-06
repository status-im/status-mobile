#import "Jail.h"
#import "RCTStatus.h"

//source: http://stackoverflow.com/a/23387659/828487
#define NSStringMultiline(...) [[NSString alloc] initWithCString:#__VA_ARGS__ encoding:NSUTF8StringEncoding]

@implementation HandlersJs

- (void)log:(JSValue *)data
{
    NSLog(@"jail log: %@", [data toString]);
}

- (NSString *)send:(JSValue *)payload
{
    char * result = CallRPC((char *) [[payload toString] UTF8String]);
    
    return [NSString stringWithUTF8String: result];
}

- (void)sendAsync:(JSValue *)args
{
    // TODO(rasom): fix this black magic, need to figure how to pass more than one
    // parameter to sendAsync
    JSValue *payload = [args callWithArguments:@[@0]];
    JSValue *callback = [args callWithArguments:@[@1]];
    
    dispatch_async( dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        NSString *result = [self send:payload];
        dispatch_async( dispatch_get_main_queue(), ^{
            [callback callWithArguments:@[result]];
        });
    });
}

- (BOOL)isConnected
{
    return YES;
}

- (void)sendSignal:(JSValue *)data
{
    [Status jailEvent:_chatId data:[data toString]];
}

- (void)setChatId:(NSString *)chatId
{
    _chatId = chatId;
}

- (void)addToContext:(JSContext *)context
{
    [context setObject:self forKeyedSubscript:@"statusNativeHandlers"];
}

@end

@implementation Cell
@end

@implementation Jail

- (void)initJail:(NSString *)js {
    _initialJs = js;
}

- (NSDictionary *)parseJail:(NSString *)chatId
                   withCode:(NSString *)js {
    Cell *cell = [self createCell:chatId withCode:js];
    JSContext *context = cell.context;
    [_cells setValue:cell forKey:chatId];
    
    JSValue *catalog = context[@"catalog"];;
    JSValue *exception = [context exception];
    NSString *error;
    if(exception != nil) {
        error = [exception toString];
        [context setException:nil];
    }
    NSDictionary *result = [[NSDictionary alloc] initWithObjectsAndKeys:[catalog toString], @"result", error, @"error", nil];
    
    return result;
}

- (NSDictionary *)call:(NSString *)chatId
                  path:(NSString *)path
                params:(NSString *)params {
    Cell *cell = [_cells valueForKey:chatId];
    JSContext *context = cell.context;
    if(cell == nil) {
        return [[NSDictionary alloc] initWithObjectsAndKeys:nil, @"result", @"jail is not initialized", @"error", nil];
    }
    JSValue *callResult = [context[@"call"] callWithArguments:@[path, params]];
    JSValue *exception = [context exception];
    NSString *error;
    if(exception != nil) {
        error = [exception toString];
        [context setException:nil];
    }
    NSDictionary *result = [[NSDictionary alloc] initWithObjectsAndKeys:[callResult toString], @"result", error, @"error", nil];
    
    return result;
}

- (Cell *)createCell:(NSString *)chatId
                 withCode:(NSString *)js {
    if(_cells == nil) {
        _cells = [NSMutableDictionary dictionaryWithCapacity:1];
    }
    
    Cell * cell = [Cell new];
    JSContext *context = [JSContext new];
    [context setName:chatId];
    cell.context = context;
    
    HandlersJs *handlers = [HandlersJs new];
    [handlers setChatId:chatId];
    [handlers addToContext:context];
    
    [self addTimer:cell];
    [context evaluateScript:_initialJs];
    
    JSValue *excep = [context exception];
    NSLog(@"err1 %@", [excep toString]);
    
    NSString *webJs =
    NSStringMultiline
    (
     var statusSignals = {
     sendSignal: function (s) {statusNativeHandlers.sendSignal(s);}
     };
     var setTimeout = function (fn, t) {
         var args = [fn, t];
         var getItem = function(idx) {return args[idx];};
         return jsTimer.setTimeout(getItem);
     };
     var setInterval = function (fn, t) {
         var args = [fn, t];
         var getItem = function(idx) {return args[idx];};
         return jsTimer.setInterval(getItem);
     };
     var clearInterval = function (id) {
         jsTimer.clearInterval(id);
     };
     var Web3 = require('web3');
     var provider = {
     send: function (payload) {
         var result = statusNativeHandlers.send(JSON.stringify(payload));
         return JSON.parse(result);
     },
     sendAsync: function (payload, callback) {
         var wrappedCallback = function (result) {
             console.log(result);
             var error = null;
             try {
                 result = JSON.parse(result);
             } catch (e) {
                 error = result;
             }
             callback(error, result);
         };
         var args = [JSON.stringify(payload), wrappedCallback];
         var getItem = function(idx) {return args[idx];};
         statusNativeHandlers.sendAsync(getItem);
     }
     };
     var web3 = new Web3(provider);
     var console = {
     log: function (data) {
         statusNativeHandlers.log(data);
     }
     };
     var Bignumber = require("bignumber.js");
     function bn(val){
         return new Bignumber(val);
     }
     
     );
    

    [context evaluateScript:webJs];
    excep = [context exception];
    NSLog(@"err2 %@", [excep toString]);
    [context evaluateScript:js];
    excep = [context exception];
    NSLog(@"err3 %@", [excep toString]);
    [context evaluateScript:@"var catalog = JSON.stringify(_status_catalog);"];
    excep = [context exception];
    NSLog(@"err4 %@", [excep toString]);
    
    return cell;
}

- (void)addTimer:(Cell *)cell {
    TimerJS *timer = [TimerJS new];
    cell.timer = timer;
    
    [timer addToContext:cell.context];
}

-(void)reset {
    NSArray *keys = [_cells allKeys];
    for (NSString *key in keys) {
        Cell *cell = [_cells valueForKey:key];
        TimerJS *timer = cell.timer;
        [timer stopTimers];
        [_cells removeObjectForKey:key];
    }
}

@end
