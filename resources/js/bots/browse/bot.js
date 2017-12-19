function browse(params, context) {
    var url;

    if (params.metadata && params.metadata.url
        && params.metadata.url !== "undefined" && params.metadata.url != "") {
        url = params.metadata.url;
    }

    if (params.url && params.url !== "undefined" && params.url != "") {
        url = params.url;
    }

    if (url && !/^[a-zA-Z-_]+:/.test(url)) {
        url = 'http://' + url;
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
    name: "browse",
    title: I18n.t('browse_title'),
    scope: ["global", "personal-chats", "group-chats", "public-chats", "registered", "dapps", "humans"],
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
