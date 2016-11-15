I18n.translations = {
  en: {
    location_title: 'Location',
    location_description: 'Share your location',
    location_address: 'Address',

    browse_title: 'Browser',
    browse_description: 'Launch the browser',

    send_title: 'Send ETH',
    send_description: 'Send a payment',

    request_title: 'Request ETH',
    request_description: 'Request a payment',
    request_requesting: 'Requesting ',

    validation_title: 'Amount',
    validation_amount_specified: 'Amount must be specified',
    validation_invalid_number: 'Amount is not valid number',
    validation_insufficient_amount: 'Not enough ETH on balance ('
  },
  ru: {
    location_title: 'Местоположение',
    location_description: 'Поделитесь своим местоположением',
    location_address: 'Адрес',

    browse_title: 'Браузер',
    browse_description: 'Запуск браузера',

    send_title: 'Отправить ETH',
    send_description: 'Отправить платеж',

    request_title: 'Запросить ETH',
    request_description: 'Запросить платеж',
    request_requesting: 'Запрос ',

    validation_title: 'Сумма',
    validation_amount_specified: 'Необходимо указать сумму',
    validation_invalid_number: 'Сумма не является действительным числом',
    validation_insufficient_amount: 'Недостаточно ETH на балансе ('
  },
  af: {
    location_title: 'Ligging',
    location_description: 'Deel jou ligging',
    location_address: 'Addres',

    browse_title: 'Webblaaier',
    browse_description: 'Begin die webblaaier',

    send_title: 'Stuur ETH',
    send_description: 'Stuur \'n betaling',

    request_title: 'Versoek ETH',
    request_description: 'Versoek \'n betaling',
    request_requesting: 'Besig met versoek ',

    validation_title: 'Bedrag',
    validation_amount_specified: 'Bedrag moet gespesifiseer word',
    validation_invalid_number: 'Bedrag is nie \'n geldige syfer nie',
    validation_insufficient_amount: 'Nie genoeg ETH in rekening nie ('
  },
  ar: {
    location_title: 'الموقع',
    location_description: 'شارك موقعك',
    location_address: 'العنوان',

    browse_title: 'المتصفح',
    browse_description: 'تشغيل المتصفح',

    send_title: 'إرسال ETH',
    send_description: 'إرسال مدفوعات',

    request_title: 'طلب ETH',
    request_description: 'طلب مدفوعات',
    request_requesting: 'مُطَالَبَة ',

    validation_title: 'المبلغ',
    validation_amount_specified: 'يجب تحديد المبلغ',
    validation_invalid_number: 'المبلغ المحدد غير صحيح',
    validation_insufficient_amount: 'لا يوجد ETH  كافي بالحساب ('
  }
};

status.command({
    name: "location",
    icon: "location",
    title: I18n.t('location_title'),
    description: I18n.t('location_description'),
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
    placeholder: I18n.t('location_address')
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
    title: I18n.t('browse_title'),
    description: I18n.t('browse_description'),
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
                    I18n.t('validation_title'),
                    I18n.t('validation_amount_specified')
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
                    I18n.t('validation_title'),
                    I18n.t('validation_invalid_number')
                )
            ]
        };
    }

    var balance = web3.eth.getBalance(context.from);
    if (bn(val).greaterThan(bn(balance))) {
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
    title: I18n.t('send_title'),
    description: I18n.t('send_description'),
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
    title: I18n.t('request_title'),
    color: "#7099e6",
    description: I18n.t('request_description'),
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
                content: I18n.t('request_requesting') + params.amount + "ETH"
            }
        };
    },
});
