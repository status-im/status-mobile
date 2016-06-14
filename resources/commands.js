status.command({
    name: "location",
    description: "Send location",
    color: "#9a5dcf"
}).param({
    name: "address",
    type: status.types.STRING
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

    return status.components.scrollView(
        suggestionsContainerStyle(ph.length),
        suggestions
    );
}

status.response({
    name: "phone",
    description: "Send phone number",
    color: "#5fc48d",
    params: [{
        name: "phone",
        type: status.types.PHONE_NUMBER,
        suggestions: phoneSuggestions
    }],
    handler: function (params) {
        return {
            event: "sign-up",
            params: [params.value]
        };
    }
});

status.command({
    name: "help",
    description: "Help",
    color: "#9a5dcf",
    params: [{
        name: "query",
        type: status.types.STRING
    }]
});

status.response({
    name: "confirmation-code",
    color: "#7099e6",
    description: "Confirmation code",
    parameters: [{
        name: "code",
        type: status.types.NUMBER
    }],
    handler: function (params) {
        return {
            event: "confirm-sign-up",
            params: [params.value]
        };
    }
});

status.response({
    name: "keypair",
    color: "#7099e6",
    description: "Keypair password",
    icon: "icon_lock_white",
    parameters: [{
        name: "password",
        type: status.types.PASSWORD
    }],
    handler: function (params) {
        return {
            event: "save-password",
            params: [params.value]
        };
    }
});

