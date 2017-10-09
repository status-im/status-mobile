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
    private StatusModule module;

    void initJail(String initJs) {
        this.initJs = initJs;
    }

    Jail(StatusModule module) {
        this.module = module;
    }

    private void addHandlers(JSContext cell, final String chatId) {
        JSFunction web3send = new JSFunction(cell, "web3send") {
            public String web3send(String payload) {
                return Statusgo.CallRPC(payload);
            }
        };
        cell.property("web3send", web3send);

        JSFunction web3sendAsync = new JSFunction(cell, "web3sendAsync") {
            public void web3sendAsync(final String payload, final JSValue callback) {
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        String result = Statusgo.CallRPC(payload);
                        callback.toFunction().call(null, result);
                    }
                };

                thread.start();
            }
        };
        cell.property("web3sendAsync", web3sendAsync);

        JSFunction statusSendSignal = new JSFunction(cell, "statusSendSignal") {
            public void statusSendSignal(String data) {
                JSONObject event = new JSONObject();
                JSONObject signal = new JSONObject();
                try {
                    event.put("chat_id", chatId);
                    event.put("data", data);
                    signal.put("type", "jail.signal");
                    signal.put("event", event);
                } catch (JSONException e) {

                }

                module.signalEvent(signal.toString());
            }
        };
        cell.property("statusSendSignal", statusSendSignal);
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
                "     sendSignal: function (s) {statusSendSignal(s);}\n" +
                "     };\n" +
                "     var Web3 = require('web3');\n" +
                "     var provider = {\n" +
                "     send: function (payload) {\n" +
                "         var result = web3send(JSON.stringify(payload));\n" +
                "         return JSON.parse(result);\n" +
                "     },\n" +
                "     sendAsync: function (payload, callback) {\n" +
                "         var wrappedCallback = function (result) {\n" +
                "             //console.log(result);\n" +
                "             var error = null;\n" +
                "             try {\n" +
                "                 result = JSON.parse(result);\n" +
                "             } catch (e) {\n" +
                "                 error = result;\n" +
                "             }\n" +
                "             callback(error, result);\n" +
                "         };\n" +
                "         web3sendAsync(JSON.stringify(payload), wrappedCallback);\n" +
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
        addHandlers(cell, chatId);
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
                jsexception = null;
            }
        } catch (JSONException e) {
            //e.printStackTrace();
        }

        return result.toString();
    }

    String callJail(String chatId, String path, String params) {
        JSContext cell = cells.get(chatId);
        JSValue call = cell.property("call");
        JSValue callResult = call.toFunction().call(null, path, params);

        JSONObject result = new JSONObject();
        try {
            result.put("result", callResult.toString());
            if (jsexception != null) {
                result.put("error", jsexception.toString());
            }
        } catch (JSONException e) {
            //e.printStackTrace();
        }

        return result.toString();
    }

    String evaluateScript(String chatId, String js) {
        JSContext cell = cells.get(chatId);
        JSValue value = cell.evaluateScript(js);

        JSONObject result = new JSONObject();
        try {
            result.put("result", value.toString());
            if (jsexception != null) {
                result.put("error", jsexception.toString());
            }
        } catch (JSONException e) {
            //e.printStackTrace();
        }

        return result.toString();
    }
}
