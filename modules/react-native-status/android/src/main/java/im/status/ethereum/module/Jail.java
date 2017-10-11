package im.status.ethereum.module;

import android.util.Log;

import com.github.status_im.status_go.cmd.Statusgo;

import org.json.JSONException;
import org.json.JSONObject;
import org.liquidplayer.webkit.javascriptcore.JSContext;
import org.liquidplayer.webkit.javascriptcore.JSException;
import org.liquidplayer.webkit.javascriptcore.JSFunction;
import org.liquidplayer.webkit.javascriptcore.JSValue;

import java.util.HashMap;
import java.util.Map;

class Jail {
    private String initJs;
    private Map<String, JSContext> cells = new HashMap<>();

    void initJail(String initJs) {
        this.initJs = initJs;
    }

    private void addHandlers(JSContext cell) {
        new JSFunction(cell, "web3send") {
            public String web3send(String payload) {
                return Statusgo.CallRPC(payload);
            }
        };

        new JSFunction(cell, "web3sendAsync") {
            public void web3sendAsync(final String payload, final JSValue callback) {
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        callback.toFunction().call(null, Statusgo.CallRPC(payload));
                    }
                };

                thread.start();
            }
        };
    }

    private JSException jsexception;

    private void checkException(String label) {
        if (jsexception != null) {
            jsexception = null;
        }
    }

    String parseJail(String chatId, String js) {
        JSContext cell = new JSContext();
        cell.setExceptionHandler(new JSContext.IJSExceptionHandler() {
            @Override
            public void handle(JSException exception) {
                jsexception = exception;
            }
        });
        String web3Js = "var statusSignals = {\n" +
                "     sendSignal: function (s) {}\n" +
                "     };\n" +
                "     var Web3 = require('web3');\n" +
                "     var provider = {\n" +
                "     send: function (payload) {\n" +
                "         var result = web3send(JSON.stringify(payload));\n" +
                "         return JSON.parse(result);\n" +
                "     },\n" +
                "     sendAsync: function (payload, callback) {\n" +
                "         var wrappedCallback = function (result) {\n" +
                "             console.log(result);\n" +
                "             var error = null;\n" +
                "             try {\n" +
                "                 result = JSON.parse(result);\n" +
                "             } catch (e) {\n" +
                "                 error = result;\n" +
                "             }\n" +
                "             callback(error, result);\n" +
                "         };\n" +
                "         web3send(JSON.stringify(payload), wrappedCallback);\n" +
                "     }\n" +
                "     };\n" +
                "     var web3 = new Web3(provider);\n" +
                "     var console = {\n" +
                "     log: function (data) {\n" +
                "         //statusNativeHandlers.log(data);\n" +
                "     }\n" +
                "     };\n" +
                "     var Bignumber = require(\"bignumber.js\");\n" +
                "     function bn(val){\n" +
                "         return new Bignumber(val);\n" +
                "     }\n";
        addHandlers(cell);
        cell.evaluateScript(initJs);
        cell.evaluateScript(web3Js);
        cell.evaluateScript(js);

        cell.evaluateScript("var catalog = JSON.stringify(_status_catalog);");
        JSValue catalog = cell.property("catalog");
        cells.put(chatId, cell);

        JSONObject result = new JSONObject();
        try {
            result.put("result", catalog.toString());
            if (jsexception != null) {
                result.put("error", jsexception.toString());
            }
        } catch (JSONException e) {
            //e.printStackTrace();
        }

        return result.toString();
    }

    String callJail(String chatId, String path, String params) {
        JSContext cell = cells.get(chatId);
        JSValue call = cell.property("call");
        JSValue result = call.toFunction().call(null, path, params);

        return result.toString();
    }
}
