status.command({
    name: "location",
    description: "Send location",
    color: "#9a5dcf",
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
            }, params.value);
        var uri = "https://maps.googleapis.com/maps/api/staticmap?center="
            + params.value
            + "&size=100x100&maptype=roadmap&key=AIzaSyBNsj1qoQEYPb3IllmWMAscuXW0eeuYqAA&language=en"
            + "&markers=size:mid%7Ccolor:0xff0000%7Clabel:%7C"
            + params.value;

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
    if (params.value != "") {
        var url = params.value;
        if (!/^[a-zA-Z-_]+:/.test(url)) {
            url = 'http://' + url;
        }

        return {webViewUrl: url};
    }
}

status.command({
    name: "browse",
    description: "browser",
    color: "#ffa500",
    fullscreen: true,
    suggestionsTrigger: 'on-send',
    params: [{
        name: "webpage",
        suggestions: browseSuggestions,
        type: status.types.TEXT
    }]
});

function validateBalance(params) {
    if(!params.value){
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
        var val = web3.toWei(params.value, "ether");
    } catch (err) {
        return {
            errors: [
                status.components.validationMessage(
                    "Amount",
                    "Amount is not valid number"//err.message
                )
            ]
        };
    }

    var balance = web3.eth.getBalance(params.command.address);
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

function sendTransaction(params) {
    var data = {
        from: params.command.from,
        to: params.command.to,
        value: web3.toWei(params.value, "ether")
    };
    var hash = web3.eth.sendTransaction(data);

    return {"transaction-hash": hash};
}

status.command({
    name: "send",
    color: "#5fc48d",
    description: "Send transaction",
    params: [{
        name: "amount",
        type: status.types.NUMBER
    }],
    preview: function (params) {
        return status.components.text(
            {},
            params.value + " ETH"
        );
    },
    handler: sendTransaction,
    validator: validateBalance
});
