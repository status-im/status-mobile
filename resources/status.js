var _status_catalog = {
        commands: {},
        responses: {},
        functions: {},
        subscriptions: {}
    },
    status = {};

function Command() {
}
function Response() {
}

Command.prototype.addToCatalog = function () {
    _status_catalog.commands[this.name] = this;
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
    this["has-handler"] = com.handler != null;
    this["registered-only"] = com.registeredOnly;
    this.validator = com.validator;
    this.color = com.color;
    this.icon = com.icon;
    this.params = com.params || [];
    this.preview = com.preview;
    this["short-preview"] = com.shortPreview;
    this["suggestions-trigger"] = com.suggestionsTrigger || "on-change";
    this.fullscreen = com.fullscreen;
    this.request = com.request;
    this.addToCatalog();

    return this;
};


Response.prototype = Object.create(Command.prototype);
Response.prototype.addToCatalog = function () {
    _status_catalog.responses[this.name] = this;
};
Response.prototype.onReceiveResponse = function (handler) {
    this.onReceive = handler;
};

var context = {}

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

function text(options, s) {
    s = Array.isArray(s) ? s : [s];
    return ['text', options].concat(s);
}

function view(options, elements) {
    return ['view', options].concat(elements);
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

function scrollView(options, elements) {
    return ['scroll-view', options].concat(elements);
}

function subscribe(path) {
    return ['subscribe', path];
}

function dispatch(path) {
    return ['dispatch', path];
}

function validationMessage(titleText, descriptionText) {
    var titleStyle = {
        style: {
            color: "white",
            fontSize: 12
        }
    };
    var title = status.components.text(titleStyle, titleText);

    var descriptionStyle = {
        style: {
            color: "white",
            fontSize: 12,
            opacity: 0.9
        }
    };
    var description = status.components.text(descriptionStyle, descriptionText);

    return status.components.view(
        {
            backgroundColor: "red",
            height: 61,
            paddingLeft: 16,
            paddingTop: 14,
        },
        [title, description]
    );
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
    on: function (name, fn) {
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
        SET_VALUE: 'set-value'
    },
    components: {
        view: view,
        text: text,
        slider: slider,
        image: image,
        touchable: touchable,
        scrollView: scrollView,
        validationMessage: validationMessage,
        subscribe: subscribe,
        dispatch: dispatch
    },
    browse: function (url) {
        addContext("web-view-url", url);
    },
    setSuggestions: function (view) {
        addContext("suggestions", view);
    },
    setDefaultDb: function (db) {
        addContext("default-db", db);
    },
    updateDb: function (db) {
        addContext("update-db", db)
    },
    sendMessage: function (text) {
        addContext("text-message", text);
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
    }
};

function calculateSubscription(parameters, context) {
    var subscriptionConfig = _status_catalog.subscriptions[parameters.name];
    if (!subscriptionConfig) {
        return;
    }

    return subscriptionConfig.handler(parameters.subscriptions);
}

status.on("subscription", calculateSubscription);

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
