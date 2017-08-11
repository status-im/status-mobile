(function () {
    window.statusAPI = {
        dispatch: function (event, options) {
            console.log("statusAPI.dispatch: " + JSON.stringify(options));
            if (options.callback) {
                console.log(options.callback);
                statusAPI.callbacks[event] = options.callback;
            }
            var json = JSON.stringify({
                event: event,
                options: options
            });
            console.log("sending from webview: " + json);
            WebViewBridge.send(json);
        },
        callbacks: {}
    };

    WebViewBridge.onMessage = function (messageString) {
        console.log("received from react-native: " + messageString);
        var message = JSON.parse(messageString);

        if (message.event === "actions-execute-js") {
            eval(message.js);
        } else if (statusAPI.callbacks[message.event]) {
            statusAPI.callbacks[message.event](message.params);
        }
    };
}());
