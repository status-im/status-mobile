var _status_catalog = {
        commands: {},
        responses: {},
        functions: {},
        subscriptions: {}
    },
    status = {};

function scopeToBitMask(scope) {
    // this function transforms scopes array to a single integer by generating a bit mask 
    return ((scope != null && scope.indexOf("global") > -1) ? 1 : 0) |
        ((scope != null && scope.indexOf("personal-chats") > -1) ? 2 : 0) |
        ((scope != null && scope.indexOf("group-chats") > -1) ? 4 : 0) |
        ((scope != null && scope.indexOf("anonymous") > -1) ? 8 : 0) |
        ((scope != null && scope.indexOf("registered") > -1) ? 16 : 0) |
        ((scope != null && scope.indexOf("dapps") > -1) ? 32 : 0) |
        ((scope != null && scope.indexOf("humans") > -1) ? 64 : 0) |
        ((scope != null && scope.indexOf("public-chats") > -1) ? 128 : 0);
}

function Command() {
}
function Response() {
}

Command.prototype.addToCatalog = function () {
    _status_catalog.commands[[this.name, this["scope-bitmask"]]] = this;
};

Command.prototype.param = function (parameter) {
    this.params.push(parameter);

    return this;
};

Command.prototype.create = function (com) {
    this.name = com.name;
    this.title = com.title;
    this.description = com.description;
    this.handler = com.handler;
    this["has-handler"] = com.handler !== null;
    this["async-handler"] = (com.handler != null) && com.asyncHandler;
    this.validator = com.validator;
    this.color = com.color;
    this.icon = com.icon;
    this.params = com.params || [];
    this.preview = com.preview;
    this["short-preview"] = com.shortPreview;
    this["on-send"] = com.onSend;
    this.fullscreen = com.fullscreen;
    this.actions = com.actions;
    this.request = com.request;
    this["execute-immediately?"] = com.executeImmediately;
    this["sequential-params"] = com.sequentialParams;
    this["hide-send-button"] = com.hideSendButton;

    // scopes
    this.scope = com.scope; 
    this["scope-bitmask"] = scopeToBitMask(this["scope"]);

    this.addToCatalog();

    return this;
};


Response.prototype = Object.create(Command.prototype);
Response.prototype.addToCatalog = function () {
    _status_catalog.responses[[this.name, this["scope-bitmask"]]] = this;
};
Response.prototype.onReceiveResponse = function (handler) {
    this.onReceive = handler;
};

var context = {};


function addContext(key, value) {
    context[status.message_id][key] = value;
}

function getContext(key) {
    return context[status.message_id][key];
}

function call(pathStr, paramsStr) {
    var params = JSON.parse(paramsStr),
        path = JSON.parse(pathStr),
        fn, callResult, message_id;

    if (typeof params.context !== "undefined" &&
        typeof params.context["message-id"] !== "undefined") {
        message_id = params.context["message-id"];
    } else {
        message_id = null;
    }
    context[message_id] = {};
    status.message_id = message_id;

    fn = path.reduce(function (catalog, name) {
            if (catalog && catalog[name]) {
                return catalog[name];
            }
        },
        _status_catalog
    );

    if (!fn) {
        return null;
    }

    context.messages = [];

    callResult = fn(params.parameters, params.context);
    result = {
        returned: callResult,
        context: context[message_id],
        messages: context.messages
    };

    return JSON.stringify(result);
}

function view(options, elements) {
    return ['view', options].concat(elements);
}

function text(options, s) {
    s = Array.isArray(s) ? s : [s];
    return ['text', options].concat(s);
}

function chatPreviewText(options, s) {
    return ['chat-preview-text', options, s];
}

function textInput(options) {
    return ['text-input', options];
}

function image(options) {
    return ['image', options];
}

function qrCode(options) {
    return ['qr-code', options];
}

function linking(options) {
    return ['linking', options];
}

function slider(options) {
    return ['slider', options];
}

function image(options) {
    return ['image', options];
}

function touchable(options, element) {
    return ['touchable', options, element];
}

function activityIndicator(options) {
    return ['activity-indicator', options];
}

function scrollView(options, elements) {
    return ['scroll-view', options].concat(elements);
}

function subscribe(path) {
    return ['subscribe', path];
}

function dispatch(path) {
    return ['dispatch', path];
}

function webView(url) {
    return ['web-view', {
        source: {
            uri: url
        },
        javaScriptEnabled: true
    }];
}

