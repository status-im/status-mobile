I18n.translations = {
    en: {
        browse_title: 'Browser',
        browse_description: 'Launch the browser'
    }
};

status.command({
    name: "browse",
    fullscreen: true,
    title: I18n.t('browse_title'),
    description: I18n.t('browse_description'),
    params: [{
        name: "url",
        placeholder: "url",
        type: status.types.TEXT
    }],
    onSend: function (params, context) {
        var url = params.args.url;
        if (!/^[a-zA-Z-_]+:/.test(url)) {
            url = 'http://' + url;
        }

        return {
            title: "Browser",
            dynamicTitle: true,
            markup: status.components.bridgedWebView(url)
        };
    }
});

status.autorun("browse");
