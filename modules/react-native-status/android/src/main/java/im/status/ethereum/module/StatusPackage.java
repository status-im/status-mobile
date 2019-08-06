package im.status.ethereum.module;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import im.status.ethereum.function.Function;
import statusgo.Statusgo;

public class StatusPackage implements ReactPackage {

    private boolean rootedDevice;

    public StatusPackage(boolean rootedDevice) {
        this.rootedDevice = rootedDevice;
    }

    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
        List<NativeModule> modules = new ArrayList<>();

        modules.add(new StatusModule(reactContext, this.rootedDevice));

        return modules;
    }

    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
        return Collections.emptyList();
    }

    public Function<String, String> getCallRPC() {
        return new Function<String, String>() {
            @Override
            public String apply(String payload) {
                return Statusgo.callRPC(payload);
            }
        };
    }
}
