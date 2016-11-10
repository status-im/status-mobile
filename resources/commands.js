status.command({
    name: "location",
    icon: "location",
    title: "Location",
    description: "Share your location",
    color: "#a187d5",
    preview: function (params) {
        var text = status.components.text(
            {
                style: {
                    marginTop: 5,
                    marginHorizontal: 0,
                    fontSize: 14,
                    fontFamily: "font",
                    color: "black"
                }
            }, params.address);
        var uri = "https://maps.googleapis.com/maps/api/staticmap?center="
            + params.address
            + "&size=100x100&maptype=roadmap&key=AIzaSyBNsj1qoQEYPb3IllmWMAscuXW0eeuYqAA&language=en"
            + "&markers=size:mid%7Ccolor:0xff0000%7Clabel:%7C"
            + params.address;

        var image = status.components.image(
            {
                source: {uri: uri},
                style: {
                    width: 100,
                    height: 100
                }
            }
        );

        return status.components.view({}, [text, image]);
    }
}).param({
    name: "address",
    type: status.types.TEXT,
    placeholder: "Address"
});


function browseSuggestions(params) {
    if (params.url && params.url !== "undefined" && params.url != "") {
        var url = params.url;
        if (!/^[a-zA-Z-_]+:/.test(url)) {
            url = 'http://' + url;
        }

        return {webViewUrl: url};
    }
}

status.command({
    name: "browse",
    title: "Browser",
    description: "Launch the browser",
    color: "#ffa500",
    fullscreen: true,
    suggestionsTrigger: 'on-send',
    params: [{
        name: "url",
        suggestions: browseSuggestions,
        type: status.types.TEXT
    }]
});

function validateBalance(params, context) {
    if (!params.amount) {
        return {
            errors: [
                status.components.validationMessage(
                    "Amount",
                    "Amount must be specified"
                )
            ]
        };
    }

    try {
        var val = web3.toWei(params.amount, "ether");
    } catch (err) {
        return {
            errors: [
                status.components.validationMessage(
                    "Amount",
                    "Amount is not valid number"
                )
            ]
        };
    }

    var balance = web3.eth.getBalance(context.from);
    if (bn(val).greaterThan(bn(balance))) {
        return {
            errors: [
                status.components.validationMessage(
                    "Amount",
                    "Not enough ETH on balance ("
                    + web3.fromWei(balance, "ether")
                    + " ETH)"
                )
            ]
        };
    }
}

function sendTransaction(params, context) {
    var data = {
        from: context.from,
        to: context.to,
        value: web3.toWei(params.amount, "ether")
    };

    try {
        return web3.eth.sendTransaction(data);
    } catch (err) {
        return {error: err};
    }
}

var send = {
    name: "send",
    icon: "money_white",
    color: "#5fc48d",
    title: "Send ETH",
    description: "Send a payment",
    params: [{
        name: "amount",
        type: status.types.NUMBER
    }],
    preview: function (params) {
        return status.components.text(
            {},
            params.amount + " ETH"
        );
    },
    handler: sendTransaction,
    validator: validateBalance
};

status.command(send);
status.response(send);

status.command({
    name: "request",
    title: "Request ETH",
    color: "#7099e6",
    description: "Request a payment",
    params: [{
        name: "amount",
        type: status.types.NUMBER
    }],
    preview: function (params) {
        return status.components.text(
            {},
            params.amount + " ETH"
        );
    },
    handler: function (params) {
        return {
            event: "request",
            params: [params.amount]
            request: {
                command: "send",
                params: {
                    amount: params.amount
                },
                content: "Requesting " + params.amount + "ETH"
            }
        };
    },
});
