function jsSuggestionsContainerStyle(suggestionsCount) {
    return {
        marginVertical: 1,
        marginHorizontal: 0,
        keyboardShouldPersistTaps: "always",
        //height: Math.min(150, (56 * suggestionsCount)),
        backgroundColor: "white",
        borderRadius: 5,
        keyboardShouldPersistTaps: "always"
    };
}

var jsSuggestionContainerStyle = {
    paddingLeft: 16,
    backgroundColor: "white"
};

var jsSubContainerStyle = {
    //height: 56,
    paddingTop: 9,
    borderBottomWidth: 1,
    borderBottomColor: "#0000001f"
};

var jsValueStyle = {
    fontSize: 14,
    fontFamily: "font",
    color: "#000000de"
};

var jsBoldValueStyle = {
    fontSize: 14,
    fontFamily: "font",
    color: "#000000de",
    fontWeight: "bold"
};

var jsDescriptionStyle = {
    marginTop: 1.5,
    paddingBottom: 9,
    fontSize: 14,
    fontFamily: "font",
    color: "#838c93de"
};

if (!String.prototype.startsWith) {
    String.prototype.startsWith = function (searchString, position) {
        position = position || 0;
        return this.substr(position, searchString.length) === searchString;
    };
}

function matchSubString(array, string) {
    var matched = [];
    for (var i = 0; i < array.length; i++) {
        var item = array[i];
        if (item.toLowerCase().startsWith(string.toLowerCase())) {
            matched.push(item);
        }
    }
    return matched;
}

function cleanCode(code) {
    // remove comments
    var commentsRegex = /\/\*.+?\*\/|\/\/.*/g;
    code = code.replace(commentsRegex, "");
    // replace string literals
    var literalsRegex = /\"(?:\\\\\"|[^\"])*?\"/g;
    code = code.replace(literalsRegex, '""');
    var literalsRegex = /\'(?:\\\\\'|[^\'])*?\'/g;
    code = code.replace(literalsRegex, '""');

    return code
}

function createObjectSuggestion(name, docInfo, code, parameterNumber) {
    var title = name;
    if (docInfo.args) {
        title += "(";
        for (var i = 0; i < docInfo.args.length; i++) {
            var argument = docInfo.args[i];
            var argumentText = (i > 0 ? ", " : "") + (parameterNumber === i ? "*" + argument.name + "*" : argument.name);
            if (argument.optional) {
                argumentText = "[" + argumentText + "]";
            }
            title += argumentText;
        }
        title += ")";
    }
    if (!docInfo.desc) {
        name += ".";
    } else if (docInfo.args) {
        name += "(";
        if (docInfo.args.length == 0) {
            name += ")";
        }
    }
    var suggestion = {
        title: title,
        desc: docInfo.desc
    };

    if (code != null) {
        suggestion.pressValue = code + name;
    }
    return suggestion;
}

var lastMessage = null;

function getLastForm(code) {
    var codeLength = code.length;
    var form = '';
    var level = 0;
    var index = codeLength - 1;
    while (index >= 0) {
        var char = code[index];
        if (level == 0 && index != 0 && (char == '(' || char == ',')) {
            break;
        }
        if (char == ')') {
            level--;
        }
        if (char == '(') {
            level++;
        }
        form = char + form;
        index--;
    }
    return form;
}

function getLastLevel(code) {
    var codeLength = code.length;
    var form = '';
    var index = codeLength - 1;
    var nested = false;
    var level = 0;
    while (index >= 0) {
        var char = code[index];
        if (char == ')') {
            level--;
            // A single unbalanced ')' shouldn't set nested to true.
            if (level > 0) {
                nested = true;
            }
        }
        if (char == '(') {
            level++;
            if (level == 0) {
                nested = false;
            }
            if (level <= 0) {
                form = "argument" + form;
                index--;
                continue;
            }
        }
        if ((level == 1 && char == ',') || level == 2) {
            break;
        }
        if (!nested || level > 0) {
            form = char + form;
        }
        index--;
    }
    if (form.indexOf("(") < 0 && form != ",") {
        var parts = form.split(',');
        form = parts[parts.length - 1];
    }
    return form;
}

