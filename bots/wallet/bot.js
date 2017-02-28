function validateSend(params, context) {
    if (!context.to) {
        return {
            errors: [
                status.components.validationMessage(
                    "Wrong address",
                    "Recipient address must be specified"
                )
            ]
        };
    }
    if (!params.amount) {
        return {
            errors: [
                status.components.validationMessage(
                    I18n.t('validation_title'),
                    I18n.t('validation_amount_specified')
                )
            ]
        };
    }

    try {
        var val = web3.toWei(params.amount.replace(",", "."), "ether");
        if (val <= 0) { throw new Error(); }
    } catch (err) {
        return {
            errors: [
                status.components.validationMessage(
                    I18n.t('validation_title'),
                    I18n.t('validation_invalid_number')
                )
            ]
        };
    }

    var balance = web3.eth.getBalance(context.from);
    var estimatedGas = web3.eth.estimateGas({
        from: context.from,
        to: context.to,
        value: val
    });
    if (bn(val).plus(bn(estimatedGas)).greaterThan(bn(balance))) {
        return {
            errors: [
                status.components.validationMessage(
                    I18n.t('validation_title'),
                    I18n.t('validation_insufficient_amount')
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
        value: web3.toWei(params.amount.replace(",", "."), "ether")
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
    title: I18n.t('send_title'),
    description: I18n.t('send_description'),
    params: [{
        name: "amount",
        type: status.types.NUMBER
    }],
    preview: function (params, context) {
        var amountStyle = {
            fontSize: 36,
            color: "#000000",
            height: 40
        };

        var amount = status.components.view(
            {
                flexDirection: "column",
                alignItems: "flex-end",
            },
            [status.components.text(
                {
                    style: amountStyle,
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
                "ETH"
            )]
        );

        return status.components.view(
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
    },
    handler: sendTransaction,
    validator: validateSend
};

status.command(send);
status.response(send);

status.command({
    name: "request",
    title: I18n.t('request_title'),
    color: "#7099e6",
    description: I18n.t('request_description'),
    params: [{
        name: "amount",
        type: status.types.NUMBER
    }],
    handler: function (params) {
        return {
            event: "request",
            params: [params.amount],
            request: {
                command: "send",
                params: {
                    amount: params.amount
                }
            }
        };
    },
    preview: function (params, context) {
        return I18n.t('request_requesting')
            + status.localizeNumber(params.amount, context.delimiter, context.separator)
            + " ETH";
    },
    validator: function(params) {
        try {
            var val = web3.toWei(params.amount, "ether");
            if (val <= 0) { throw new Error(); }
        } catch (err) {
            return {
                errors: [
                    status.components.validationMessage(
                        I18n.t('validation_title'),
                        I18n.t('validation_invalid_number')
                    )
                ]
            };
        }
    }
});

