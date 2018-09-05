(function () {
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
}());