// Send command/response

function calculateFee(n, tx) {
    var estimatedGas = 21000;
    if (tx !== null) {
        try {
            estimatedGas = web3.eth.estimateGas(tx);
        } catch (err) {

        }
    }

    var gasMultiplicator = Math.pow(1.4, n).toFixed(3);
    var gasPrice = 211000000000;
    try {
        gasPrice = web3.eth.gasPrice;
    } catch (err) {

    }
    var weiFee = gasPrice * gasMultiplicator * estimatedGas;
    // force fee in eth to be of BigNumber type
    var ethFee = web3.toBigNumber(web3.fromWei(weiFee, "ether"));
    // always display 7 decimal places
    return ethFee.toFixed(7);
}

function calculateGasPrice(n) {
    var gasMultiplicator = Math.pow(1.4, n).toFixed(3);
    var gasPrice = 211000000000;
    try {
        gasPrice = web3.eth.gasPrice;
    } catch (err) {

    }

    return gasPrice * gasMultiplicator;
}

status.defineSubscription(
    "calculatedFee",
    {value: ["sliderValue"], tx: ["transaction"]},
    function (params) {
        return calculateFee(params.value, params.tx);
    }
);

function getFeeExplanation(n) {
    return I18n.t('send_explanation') + I18n.t('send_explanation_' + (n + 2));
}

status.defineSubscription(
    "feeExplanation",
    {value: ["sliderValue"]},
    function(params) {
        return getFeeExplanation(params.value);
    }
);

function amountParameterBox(groupChat, params, context) {
    if (!params["bot-db"]) {
        params["bot-db"] = {};
    }

    var contactAddress;
    if (groupChat) {
        if (params["bot-db"]["public"] && params["bot-db"]["public"]["recipient"]) {
            contactAddress = params["bot-db"]["public"]["recipient"]["address"];
        } else {
            contactAddress = null;
        }
    } else {
        contactAddress = context.to;
    }
    
    var txData;
    var amount;
    var amountIndex = groupChat ? 1 : 0;
    
    try {
        amount = params.args[amountIndex];
        txData = {
            to: contactAddress,
            value: web3.toWei(amount) || 0
        };
    } catch (err) {
        amount = null;
        txData = {
            to: contactAddress,
            value: 0
        };
    } 

    var sliderValue = params["bot-db"]["sliderValue"] || 0;

    try {

        status.setDefaultDb({
            transaction: txData,
            calculatedFee: calculateFee(sliderValue, txData),
            feeExplanation: getFeeExplanation(sliderValue),
            sliderValue: sliderValue
        });

    } catch (err) {

        status.setDefaultDb({
            transaction: txData,
            calculatedFee: "0",
            feeExplanation: "",
            sliderValue: sliderValue
        });
    }
    
    return {
        title: I18n.t('send_title'),
        showBack: true,
        markup: status.components.scrollView(
            {
                keyboardShouldPersistTaps: "always"
            },
            [status.components.view(
                {
                    flex: 1
                },
                [
                    status.components.text(
                        {
                            style: {
                                fontSize: 14,
                                color: "rgb(147, 155, 161)",
                                paddingTop: 12,
                                paddingLeft: 16,
                                paddingRight: 16,
                                paddingBottom: 20
                            }
                        },
                        I18n.t('send_specify_amount')
                    ),
                    status.components.touchable(
                        {
                            onPress: status.components.dispatch([status.events.FOCUS_INPUT, []])
                        },
                        status.components.view(
                            {
                                flexDirection: "row",
                                alignItems: "center",
                                textAlign: "center",
                                justifyContent: "center"
                            },
                            [
                                status.components.text(
                                    {
                                        font: "light",
                                        numberOfLines: 1,
                                        ellipsizeMode: "tail",
                                        style: {
                                            maxWidth: 250,
                                            fontSize: 38,
                                            marginLeft: 8,
                                            color: "black"
                                        }
                                    },
                                    amount || "0.00"
                                ),
                                status.components.text(
                                    {
                                        font: "light",
                                        style: {
                                            fontSize: 38,
                                            marginLeft: 8,
                                            color: "rgb(147, 155, 161)"
                                        }
                                    },
                                    I18n.t('eth')
                                ),
                            ]
                        )
                    ),
                    status.components.text(
                        {
                            style: {
                                fontSize: 14,
                                color: "rgb(147, 155, 161)",
                                paddingTop: 14,
                                paddingLeft: 16,
                                paddingRight: 16,
                                paddingBottom: 5
                            }
                        },
                        I18n.t('send_fee')
                    ),
                    status.components.view(
                        {
                            flexDirection: "row"
                        },
                        [
                            status.components.text(
                                {
                                    style: {
                                        fontSize: 17,
                                        color: "black",
                                        paddingLeft: 16
                                    }
                                },
                                [status.components.subscribe(["calculatedFee"])]
                            ),
                            status.components.text(
                                {
                                    style: {
                                        fontSize: 17,
                                        color: "rgb(147, 155, 161)",
                                        paddingLeft: 4,
                                        paddingRight: 4
                                    }
                                },
                                I18n.t('eth')
                            )
                        ]
                    ),
                    status.components.slider(
                        {
                            maximumValue: 2,
                            minimumValue: -2,
                            onSlidingComplete: status.components.dispatch(
                                [status.events.UPDATE_DB, "sliderValue"]
                            ),
                            step: 1,
                            style: {
                                marginLeft: 16,
                                marginRight: 16
                            }
                        }
                    ),
                    status.components.view(
                        {
                            flexDirection: "row"
                        },
                        [
                            status.components.text(
                                {
                                    style: {
                                        flex: 1,
                                        fontSize: 14,
                                        color: "rgb(147, 155, 161)",
                                        paddingLeft: 16,
                                        alignSelf: "flex-start"
                                    }
                                },
                                I18n.t('send_cheaper')
                            ),
                            status.components.text(
                                {
                                    style: {
                                        flex: 1,
                                        fontSize: 14,
                                        color: "rgb(147, 155, 161)",
                                        paddingRight: 16,
                                        alignSelf: "flex-end",
                                        textAlign: "right"
                                    }
                                },
                                I18n.t('send_faster')
                            )
                        ]
                    ),
                    status.components.text(
                        {
                            style: {
                                fontSize: 14,
                                color: "black",
                                paddingTop: 16,
                                paddingLeft: 16,
                                paddingRight: 16,
                                paddingBottom: 16,
                                lineHeight: 24
                            }
                        },
                        [status.components.subscribe(["feeExplanation"])]
                    )
                ]
            )]
        )
    };
}

