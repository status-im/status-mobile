status.command({
    name: "browse",
    title: I18n.t('browse_title'),
    description: I18n.t('browse_description'),
    color: "#ffa500",
    fullscreen: true,
    onSend: function (params, context) {
        var url = 'https://status.im/dapps/wallet';
        if (context.debug) {
            url = 'http://127.0.0.1:3450';
        }

        return {
            title: "Wallet",
            dynamicTitle: false,
            markup: status.components.bridgedWebView(url)
        };
    }
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
