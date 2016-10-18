function wallet() {
    var url = 'http://127.0.0.1:3450';

    return {webViewUrl: url};
}

status.command({
    name: "browse",
    description: "Browse wallet",
    color: "#ffa500",
    fullscreen: true,
    suggestionsTrigger: 'on-send',
    params: [{
        name: "webpage",
        suggestions: wallet,
        type: status.types.TEXT
    }]
});

status.autorun("browse");