function getPartialSuggestions(doc, fullCode, code) {
    var suggestions = [];
    var functionParts = code.split("(");
    var objectParts = code.split(/[^a-zA-Z_0-9\$\-\u00C0-\u1FFF\u2C00-\uD7FF\w]+/);

    var index = 0;
    var suggestedFunction = '';
    while (index < objectParts.length) {
        var part = objectParts[index];
        if (part != "desc" && part != "args" && doc[part] != null) {
            doc = doc[part];
            suggestedFunction += part + '.';
            index++;
        } else {
            break;
        }
    }
    suggestedFunction = suggestedFunction.substring(0, suggestedFunction.length - 1)
    if (functionParts.length == 1) {
        // object suggestions
        if (index > objectParts.length - 1) {
            var suggestion = objectParts[objectParts.length - 1];
            suggestions.push(createObjectSuggestion(suggestion, doc, fullCode.substring(0, fullCode.length - suggestion.length)));
        } else if (index === objectParts.length - 1) {
            var lastPart = objectParts[index];
            var keys = Object.keys(doc);
            var matches = matchSubString(keys, lastPart);

            for (var i = 0; i < matches.length; i++) {
                var suggestion = matches[i];
                if (suggestion == "desc" || suggestion == "args") {
                    continue;
                }
                var docInfo = doc[suggestion];
                if (docInfo != null) {
                    suggestions.push(createObjectSuggestion(suggestion, docInfo, fullCode.substring(0, fullCode.length - lastPart.length)));
                }
            }
        }
    } else if (functionParts.length == 2) {
        // parameter suggestions
        var parameters = functionParts[1].split(",");
        if (doc.args && parameters.length <= doc.args.length && parameters[parameters.length - 1].indexOf(")") < 0) {
            var paramInfo = doc.args[parameters.length - 1];
            var docInfo = doc;
            docInfo.desc = paramInfo.name + ": " + paramInfo.desc;
            suggestions.push(createObjectSuggestion(suggestedFunction, docInfo, null, parameters.length - 1));
        }
    }
    //console.debug(suggestions);
    return suggestions;
}

function getJsSuggestions(code, context) {
    var suggestions = [];
    var doc = DOC_MAP;
    // TODO: what's /c / doing there ???
    //console.debug(code);
    var previousMessage = localStorage.getItem("previousMessage");
    if (!code || code == "" || code == "c ") {
        code = "";
        //console.debug("Last message: " + context.data);
        if (previousMessage != null) {
            suggestions.push({
                title: 'Last command used:',
                desc: previousMessage,
                pressValue: previousMessage
            });
        }
        var keys = Object.keys(doc);
        for (var i = 0; i < keys.length; i++) {
            var suggestion = keys[i];
            var docInfo = doc[suggestion];
            if (docInfo != null) {
                suggestions.push(createObjectSuggestion(suggestion, docInfo, ""));
            }
        }
    } else {
        // TODO: what's /c / doing there ???
        if (code.startsWith("c ")) {
            code = code.substring(2);
        }
        if (previousMessage != null &&
            (typeof previousMessage === 'string' || previousMessage instanceof String) &&
            previousMessage.startsWith(code)) {
            suggestions.unshift({
                title: 'Last command used:',
                desc: previousMessage,
                pressValue: previousMessage
            });
        }
        var originalCode = code;
        code = cleanCode(code);
        var levelCode = getLastLevel(code);
        var code = getLastForm(levelCode);
        if (levelCode != code) {
            suggestions = getPartialSuggestions(doc, originalCode, levelCode);
        }

        //console.debug("Final code: " + code);
        //console.debug("Level code: " + levelCode);
        suggestions = suggestions.concat(getPartialSuggestions(doc, originalCode, code));
    }
    return suggestions;
}

