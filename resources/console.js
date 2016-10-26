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
    params: [{
        name: "phone",
        type: status.types.PHONE,
        suggestions: phoneSuggestions,
        placeholder: "Phone number"
    }]
};
status.response(phoneConfig);
status.command(phoneConfig);


status.command({
    name: "help",
    description: "Help",
    color: "#7099e6",
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
    validator: function (params) {
        if (!/^[\d]{4}$/.test(params.code)) {
            var error = status.components.validationMessage(
                "Confirmation code",
                "Wrong format"
            );

            return {errors: [error]}
        }
    }
});

status.response({
    name: "password",
    color: "#7099e6",
    description: "Password",
    icon: "lock_white",
    params: [{
        name: "password",
        type: status.types.PASSWORD,
        placeholder: "Type your password"
    }, {
        name: "password-confirmation",
        type: status.types.PASSWORD,
        placeholder: "Please re-enter password to confirm"
    }],
    validator: function (params, context) {
        var errorMessages = [];
        var currentParameter = context["current-parameter"];

        if (
            currentParameter == "password" &&
            params.password.length < 6
        ) {
            errorMessages.push("Password should be not less then 6 symbols.");
        }

        if (currentParameter == "password-confirmation" &&
            params.password != params["password-confirmation"]) {
            errorMessages.push("Password confirmation doesn't match password.");
        }

        if (errorMessages.length) {
            var errors = [];
            for (var idx in errorMessages) {
                errors.push(
                    status.components.validationMessage(
                        "Password",
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
