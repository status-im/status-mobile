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
    suggestionsTrigger: 'on-send',
    params: [{
        name: "url",
        suggestions: function(params, context) {
            var url = "dapp-url";

            if (params.url && params.url !== "undefined" && params.url != "") {
                url = params.url;
                if (!/^[a-zA-Z-_]+:/.test(url)) {
                    url = 'http://' + url;
                }
            }
            status.browse(url);
        },
        type: status.types.TEXT
    }]
});

status.autorun("browse");