function createMarkupText(text) {
    var parts = [];
    var index = 0;
    var currentText = '';
    var isBold = false;
    while (index < text.length) {
        var char = text[index];
        if (char == '*') {
            if (currentText != '') {
                parts.push(
                    status.components.text(
                        {style: isBold ? jsBoldValueStyle : jsValueStyle},
                        currentText
                    )
                );
                currentText = '';
            }
            isBold = !isBold;
        } else {
            currentText += char;
        }
        index++;
    }
    if (currentText != '') {
        parts.push(
            status.components.text(
                {style: isBold ? jsBoldValueStyle : jsValueStyle},
                currentText
            )
        );
    }
    //console.debug(parts);
    return parts;
}

function jsSuggestions(params, context) {
    var suggestions = getJsSuggestions(params.code, context);
    var sugestionsMarkup = [];

    for (var i = 0; i < suggestions.length; i++) {
        var suggestion = suggestions[i];

        if (suggestion.title.indexOf('*') >= 0) {
            suggestion.title = createMarkupText(suggestion.title);
        }
        var suggestionMarkup = status.components.view(jsSuggestionContainerStyle,
            [status.components.view(jsSubContainerStyle,
                [
                    status.components.text({style: jsValueStyle},
                        suggestion.title),
                    status.components.text({style: jsDescriptionStyle},
                        suggestion.desc)
                ])]);
        if (suggestion.pressValue) {
            suggestionMarkup = status.components.touchable({
                    onPress: status.components.dispatch([status.events.SET_VALUE, suggestion.pressValue])
                },
                suggestionMarkup
            );
        }
        sugestionsMarkup.push(suggestionMarkup);
    }

    if (sugestionsMarkup.length > 0) {
        var view = status.components.scrollView(jsSuggestionsContainerStyle(sugestionsMarkup.length),
            sugestionsMarkup
        );
        return {markup: view};
    } else {
        return {markup: null};
    }
}


function jsHandler(params, context) {
    var result = {
        err: null,
        data: null,
        messages: []
    };
    messages = [];
    try {
        result["text-message"] = String(JSON.stringify(eval(params.code)));
        localStorage.setItem("previousMessage", params.code);
    } catch (e) {
        result.err = e;
    }

    result.messages = messages;

    return result;
}