var recipientSendParam = {
    name: "recipient",
    type: status.types.TEXT,
    suggestions: function (params) {
        return {
            title: I18n.t('send_title'),
            markup: status.components.chooseContact(I18n.t('send_choose_recipient'), "recipient", 0)
        };
    }
};

function amountSendParam(groupChat) {
    return {
        name: "amount",
        type: status.types.NUMBER,
        suggestions: amountParameterBox.bind(this, groupChat)
    };
}

var paramsPersonalSend = [amountSendParam(false)];
var paramsGroupSend = [recipientSendParam, amountSendParam(true)];

function validateSend(validateRecipient, params, context) {
    if (!params["bot-db"]) {
        params["bot-db"] = {};
    }

    if (validateRecipient) {
        if (!params["bot-db"]["public"]
            || !params["bot-db"]["public"]["recipient"]
            || !params["bot-db"]["public"]["recipient"]["address"]) {
            return {
                markup: status.components.validationMessage(
                    "Wrong address",
                    "Recipient address must be specified"
                )
            };
        }
    }
    
    if (!params["amount"]) {
        return {
            markup: status.components.validationMessage(
                I18n.t('validation_title'),
                I18n.t('validation_amount_specified')
            )
        };
    }

    var amount = params["amount"].replace(",", ".");
    var amountSplitted = amount.split(".");
    if (amountSplitted.length === 2 && amountSplitted[1].length > 18) {
        return {
            markup: status.components.validationMessage(
                I18n.t('validation_title'),
                I18n.t('validation_amount_is_too_small')
            )
        };
    }

    if (isNaN(parseFloat(params.amount.replace(",", ".")))) {
        return {
            markup: status.components.validationMessage(
                I18n.t('validation_title'),
                I18n.t('validation_invalid_number')
            )
        };
    }

    try {
        var val = web3.toWei(amount, "ether");
        if (val < 0) {
            throw new Error();
        }
    } catch (err) {
        return {
            markup: status.components.validationMessage(
                I18n.t('validation_title'),
                I18n.t('validation_invalid_number')
            )
        };
    }

    try {
        var balance = web3.eth.getBalance(context.from);
        if (isNaN(balance)) {
            throw new Error();
        }
    } catch (err) {
        return {
            markup: status.components.validationMessage(
                I18n.t('validation_internal_title'),
                I18n.t('validation_balance')
            )
        };
    }

    
    var fee = calculateFee(
        params["bot-db"]["sliderValue"],
        {
            to: context.to,
            value: val
        }
    );

    if (bn(val).plus(bn(web3.toWei(fee, "ether"))).greaterThan(bn(balance))) {
        return {
            markup: status.components.validationMessage(
                I18n.t('validation_title'),
                I18n.t('validation_insufficient_amount')
                + web3.fromWei(balance, "ether")
                + " ETH)"
            )
        };
    }
}

