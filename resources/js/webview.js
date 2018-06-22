(function () {

    WebViewBridge.onMessage = function (messageString) {
        console.log("received from react-native: " + messageString);

        if (messageString === "navigate-to-blank")
            window.location.href = "about:blank";
    };
}());