function bridgedWebView(url) {
    return ['bridged-web-view', {
        url: url
    }];
}

function validationMessage(titleText, descriptionText) {
    return ['validation-message', {
        title: titleText,
        description: descriptionText
    }];
}

function chooseContact(titleText, botDbKey, argumentIndex) {
    return ['choose-contact', {
        title: titleText,
        "bot-db-key": botDbKey,
        index: argumentIndex
    }];
}

function separator() {
    return ['separator'];
}

var status = {
    command: function (h) {
        var command = new Command();
        return command.create(h);
    },
    response: function (h) {
        var response = new Response();
        return response.create(h);
    },
    addListener: function (name, fn) {
        _status_catalog.functions[name] = fn;
    },
    localizeNumber: function (num, del, sep) {
        return I18n.toNumber(
            num.replace(",", "."),
            {
                precision: 10,
                strip_insignificant_zeros: true,
                delimiter: del,
                separator: sep
            });
    },
    types: {
        TEXT: 'text',
        NUMBER: 'number',
        PHONE: 'phone',
        PASSWORD: 'password'
    },
    events: {
        SET_VALUE: 'set-value',
        SET_COMMAND_ARGUMENT: 'set-command-argument',
        UPDATE_DB: 'set',
        SET_COMMAND_ARGUMENT_FROM_DB: 'set-command-argument-from-db',
        SET_VALUE_FROM_DB: 'set-value-from-db',
        FOCUS_INPUT: 'focus-input'
    },
    actions: {
        WEB_VIEW_BACK: 'web-view-back',
        WEB_VIEW_FORWARD: 'web-view-forward',
        FULLSCREEN: 'fullscreen',
        CUSTOM: 'custom',
    },
    components: {
        view: view,
        text: text,
        chatPreviewText: chatPreviewText,
        textInput: textInput,
        slider: slider,
        image: image,
        qrCode: qrCode,
        linking: linking,
        slider: slider,
        touchable: touchable,
        activityIndicator: activityIndicator,
        scrollView: scrollView,
        webView: webView,
        validationMessage: validationMessage,
        bridgedWebView: bridgedWebView,
        chooseContact: chooseContact,
        subscribe: subscribe,
        dispatch: dispatch,
        separator: separator
    },
    showSuggestions: function (view) {
        status.sendSignal("show-suggestions", view);
    },
    setDefaultDb: function (db) {
        addContext("default-db", db);
    },
    updateDb: function (db) {
        addContext("update-db", db)
    },
    sendMessage: function (message) {
        if(typeof message !== "string") {
            message = JSON.stringify(message);
        }
        status.sendSignal("send-message", message);
    },
    addLogMessage: function (type, message) {
        var message = {
            type: type,
            message: JSON.stringify(message)
        };
        var logMessages = getContext("log-messages");
        if (!logMessages) {
            logMessages = [];
        }
        logMessages.push(message);
        addContext("log-messages", logMessages);
    },
    defineSubscription: function (name, subscriptions, handler) {
        _status_catalog.subscriptions[name] = {
            subscriptions: subscriptions,
            handler: handler
        };
    },
    sendSignal: function (eventName, data) {
        var event = {
            event: eventName,
            data: data
        };

        statusSignals.sendSignal(JSON.stringify(event));
    }
};

function calculateSubscription(parameters, context) {
    var subscriptionConfig = _status_catalog.subscriptions[parameters.name];
    if (!subscriptionConfig) {
        return;
    }

    return subscriptionConfig.handler(parameters.subscriptions);
}

status.addListener("subscription", calculateSubscription);

console = (function (old) {
    return {
        log: function (text) {
            old.log(text);
            status.addLogMessage('log', text);
        },
        debug: function (text) {
            old.debug(text);
            status.addLogMessage('debug', text);
        },
        info: function (text) {
            old.info(text);
            status.addLogMessage('info', text);
        },
        warn: function (text) {
            old.warn(text);
            status.addLogMessage('warn', text);
        },
        error: function (text) {
            old.error(text);
            status.addLogMessage('error', text);
        }
    };
}(console));

var localStorage = {};

localStorage.setItem = function(key, value) {
    if(value === null) {
        delete localStorageData[key];
    } else {
        localStorageData[key] = value;
    }

    status.sendSignal("local-storage", JSON.stringify(localStorageData));
};

localStorage.getItem = function (key) {
    if (typeof localStorageData[key] === "undefined") {
        return null;
    }
    return localStorageData[key];
};
