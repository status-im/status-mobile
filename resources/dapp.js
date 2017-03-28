I18n.translations = {
    en: {
        browse_title: 'Browser',
        browse_description: 'Launch the browser'
    }
};

status.command({
    name: "browse",
    title: I18n.t('browse_title'),
    description: I18n.t('browse_description'),
    color: "#ffa500",
    fullscreen: true,
    params: [{
        name: "url",
        optional: true,
        type: status.types.TEXT
    }],
    onSend: function (params, context) {
        var url = params.args.url || params.metadata.url;
        if (!/^[a-zA-Z-_]+:/.test(url)) {
            url = 'http://' + url;
        }

        return {
            title: params.metadata.name,
            dynamicTitle: true,
            markup: status.components.bridgedWebView(url)
        };
    }
});

status.autorun("browse");
