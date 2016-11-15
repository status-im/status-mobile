I18n.translations = {
  en: {
    phone_title: 'Send Phone Number',
    phone_description: 'Find friends using your number',
    phone_placeholder: 'Phone number',

    confirm_description: 'Confirmation code',
    confirm_validation_title: 'Confirmation code',
    confirm_validation_description: 'Wrong format',

    password_description: 'Password',
    password_placeholder: 'Type your password',
    password_placeholder2: 'Please re-enter password to confirm',
    password_error: 'Password should be not less then 6 symbols.',
    password_error1: 'Password confirmation doesn\'t match password.',
    password_validation_title: 'Password'

  },
  ru: {
    phone_title: 'Отправить номер телефона',
    phone_description: 'Найти друзей, используя ваш номер',
    phone_placeholder: 'Номер телефона',

    confirm_description: 'Код подтверждения',
    confirm_validation_title: 'Код подтверждения',
    confirm_validation_description: 'Неверный формат',

    password_description: 'Пароль',
    password_placeholder: 'Введите свой пароль',
    password_placeholder2: 'Повторно введите пароль для подтверждения',
    password_error: 'Пароль должен содержать не менее 6 символов',
    password_error1: 'Подтверждение пароля не совпадает с паролем',
    password_validation_title: 'Пароль'

  },
  af: {
    phone_title: 'Stuur telefoonnommer',
    phone_description: 'Vind vriende deur jou nommer te gebruik',
    phone_placeholder: 'Telefoonnommer',

    confirm_description: 'Bevestigingskode',
    confirm_validation_title: 'Bevestigingskode',
    confirm_validation_description: 'Verkeerde formaat',

    password_description: 'Wagwoord',
    password_placeholder: 'Tik jou wagwoord in',
    password_placeholder2: 'Tik asseblief weer jou wagwoord in om te bevestig',
    password_error: 'Wagwoord mag nie minder as 6 simbole wees nie.',
    password_error1: 'Wagwoordbevestiging is nie dieselfde as wagwoord nie.',
    password_validation_title: 'Wagwoord'

  }, ar: {
    phone_title: 'أرسل رقم الهاتف',
    phone_description: 'ابحث عن الأصدقاء باستخدام رقمك',
    phone_placeholder: 'رقم الهاتف',

    confirm_description: 'رمز التأكيد',
    confirm_validation_title: 'رمز التأكيد',
    confirm_validation_description: 'صيغة خاطئة',

    password_description: 'كلمة المرور',
    password_placeholder: 'اكتب كلمة المرور الخاصة بك',
    password_placeholder2: 'الرجاء إعادة إدخال كلمة المرور للتأكيد',
    password_error: 'ينبغي أن لا تقل كلمة المرور عن 6 رموز.',
    password_error1: 'لا يتوافق تأكيد كلمة المرور مع كلمة المرور.',
    password_validation_title: 'كلمة المرور'

  }
};

var phones = [ // TODO this is supposed to be regionalised
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
        borderRadius: 5,
        flex: 1
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
    if (!params.phone || params.phone == "") {
        ph = phones;
    } else {
        ph = phones.filter(function (phone) {
            return startsWith(phone.number, params.phone);
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

    /*var view = status.components.view(
        {style: {flex: 1, flexDirection: "column"}},
        [status.components.scrollView(
            suggestionsContainerStyle(ph.length),
            suggestions
        )]
    );*/

    var view = status.components.scrollView(
        suggestionsContainerStyle(ph.length),
        suggestions
    );

    return {markup: view};
}

var phoneConfig = {
    name: "phone",
    icon: "phone_white",
    title: I18n.t('phone_title'),
    description: I18n.t('phone_description'),
    color: "#5bb2a2",
    params: [{
        name: "phone",
        type: status.types.PHONE,
        suggestions: phoneSuggestions,
        placeholder: I18n.t('phone_placeholder')
    }]
};
status.response(phoneConfig);
status.command(phoneConfig);


// status.command({
//     name: "help",
//     title: "Help",
//     description: "Request help from Console",
//     color: "#7099e6",
//     params: [{
//         name: "query",
//         type: status.types.TEXT
//     }]
// });

status.response({
    name: "confirmation-code",
    color: "#7099e6",
    description: I18n.t('confirm_description'),
    params: [{
        name: "code",
        type: status.types.NUMBER
    }],
    validator: function (params) {
        if (!/^[\d]{4}$/.test(params.code)) {
            var error = status.components.validationMessage(
                I18n.t('confirm_validation_title'),
                I18n.t('confirm_validation_description')
            );

            return {errors: [error]}
        }
    }
});

status.response({
    name: "password",
    color: "#7099e6",
    description: I18n.t('password_description'),
    icon: "lock_white",
    params: [{
        name: "password",
        type: status.types.PASSWORD,
        placeholder: I18n.t('password_placeholder')
    }, {
        name: "password-confirmation",
        type: status.types.PASSWORD,
        placeholder: I18n.t('password_placeholder2')
    }],
    validator: function (params, context) {
        var errorMessages = [];
        var currentParameter = context["current-parameter"];

        if (
            currentParameter == "password" &&
            params.password.length < 6
        ) {
            errorMessages.push(I18n.t('password_error'));
        }

        if (currentParameter == "password-confirmation" &&
            params.password != params["password-confirmation"]) {
            errorMessages.push(I18n.t('password_error1'));
        }

        if (errorMessages.length) {
            var errors = [];
            for (var idx in errorMessages) {
                errors.push(
                    status.components.validationMessage(
                        I18n.t('password_validation_title'),
                        errorMessages[idx]
                    )
                );
            }

            return {errors: errors};
        }

        return {params: params, context: context};
    },
    preview: function (params, context) {
        var style = {
            marginTop: 5,
            marginHorizontal: 0,
            fontSize: 14,
            color: "black"
        };

        if (context.platform == "ios") {
            style.fontSize = 8;
            style.marginTop = 10;
            style.marginBottom = 2;
            style.letterSpacing = 1;
        }

        return status.components.text({style: style}, "●●●●●●●●●●");
    }
});