function handleSend(groupChat, params, context) {
    var val = web3.toWei(params["amount"].replace(",", "."), "ether");

    var gasPrice = calculateGasPrice(params["bot-db"]["sliderValue"]);
    var data = {
        from: context.from, 
        value: val
    };

    if (groupChat) {
        data.to = params["bot-db"]["public"]["recipient"]["address"];
    } else {
        data.to = context.to;
    }

    if (gasPrice) {
        data.gasPrice = gasPrice;
    }

    web3.eth.sendTransaction(data, function(error, hash) {
        if (error) {
            // Do nothing, as error handling will be done as response to transaction.failed event from go 
        } else {
            status.sendSignal("handler-result", {
                status: "success",
                hash: hash,
                origParams: context["orig-params"]
            });
        } 
    });
    // async handler, so we don't return anything immediately
}

function previewSend(showRecipient, params, context) {
    var amountStyle = {
        fontSize: 36,
        color: "#000000",
        height: 40
    };

    var amount = status.components.view(
        {
            flexDirection: "column",
            alignItems: "flex-end",
            maxWidth: 250
        },
        [status.components.text(
            {
                style: amountStyle,
                numberOfLines: 1,
                ellipsizeMode: "tail",
                font: "light"
            },
            status.localizeNumber(params.amount, context.delimiter, context.separator)
        )]);

    var currency = status.components.view(
        {
            style: {
                flexDirection: "column",
                justifyContent: "flex-end",
                paddingBottom: 0
            }
        },
        [status.components.text(
            {
                style: {
                    color: "#9199a0",
                    fontSize: 16,
                    lineHeight: 18,
                    marginLeft: 7.5
                }
            },
            I18n.t('eth')
        )]
    );

    var amountRow = status.components.view(
        {
            style: {
                flexDirection: "row",
                justifyContent: "space-between",
                marginTop: 8,
                marginBottom: 8
            }
        },
        [amount, currency]
    );

    var markup = [amountRow];

    if (showRecipient
        && params["bot-db"]
        && params["bot-db"]["public"]
        && params["bot-db"]["public"]["recipient"]
        && context["chat"]["group-chat"] === true) {
        var recipientRow = status.components.text(
            {
                style: {
                    color: "#9199a0",
                    fontSize: 14,
                    lineHeight: 18
                }
            },
            I18n.t('send_sending_to') + " " + params["bot-db"]["public"]["recipient"]["name"]
        );
        markup.push(recipientRow);
    }

    return {
        markup: status.components.view(
            {
                style: {
                    flexDirection: "column"
                }
            },
            markup
        )
    };
}

function shortPreviewSend(params, context) {
    return {
        markup: status.components.text(
            {},
            I18n.t('send_title') + ": "
            + status.localizeNumber(params.amount, context.delimiter, context.separator)
            + " ETH"
        )
    };
}

var personalSend = {
    name: "send",
    scope: ["global", "personal-chats", "registered", "humans"],
    icon: "money_white",
    color: "#5fc48d",
    title: I18n.t('send_title'),
    description: I18n.t('send_description'),
    params: paramsPersonalSend,
    validator: validateSend.bind(this, false),
    handler: handleSend.bind(this, false),
    asyncHandler: true,
    preview: previewSend.bind(this, false),
    shortPreview: shortPreviewSend
};

var groupSend = {
    name: "send",
    scope: ["global", "group-chats", "registered", "humans"],
    icon: "money_white",
    color: "#5fc48d",
    title: I18n.t('send_title'),
    description: I18n.t('send_description'),
    params: paramsGroupSend,
    validator: validateSend.bind(this, true),
    handler: handleSend.bind(this, true),
    asyncHandler: true,
    preview: previewSend.bind(this, true),
    shortPreview: shortPreviewSend
};

status.command(personalSend);
status.response(personalSend);

status.command(groupSend);
status.response(groupSend);

// Request command

