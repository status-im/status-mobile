// Send command/response


function amountParameterBox(params, context) {


    return {
        title: I18n.t('send_title'),
        showBack: true,
        markup: status.components.view({
            flex: 1
        }, [
            status.components.text({
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
            )
        ])
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

function amountSendParam() {
    return {
        name: "amount",
        type: status.types.NUMBER,
        suggestions: amountParameterBox.bind(this)
    };
}

var paramsPersonalSend = [amountSendParam()];
var paramsGroupSend = [recipientSendParam, amountSendParam()];

function validateSend(validateRecipient, params, context) {


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


}

function handleSend(params, context) {

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
    handler: handleSend.bind(this),
    asyncHandler: false
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
    handler: handleSend.bind(this),
    asyncHandler: false
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
    var network = context["network"];

    return {
        event: "request",
        request: {
            command: "send",
            params: {
                network: network,
                amount: val,
            },
            prefill: [val]
        }
    };
}

function handleGroupRequest(params, context) {
    var val = params["amount"].replace(",", ".");
    var network = context["network"];

    return {
        event: "request",
        request: {
            command: "send",
            params: {
                recipient: context["current-account"]["name"],
                network: network,
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
    validator: validateRequest.bind(null, true)
});
