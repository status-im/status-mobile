function browseSuggestions(params, context) {
    var url;

    if (context["dapp-url"]) {
        url = context["dapp-url"];
    }

    if (params.url && params.url !== "undefined" && params.url != "") {
        url = params.url;
        if (!/^[a-zA-Z-_]+:/.test(url)) {
            url = 'http://' + url;
        }
    }

    if (url) {
        status.browse(url);
    }
}

status.command({
    name: "global",
    title: I18n.t('browse_title'),
    description: I18n.t('browse_description'),
    color: "#ffa500",
    fullscreen: true,
    suggestionsTrigger: 'on-send',
    params: [{
        name: "url",
        suggestions: browseSuggestions,
        type: status.types.TEXT
    }]
});
