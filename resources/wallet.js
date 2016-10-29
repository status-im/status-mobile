function wallet(params) {
    var url = 'http://127.0.0.1:3450';

    if (params.url && params.url !== "undefined" && params.url != "") {
        url = params.url;
        if (!/^[a-zA-Z-_]+:/.test(url)) {
            url = 'http://' + url;
        }
    }

    return {webViewUrl: url};
}

status.command({
    name: "browse",
    description: "Browse wallet",
    color: "#ffa500",
    fullscreen: true,
    suggestionsTrigger: 'on-send',
    params: [{
        name: "url",
        suggestions: wallet,
        type: status.types.TEXT
    }]
});

status.autorun("browse");