function suggestionsContainerStyle(suggestionsCount) {
    return {
        marginVertical: 1,
        marginHorizontal: 0,
        keyboardShouldPersistTaps: "always",
        height: Math.min(150, (56 * suggestionsCount)),
        backgroundColor: "white",
        borderRadius: 5,
        flexGrow: 1
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

var ropstenNetworkId = 3;
var rinkebyNetworkId = 4;

var ropstenFaucets = [
    {
        name: "Status Testnet Faucet",
        url: "http://51.15.45.169:3001",
    }
];

var rinkebyFaucets = [
    {
        name: "Status Rinkeby Faucet",
        url: "http://51.15.60.23:3001",
    }
];

function getFaucets(networkId) {
    if (networkId == ropstenNetworkId) {
        return ropstenFaucets;
    } else if (networkId == rinkebyNetworkId) {
        return rinkebyFaucets;
    } else {
        return [];
    }
}

var faucets = getFaucets(status.ethereumNetworkId);

function faucetSuggestions(params) {
    var suggestions = faucets.map(function (entry) {
        return status.components.touchable(
            {onPress: status.components.dispatch([status.events.SET_COMMAND_ARGUMENT, [0, entry.url, false]])},
            status.components.view(
                suggestionContainerStyle,
                [status.components.view(
                    suggestionSubContainerStyle,
                    [
                        status.components.text(
                            {style: valueStyle},
                            entry.name
                        ),
                        status.components.text(
                            {style: descriptionStyle},
                            entry.url
                        )
                    ]
                )]
            )
        );
    });

    var view = status.components.scrollView(
        suggestionsContainerStyle(faucets.length),
        suggestions
    );

    return {markup: view};
}

var faucetCommandConfig ={
    name: "faucet",
    title: I18n.t('faucet_title'),
    description: I18n.t('faucet_description'),
    color: "#7099e6",
    scope: ["personal-chats", "registered", "dapps"],
    params: [{
        name: "url",
        type: status.types.TEXT,
        suggestions: faucetSuggestions,
        placeholder: I18n.t('faucet_placeholder')
    }],
    preview: function (params) {
        return {
            markup: status.components.text(
                {},
                params.url
            )
        };
    },
    shortPreview: function (params) {
        return {
            markup: status.components.chatPreviewText(
                {},
                I18n.t('faucet_title') + ": " + params.url
            )
        };
    },
    validator: function (params, context) {
        var f = faucets.map(function (entry) {
            return entry.url;
        });

            if (f.indexOf(params.url) == -1) {
                var error = status.components.validationMessage(
                    I18n.t('faucet_incorrect_title'),
                    I18n.t('faucet_incorrect_description')
                );

                return {markup: error};
            }
        }
    }
;

if (faucets.length > 0) {
    status.command(faucetCommandConfig);
}

function debugSuggestions(params) {
    var suggestions = ["On", "Off"].map(function (entry) {
        return status.components.touchable(
            {onPress: status.components.dispatch([status.events.SET_COMMAND_ARGUMENT, [0, entry, false]])},
            status.components.view(
                suggestionContainerStyle,
                [status.components.view(
                    suggestionSubContainerStyle,
                    [
                        status.components.text(
                            {style: valueStyle},
                            entry
                        )
                    ]
                )]
            )
        );
    });

    var view = status.components.scrollView(
        suggestionsContainerStyle(faucets.length),
        suggestions
    );

    return {markup: view};
}

status.command({
    name: "debug",
    title: I18n.t('debug_mode_title'),
    description: I18n.t('debug_mode_description'),
    color: "#7099e6",
    scope: ["personal-chats", "registered", "dapps"],
    params: [{
        name: "mode",
        suggestions: debugSuggestions,
        type: status.types.TEXT
    }],
    preview: function (params) {
        return {
            markup: status.components.text(
                {},
                I18n.t('debug_mode_title') + ": " + params.mode
            )
        };
    },
    shortPreview: function (params) {
        return {
            markup: status.components.chatPreviewText(
                {},
                I18n.t('debug_mode_title') + ": " + params.mode
            )
        };
    }
});

status.response({
    name: "password",
    color: "#7099e6",
    scope: ["personal-chats", "anonymous", "dapps"],
    description: I18n.t('password_description'),
    icon: "lock_white",
    sequentialParams: true,
    params: [
        {
            name: "password",
            type: status.types.PASSWORD,
            placeholder: I18n.t('password_placeholder'),
            hidden: true
        },
        {
            name: "password-confirmation",
            type: status.types.PASSWORD,
            placeholder: I18n.t('password_placeholder2'),
            hidden: true
        }
    ],
    validator: function (params, context) {
        if (!params.hasOwnProperty("password-confirmation") || params["password-confirmation"].length === 0) {
            if (params.password === null || params.password.length < 6) {
                var error = status.components.validationMessage(
                    I18n.t('password_validation_title'),
                    I18n.t('password_error')
                );
                return {markup: error};
            }
        } else {
            if (params.password !== params["password-confirmation"]) {
                var error = status.components.validationMessage(
                    I18n.t('password_validation_title'),
                    I18n.t('password_error1')
                );
                return {markup: error};
            }
        }

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

        return {markup: status.components.text({style: style}, "●●●●●●●●●●")};
    }
});

status.response({
    name: "grant-permissions",
    scope: ["personal-chats", "anonymous", "registered", "dapps"],
    color: "#7099e6",
    description: "Grant permissions",
    icon: "lock_white",
    executeImmediately: true
});

status.addListener("on-message-input-change", function (params, context) {
    return jsSuggestions({code: params.message}, context);
});

status.addListener("on-message-send", function (params, context) {
    return jsHandler({code: params.message}, context);
});
