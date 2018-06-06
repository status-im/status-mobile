package im.status.ethereum.module;

import android.util.Log;

import com.github.status_im.status_go.Statusgo;

import org.json.JSONException;
import org.json.JSONObject;
import org.liquidplayer.webkit.javascriptcore.JSContext;
import org.liquidplayer.webkit.javascriptcore.JSException;
import org.liquidplayer.webkit.javascriptcore.JSFunction;
import org.liquidplayer.webkit.javascriptcore.JSValue;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class JSCJail implements Jail {
    private static final String TAG = "JSCJail";
    private String initJs;
    private Map<String, Cell> cells = new HashMap<>();
    private StatusModule module;

    @Override
    public void initJail(String initJs) {
        this.initJs = initJs;
    }

    JSCJail(StatusModule module) {
        this.module = module;
    }

    private class Cell {
        JSContext context;
        Timer timer;
    }

    private class Timer {
        private Map<String, ScheduledExecutorService> timers = new HashMap<>();

        String setTimeout(JSValue callback, int interval) {
            return this.scheduleTask(callback, interval, false);
        }

        String setInterval(JSValue callback, int interval) {
            return this.scheduleTask(callback, interval, true);
        }

        private String scheduleTask(final JSValue callback, int interval, final boolean repeatable) {
            final String id = UUID.randomUUID().toString();
            final ScheduledExecutorService scheduler =
                    Executors.newSingleThreadScheduledExecutor();

            timers.put(id, scheduler);

            scheduler.scheduleAtFixedRate
                    (new Runnable() {
                        public void run() {
                            if (!repeatable) {
                                scheduler.shutdown();
                                timers.remove(id);
                            }
                            callback.toFunction().call();
                        }
                    }, interval, interval, TimeUnit.MILLISECONDS);

            return id;
        }

        void clearInterval(String id) {
            if (!timers.containsKey(id)) {
                return;
            }
            ScheduledExecutorService scheduler = timers.get(id);
            scheduler.shutdown();
            timers.remove(id);
        }

        void reset() {
            for (String entry : timers.keySet()) {
                timers.get(entry).shutdown();
            }
            timers.clear();
        }

    }


    private void addHandlers(Cell cell, final String chatId) {
        JSContext context = cell.context;
        final Timer timer = cell.timer;

        JSFunction web3send = new JSFunction(context, "web3send") {
            public String web3send(String payload) {
                return Statusgo.CallRPC(payload);
            }
        };
        context.property("web3send", web3send);

        JSFunction web3sendAsync = new JSFunction(context, "web3sendAsync") {
            public void web3sendAsync(final String payload, final JSValue callback) {
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        String result = Statusgo.CallRPC(payload);
                        callback.toFunction().call(null, result);
                    }
                };

                StatusThreadPoolExecutor.getInstance().execute(r);
            }
        };
        context.property("web3sendAsync", web3sendAsync);

        JSFunction statusSendSignal = new JSFunction(context, "statusSendSignal") {
            public void statusSendSignal(String data) {
                JSONObject event = new JSONObject();
                JSONObject signal = new JSONObject();
                try {
                    event.put("chat_id", chatId);
                    event.put("data", data);
                    signal.put("type", "jail.signal");
                    signal.put("event", event);
                } catch (JSONException e) {
                    Log.d(TAG, "Failed to construct signal JSON object: " + e.getMessage());
                }

                module.signalEvent(signal.toString());
            }
        };
        context.property("statusSendSignal", statusSendSignal);

        JSFunction setTimeout = new JSFunction(context, "setTimeout") {
            public String setTimeout(final JSValue callback, final int ms) {
                return timer.setTimeout(callback, ms);
            }
        };
        context.property("setTimeout", setTimeout);

        JSFunction setInterval = new JSFunction(context, "setInterval") {
            public String setInterval(final JSValue callback, final int ms) {
                return timer.setInterval(callback, ms);
            }
        };
        context.property("setInterval", setInterval);

        JSFunction clearInterval = new JSFunction(context, "clearInterval") {
            public void clearInterval(String id) {
                timer.clearInterval(id);
            }
        };
        context.property("clearInterval", clearInterval);

        JSFunction statusLog = new JSFunction(context, "statusLog") {
            public void statusLog(String data) {
                Log.d("statusJSLog", data);
            }
        };
        context.property("statusLog", statusLog);
    }

    private JSException jsException;

    private void checkException(String label) {
        if (jsException != null) {
            jsException = null;
        }
    }

    @Override
    public String parseJail(String chatId, String js) {
        Cell cell = new Cell();
        JSContext context = new JSContext();
        cell.context = context;
        cell.timer = new Timer();

        context.setExceptionHandler(new JSContext.IJSExceptionHandler() {
            @Override
            public void handle(JSException exception) {
                jsException = exception;
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
                "         statusLog(data);\n" +
                "     }\n" +
                "     };\n" +
                "     var Bignumber = require(\"bignumber.js\");\n" +
                "     function bn(val){\n" +
                "         return new Bignumber(val);\n" +
                "     }\n";
        addHandlers(cell, chatId);
        context.evaluateScript(initJs);
        context.evaluateScript(web3Js);
        context.evaluateScript(js);

        context.evaluateScript("var catalog = JSON.stringify(_status_catalog);");
        JSValue catalog = context.property("catalog");
        cells.put(chatId, cell);

        JSONObject result = new JSONObject();
        try {
            result.put("result", catalog.toString());
            if (jsException != null) {
                result.put("error", jsException.toString());
                jsException = null;
            }
        } catch (JSONException e) {
            Log.d(TAG, "Failed to construct JSON response for parseJail: " + e.getMessage());
        }

        return result.toString();
    }

    @Override
    public String callJail(String chatId, String path, String params) {
        if (!cells.containsKey(chatId)) {
            return null;
        }
        JSContext context = cells.get(chatId).context;
        JSValue call = context.property("call");
        JSValue callResult = call.toFunction().call(null, path, params);

        JSONObject result = new JSONObject();
        try {
            result.put("result", callResult.toString());
            if (jsException != null) {
                result.put("error", jsException.toString());
            }
        } catch (JSONException e) {
            Log.d(TAG, "Failed to construct JSON response for callJail: " + e.getMessage());
        }

        return result.toString();
    }

    @Override
    public void reset() {
        for (String entry : cells.keySet()) {
            cells.get(entry).timer.reset();
        }
        cells.clear();
    }
}
