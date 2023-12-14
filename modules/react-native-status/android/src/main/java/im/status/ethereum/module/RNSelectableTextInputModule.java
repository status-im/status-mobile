package im.status.ethereum.module;

import android.view.ActionMode;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.uimanager.NativeViewHierarchyManager;
import com.facebook.react.uimanager.UIBlock;
import com.facebook.react.uimanager.UIManagerModule;
import com.facebook.react.views.textinput.ReactEditText;
import javax.annotation.Nonnull;

class RNSelectableTextInputModule extends ReactContextBaseJavaModule {

    private ActionMode lastActionMode;

    public RNSelectableTextInputModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Nonnull
    @Override
    public String getName() {
        return "RNSelectableTextInputManager";
    }

    @ReactMethod
    public void setupMenuItems(final Integer selectableTextViewReactTag, final Integer textInputReactTag) {
        ReactApplicationContext reactContext = this.getReactApplicationContext();
        UIManagerModule uiManager = reactContext.getNativeModule(UIManagerModule.class);
        uiManager.addUIBlock(new UIBlock() {
            public void execute (NativeViewHierarchyManager nvhm) {
                RNSelectableTextInputViewManager rnSelectableTextManager = (RNSelectableTextInputViewManager) nvhm.resolveViewManager(selectableTextViewReactTag);
                ReactEditText reactTextView = (ReactEditText) nvhm.resolveView(textInputReactTag);
                rnSelectableTextManager.registerSelectionListener(reactTextView);
            }
        });
    }

    @ReactMethod
    public void startActionMode(final Integer textInputReactTag) {
        ReactApplicationContext reactContext = this.getReactApplicationContext();
        UIManagerModule uiManager = reactContext.getNativeModule(UIManagerModule.class);
        uiManager.addUIBlock(new UIBlock() {
            public void execute (NativeViewHierarchyManager nvhm) {
                ReactEditText reactTextView = (ReactEditText) nvhm.resolveView(textInputReactTag);
                lastActionMode = reactTextView.startActionMode(reactTextView.getCustomSelectionActionModeCallback(), ActionMode.TYPE_FLOATING);
            }
        });
    }

    @ReactMethod
    public void hideLastActionMode(){
        ReactApplicationContext reactContext = this.getReactApplicationContext();
        UIManagerModule uiManager = reactContext.getNativeModule(UIManagerModule.class);
        uiManager.addUIBlock(new UIBlock() {
            public void execute (NativeViewHierarchyManager nvhm) {
                if(lastActionMode!=null){
                    lastActionMode.finish();
                    lastActionMode = null;
                }
            }
        });
    }

    @ReactMethod
    public void setSelection(final Integer textInputReactTag, final Integer start, final Integer end){
        ReactApplicationContext reactContext = this.getReactApplicationContext();
        UIManagerModule uiManager = reactContext.getNativeModule(UIManagerModule.class);
        uiManager.addUIBlock(new UIBlock() {
            public void execute (NativeViewHierarchyManager nvhm) {
                ReactEditText reactTextView = (ReactEditText) nvhm.resolveView(textInputReactTag);
                reactTextView.setSelection(start, end);
            }
        });
    }
 
}
