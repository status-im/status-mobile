(function () {
    function bridgeSend(data){
        WebViewBridge.send(JSON.stringify(data));
    }

    var history = window.history;
    var pushState = history.pushState;
    history.pushState = function(state) {
        setTimeout(function () {
            bridgeSend({
               type: 'history-state-changed',
               navState: { url: location.href, title: document.title }
            });
        }, 100);
        return pushState.apply(history, arguments);
    };

    window.addEventListener('message', function (event) {
        if (!event.data || !event.data.type) { return; }
        if (event.data.type === 'STATUS_API_REQUEST') {
            bridgeSend({
                type: 'status-api-request',
                permissions: event.data.permissions,
                host: window.location.hostname
            });
        }
    });

    WebViewBridge.onMessage = function (message) {

        data = JSON.parse(message);

        if (data.type === "navigate-to-blank")
            window.location.href = "about:blank";

        else if (data.type === "status-api-success")
            window.STATUS_API = data.data;
            window.postMessage({ type: 'STATUS_API_SUCCESS', permissions: data.keys }, "*");
    };
}());
