# FAQ

## Why do we use async JS calls in RN but sometimes sync calls for bots?

(Based on answer by @roma in Slack on August 15, 2017)

Different JS environments have different capabilities. In general we want to use async.

**React Native**
1) RN doesn’t support sync http calls (that’s why we didn’t use sync calls with web3 when we used http provider)
2) RN doesn’t allow to make sync calls to native modules, currently we are calling CallRPC through native modules, that’s why calls can be only async

**Dapps**

Dapps are running in webview env which supports sync calls in some cases (only android), but we don’t support sync calls for iOS, that will not work. Mostly any call to web3 causes http request, so using sync call is a bad idea in one threaded env, in any case. So i wouldn’t use it in dapps.

**Bots in Otto VM**

During the last year both sync and async calls  were handled by the same function and tbh that was sync call with only difference: in case when we pass callback it was called instead of returning the value. But the call was blocking. I’m not sure if it was implemented, but there was an issues about making async calls in otto really async.

**Summary**
- Both sync and async calls are available in otto
- Only async in RN
- Only async in iOS webview
- Both async/sync in Android webview
