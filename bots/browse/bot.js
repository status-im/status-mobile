function browseSuggestions(params, context) {
    var url;

    if (params.url && params.url !== "undefined" && params.url != "") {
        url = params.url;
        if (!/^[a-zA-Z-_]+:/.test(url)) {
            url = 'http://' + url;
        }
    }

    return {
        title: "Browser",
        dynamicTitle: true,
        markup: status.components.bridgedWebView(url)
    };
}

status.command({
    name: "global",
    title: I18n.t('browse_title'),
    registeredOnly: true,
    description: I18n.t('browse_description'),
    color: "#ffa500",
    fullscreen: true,
    suggestionsTrigger: 'on-send',
    params: [{
        name: "url",
        type: status.types.TEXT,
        placeholder: "URL"
    }],
    onSend: browseSuggestions
});
