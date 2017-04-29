function browse(params, context) {
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
        singleLineInput: true,
        actions: [
            {
                type: status.actions.WEB_VIEW_BACK
            },
            {
                type: status.actions.WEB_VIEW_FORWARD
            },
            {
                type: status.actions.FULLSCREEN
            },
        ],
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
    params: [{
        name: "url",
        type: status.types.TEXT,
        placeholder: "URL"
    }],
    onSend: browse
});
