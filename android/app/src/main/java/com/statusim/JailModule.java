package com.statusim;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

public class JailModule extends ReactContextBaseJavaModule {

    public JailModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "Jail";
    }

    @ReactMethod
    public void parse(String js, Callback succ, Callback fail) {
        succ.invoke("{\"commands\":{\"request-money\":" +
                "{\"description\":\"olala!\",\"name\":\"request-money\"," +
                "\"params\":{\"foo\":{\"type\":\"string\"}}}}," +
                "\"responses\":{}}");
    }
}

