package im.status.ethereum.module;

import android.os.Build;
import com.facebook.react.bridge.Callback;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Web3Bridge {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private OkHttpClient client;

    public Web3Bridge() {
        OkHttpClient.Builder b = new OkHttpClient.Builder();
        b.readTimeout(310, TimeUnit.SECONDS);
        client = b.build();
    }

    public void sendRequest(final String host, final String json, final Callback callback) {

        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(host)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String rpcResponse = response.body().string().trim();

            callback.invoke(rpcResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
