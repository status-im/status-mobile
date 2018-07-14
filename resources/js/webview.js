(function () {
    var history = window.history;
    var pushState = history.pushState;
    history.pushState = function(state) {
        setTimeout(function () {
            WebViewBridge.send(JSON.stringify({
                type: 'navStateChange',
                navState: { url: location.href, title: document.title }
            }))
        }, 100);
        return pushState.apply(history, arguments);
    };

    WebViewBridge.onMessage = function (messageString) {
        console.log("received from react-native: " + messageString);

        if (messageString === "navigate-to-blank")
            window.location.href = "about:blank";
    };
}());