var recipientRequestParam = {
    name: "recipient",
    type: status.types.TEXT,
    suggestions: function (params) {
        return {
            title: I18n.t('request_title'),
            markup: status.components.chooseContact(I18n.t('send_choose_recipient'), "recipient", 0)
        };
    }
};

var amountRequestParam = {
    name: "amount",
    type: status.types.NUMBER
};

var paramsPersonalRequest = [amountRequestParam];
var paramsGroupRequest = [recipientRequestParam, amountRequestParam];

function handlePersonalRequest(params, context) {
    var val = params["amount"].replace(",", ".");
    
    return {
        event: "request",
        request: {
            command: "send",
            params: {
                amount: val
            },
            prefill: [val]
        }
    };
}

function handleGroupRequest(params, context) {
    var val = params["amount"].replace(",", ".");
    
    return {
        event: "request",
        request: {
            command: "send",
            params: {
                recipient: context["current-account"]["name"],
                amount: val
            },
            prefill: [context["current-account"]["name"], val],
            prefillBotDb: {
                public: {
                    recipient: context["current-account"]
                }
            }
        }
    };
}

function previewRequest(showRecipient, params, context) {
    var amountRow = status.components.text(
        {},
        I18n.t('request_requesting') + " "
        + status.localizeNumber(params.amount, context.delimiter, context.separator)
        + " ETH"
    );

    var markup = [amountRow];

    if (showRecipient
        && params["bot-db"]
        && params["bot-db"]["public"]
        && params["bot-db"]["public"]["recipient"]
        && context["chat"]["group-chat"] === true) {

        var recipientRow = status.components.text(
            {
                style: {
                    color: "#9199a0",
                    fontSize: 14,
                    lineHeight: 18
                }
            },
            I18n.t('request_requesting_from') + " " + params["bot-db"]["public"]["recipient"]["name"]
        );
        markup.push(recipientRow);
    } 
    
    return {
        markup: status.components.view(
            {
                style: {
                    flexDirection: "column"
                }
            },
            markup
        )
    };
}

function shortPreviewRequest(params, context) {
    return {
        markup: status.components.text(
            {},
            I18n.t('request_requesting') + " "
            + status.localizeNumber(params.amount, context.delimiter, context.separator)
            + " ETH"
        )
    };
}

function validateRequest(validateRecipient, params) {
    if (!params["bot-db"]) {
        params["bot-db"] = {};
    }

    if (validateRecipient) {
        if (!params["bot-db"]["public"] || !params["bot-db"]["public"]["recipient"] || !params["bot-db"]["public"]["recipient"]["address"]) {
            return {
                markup: status.components.validationMessage(
                    "Wrong address",
                    "Recipient address must be specified"
                )
            };
        }
    }
    
    if (!params["amount"]) {
        return {
            markup: status.components.validationMessage(
                I18n.t('validation_title'),
                I18n.t('validation_amount_specified')
            )
        };
    }

    var amount = params.amount.replace(",", ".");
    var amountSplitted = amount.split(".");
    if (amountSplitted.length === 2 && amountSplitted[1].length > 18) {
        return {
            markup: status.components.validationMessage(
                I18n.t('validation_title'),
                I18n.t('validation_amount_is_too_small')
            )
        };
    }

    if (isNaN(parseFloat(params.amount.replace(",", ".")))) {
        return {
            markup: status.components.validationMessage(
                I18n.t('validation_title'),
                I18n.t('validation_invalid_number')
            )
        };
    }
    
    try {
        var val = web3.toWei(amount, "ether");
        if (val < 0) {
            throw new Error();
        }
    } catch (err) {
        return {
            markup: status.components.validationMessage(
                I18n.t('validation_title'),
                I18n.t('validation_invalid_number')
            )
        };
    }
}

status.command({
    name: "request",
    scope: ["global", "personal-chats", "registered", "humans"],
    icon: "money_white",
    color: "#5fc48d",
    title: I18n.t('request_title'),
    description: I18n.t('request_description'),
    params: paramsPersonalRequest,
    handler: handlePersonalRequest,
    preview: previewRequest.bind(null, false),
    shortPreview: shortPreviewRequest,
    validator: validateRequest.bind(null, false)
});

status.command({
    name: "request",
    scope: ["global", "group-chats", "registered", "humans"],
    icon: "money_white",
    color: "#5fc48d",
    title: I18n.t('request_title'),
    description: I18n.t('request_description'),
    params: paramsGroupRequest,
    handler: handleGroupRequest,
    preview: previewRequest.bind(null, true),
    shortPreview: shortPreviewRequest,
    validator: validateRequest.bind(null, true)
});
