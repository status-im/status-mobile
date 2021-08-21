package im.status.ethereum.rnclipboard;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;


import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class ClipboardAndroid extends ReactContextBaseJavaModule {
    ReactApplicationContext mContext;
    private ContentResolver cr;

    ClipboardAndroid(ReactApplicationContext context){
            super(context);
            this.mContext = context;
            this.cr = context.getContentResolver();
    }

    
    /*
    * Get clipboard objects and pass data to javascript callback to be called in React Native
    */
    @ReactMethod
    public void pasteEvent(Callback cb){
        ClipboardManager clipboardManager = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        if (!(clipboardManager.hasPrimaryClip())){
            cb.invoke(null);
        }
        else if (clipboardManager.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)){
            pasteString(clipboardManager.getPrimaryClip().getItemAt(0), cb);
        }
        else {
            pasteImage(clipboardManager.getPrimaryClip(), cb);
        }
    }

    @ReactMethod
    public void copyText(String copyString){
        ClipboardManager clipboard = (ClipboardManager)
                mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("simple text", copyString);
        clipboard.setPrimaryClip(clip);
    }

    private void pasteString(ClipData.Item dataItem, Callback dataCall){
        String pasteString = (String) dataItem.getText();
        if (pasteString != null){
            dataCall.invoke(pasteString);
        }
    }

    private void pasteImage(ClipData clipData, Callback cb){
        Log.i(mContext.getApplicationInfo().packageName, "the paste is called");
        if(clipData != null){
            ClipData.Item item = clipData.getItemAt(0);
            Uri pasteUri = item.getUri();
            if (pasteUri != null){
                String mimeType = cr.getType(pasteUri);
                if (mimeType != null){
                    Log.i(mContext.getApplicationInfo().packageName, "mimetype is " + mimeType);
                    if (mimeType.equals("image/jpeg") || mimeType.equals("image/png") || mimeType.equals("image/jpg")){
                        String imgPath = pasteUri.getPath();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(cr, pasteUri);
                            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                            if (mimeType.equals("image/jpeg") || mimeType.equals("image/jpg")){
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                            }
                            if (mimeType.equals("image/png")){
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                            }
                            byte[] byteArray = outputStream.toByteArray();
                            String encodedString = Base64.encodeToString(byteArray, Base64.DEFAULT);
                            StringBuilder builder = new StringBuilder("data:" + mimeType + ";base64,").append(encodedString);
                            cb.invoke(builder.toString());
                            Log.i(mContext.getApplicationInfo().packageName, "the image string " + encodedString);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e(mContext.getApplicationInfo().packageName, e.getLocalizedMessage());
                        }
                        Log.i(mContext.getApplicationInfo().packageName, "the paste is path " + imgPath);
                    }

                }
            }
        }
    }

    @NonNull
    @Override
    public String getName() {
        return "ClipboardAndroid";
    }
}
