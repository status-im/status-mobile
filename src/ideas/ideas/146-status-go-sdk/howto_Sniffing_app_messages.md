## HOWTO : Sniffing app messages

So one of the main points of this swarm is to interact with status app, to do that, we need to speak its same language.

To do that the easiest way is to sniff messages sent by the app, to do this you can:

1.- Clone status-react
2.- Add a log line on [StatusModule.java::sendWeb3Request method](https://github.com/status-im/status-react/blob/develop/modules/react-native-status/android/src/main/java/im/status/ethereum/module/StatusModule.java#L690) printing the payload like:

```
Thread thread = new Thread() {
    @Override
    public void run() {
+                Log.d("PAYLOAD", "PAYLOAD : " + payload);
+
        String res = Statusgo.CallRPC(payload);
        callback.invoke(res);
    }
```
3.- [Deploy it to your devide/emulator](https://wiki.status.im/Building_Status)
4.- Connect to your emulator and check the logs `adb logcat|grep "PAYLOAD"`
