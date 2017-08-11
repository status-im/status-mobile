function round(n) {
    return Math.round(n * 100) / 100;
}

function doubledValueLabel(params) {
    var value = round(params.value);
    return "sliderValue = " + value +
        "; (2 * sliderValue) = " + (2 * value);
}

status.defineSubscription(
    // the name of subscription and the name of the value in bot-db
    // associated with this subscription
    "doubledValue",
    // the map of values on which subscription depends: keys are arbitrary names
    // and values are db paths to another value
    {value: ["sliderValue"]},
    // the function which will be called as reaction on changes of values above,
    // should be pure. Returned result will be associated with subscription in bot-db
    doubledValueLabel
);

status.defineSubscription(
    "roundedValue",
    {value: ["sliderValue"]},
    function (params) {
        return round(params.value);
    }
);

function demoSuggestions(params, context) {
    var balance = parseFloat(web3.fromWei(web3.eth.getBalance(context.from), "ether"));
    var defaultSliderValue = balance / 2;

    var view = ["view", {},
        ["text", {}, "Balance " + balance + " ETH"],
        ["text", {}, ["subscribe", ["doubledValue"]]],
        ["slider", {
            maximumValue: ["subscribe", ["balance"]],
            value: defaultSliderValue,
            minimumValue: 0,
            onSlidingComplete: ["dispatch", ["set", "sliderValue"]],
            step: 0.05
        }],
        ['touchable',
            {onPress: ['dispatch', ["set-value-from-db", "roundedValue"]]},
            ["view", {}, ["text", {}, "Set value"]]
        ],
        ["text", {style: {color: "red"}}, ["subscribe", ["validationText"]]]
    ];

    status.setDefaultDb({
        sliderValue: defaultSliderValue,
        doubledValue: doubledValueLabel({value: defaultSliderValue})
    });

    var validationText = "";

    if (typeof params !== 'undefined') {
        if (isNaN(params.message)) {
            validationText = "That's not a float number!";
        } else if (parseFloat(params.message) > balance) {
            validationText =
                "Input value is too big!" +
                " You have only " + balance + " ETH on your balance!";
        }
    }
    status.updateDb({
        balance: balance,
        validationText: validationText
    });

    return {markup: view};
}

status.addListener("on-message-input-change", demoSuggestions);
status.addListener("on-message-send", function (params, context) {
    var cnt = localStorage.getItem("cnt");
    if (!cnt) {
        cnt = 0;
    }

    cnt++;

    localStorage.setItem("cnt", cnt);
    if (isNaN(params.message)) {
        return {"text-message": "Seems that you don't want to send money :(. cnt = " + cnt};
    }

    var balance = web3.eth.getBalance(context.from);
    var value = parseFloat(params.message);
    var weiValue = web3.toWei(value, "ether");
    if (bn(weiValue).greaterThan(bn(balance))) {
        return {"text-message": "No way man, you don't have enough money! :)"};
    }
    web3.eth.sendTransaction({
        from: context.from,
        to: context.from,
        value: weiValue
    }, function (error, hash) {
        if (error) {
            status.sendMessage("Something went wrong, try again :(");
            status.showSuggestions(demoSuggestions(params, context).markup);
        } else {
            status.sendMessage("You are the hero, you sent " + value + " ETH to yourself!")
        }
    });
});

status.command({
    name: "init-request",
    description: "send-request",
    color: "#a187d5",
    sequentialParams: true,
    params: [{
        name: "address",
        type: status.types.TEXT,
        placeholder: "address"
    }],
    handler: function (params) {
        return {
            "text-message": {
                type: "request",
                content: {
                    command: "response",
                    params: {first: "123"},
                    text: "That's request's content! It works!"
                }
            }
        };
    }
});

status.response({
    name: "response",
    color: "#a187d5",
    sequentialParams: true,
    params: [{
        name: "first",
        type: status.types.TEXT,
        placeholder: "first"
    }],
    handler: function (params) {
        return {"text-message": "ok"};
    }
});
