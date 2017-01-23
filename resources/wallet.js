function wallet(params, context) {
    var url = 'https://status.im/dapps/wallet';

    if (context.debug) {
        url = 'http://127.0.0.1:3450';
    }

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
    title: I18n.t('browse_title'),
    description: I18n.t('browse_description'),
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

status.registerFunction("send", function (params, context) {
    var data = {
        from: context.from,
        to: params.address,
        value: web3.toWei(params.amount, "ether")
    };

    web3.eth.sendTransaction(data);
})
