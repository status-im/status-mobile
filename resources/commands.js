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

var phones = [
    {
        number: "89171111111",
        description: "Number format 1"
    },
    {
        number: "89371111111",
        description: "Number format 1"
    },
    {
        number: "+79171111111",
        description: "Number format 2"
    },
    {
        number: "9171111111",
        description: "Number format 3"
    }
];

function suggestionsContainerStyle(suggestionsCount) {
    return {
        marginVertical: 1,
        marginHorizontal: 0,
        height: Math.min(150, (56 * suggestionsCount)),
        backgroundColor: "white",
        borderRadius: 5
    };
}

var suggestionContainerStyle = {
    paddingLeft: 16,
    backgroundColor: "white"
};

var suggestionSubContainerStyle = {
    height: 56,
    borderBottomWidth: 1,
    borderBottomColor: "#0000001f"
};

var valueStyle = {
    marginTop: 9,
    fontSize: 14,
    fontFamily: "font",
    color: "#000000de"
};

var descriptionStyle = {
    marginTop: 1.5,
    fontSize: 14,
    fontFamily: "font",
    color: "#838c93de"
};

function startsWith(str1, str2) {
    // String.startsWith(...) doesn't work in otto
    return str1.lastIndexOf(str2, 0) == 0 && str1 != str2;
}

function phoneSuggestions(params) {
    var ph, suggestions;
    if (!params.value || params.value == "") {
        ph = phones;
    } else {
        ph = phones.filter(function (phone) {
            return startsWith(phone.number, params.value);
        });
    }

    if (ph.length == 0) {
        return;
    }

    suggestions = ph.map(function (phone) {
        return status.components.touchable(
            {onPress: [status.events.SET_VALUE, phone.number]},
            status.components.view(suggestionContainerStyle,
                [status.components.view(suggestionSubContainerStyle,
                    [
                        status.components.text(
                            {style: valueStyle},
                            phone.number
                        ),
                        status.components.text(
                            {style: descriptionStyle},
                            phone.description
                        )
                    ])])
        );
    });

    var view = status.components.scrollView(
        suggestionsContainerStyle(ph.length),
        suggestions
    );

    return {markup: view};
}

var phoneConfig = {
    name: "phone",
    description: "Send phone number",
    color: "#5fc48d",
    validator: function (params) {
        return {
            validationHandler: "phone",
            parameters: [params.value]
        };
    },
    params: [{
        name: "phone",
        type: status.types.PHONE,
        suggestions: phoneSuggestions,
        placeholder: "Phone number"
    }],
    handler: function (params) {
        return {
            event: "sign-up",
            params: [params.value]
        };
    }
};
status.response(phoneConfig);
status.command(phoneConfig);

status.command({
    name: "help",
    description: "Help",
    color: "#7099e6",
    /* Validator example
     validator: function (params) {
     if (params.value != "3") {
     var error = status.components.view(
     {backgroundColor: "red"},
     [status.components.text({}, "ooops :(")]
     );
     return {errors: [error]}
     }
     },*/
    params: [{
        name: "query",
        type: status.types.TEXT
    }]
});

status.response({
    name: "confirmation-code",
    color: "#7099e6",
    description: "Confirmation code",
    params: [{
        name: "code",
        type: status.types.NUMBER
    }],
    handler: function (params) {
        return {
            event: "confirm-sign-up",
            params: [params.value]
        };
    },
    validator: function(params){
        if(!/^[\d]{4}$/.test(params.value)){
            var error = status.components.validationMessage(
                "Confirmation code",
                "Wrong format"
            );

            return {errors: [error]}
        }
    }
});

status.response({
    name: "keypair",
    color: "#7099e6",
    description: "Keypair password",
    icon: "icon_lock_white",
    params: [{
        name: "password",
        type: status.types.PASSWORD
    }],
    handler: function (params) {
        return {
            event: "save-password",
            params: [params.value]
        };
    },
    preview: function (params) {
        return status.components.text(
            {
                style: {
                    marginTop: 5,
                    marginHorizontal: 0,
                    fontSize: 14,
                    fontFamily: "font",
                    color: "black"
                }
            }, "*****");
    }
});

function walletView(params) {
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
        suggestions: walletView,
        type: status.types.TEXT
    }]
});

function validateBalance(params) {
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
