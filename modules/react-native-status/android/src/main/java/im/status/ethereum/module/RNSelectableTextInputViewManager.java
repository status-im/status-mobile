package im.status.ethereum.module;

import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.Menu;
import android.view.MenuItem;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.facebook.react.views.textinput.ReactEditText;
import com.facebook.react.views.view.ReactViewGroup;
import com.facebook.react.views.view.ReactViewManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RNSelectableTextInputViewManager extends ReactViewManager {
    public static final String REACT_CLASS = "RNSelectableTextInput";
    private String[] _menuItems = new String[0];

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    public ReactViewGroup createViewInstance(ThemedReactContext context) {
        return new ReactViewGroup(context);
    }

    @ReactProp(name = "menuItems")
    public void setMenuItems(ReactViewGroup reactViewGroup, ReadableArray items) {
        if(items != null) {
            List<String> result = new ArrayList<String>(items.size());
            for (int i = 0; i < items.size(); i++) {
                result.add(items.getString(i));
            }
            this._menuItems = result.toArray(new String[items.size()]);
        }
    }

    public void registerSelectionListener(final ReactEditText view) {
        view.setCustomSelectionActionModeCallback(new Callback() {
            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                menu.clear();
                for (int i = 0; i < _menuItems.length; i++) {
                    menu.add(0, i, 0, _menuItems[i]);
                }
                return true;
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                int selectionStart = view.getSelectionStart();
                int selectionEnd = view.getSelectionEnd();
                String selectedText = view.getText().toString().substring(selectionStart, selectionEnd);

                // Dispatch event
                onSelectNativeEvent(view, item.getItemId(), selectedText, selectionStart, selectionEnd);

                mode.finish();

                return true;
            }

        });
    }

    public void onSelectNativeEvent(ReactEditText view, int eventType, String content, int selectionStart, int selectionEnd) {
        WritableMap event = Arguments.createMap();
        event.putInt("eventType", eventType);
        event.putString("content", content);
        event.putInt("selectionStart", selectionStart);
        event.putInt("selectionEnd", selectionEnd);

        // Dispatch
        ReactContext reactContext = (ReactContext) view.getContext();
        reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(view.getId(), "topSelection", event);
    }

    @Override
    public Map getExportedCustomDirectEventTypeConstants() {
        return MapBuilder.builder()
                         .put("topSelection", MapBuilder.of("registrationName","onSelection"))
                         .build();
    }
}
