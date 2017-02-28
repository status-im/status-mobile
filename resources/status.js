var _status_catalog = {
        commands: {},
        responses: {},
        functions: {}
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

function addContext(ns, key, value) {
    context[ns][key] = value;
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

function image(options) {
    return ['image', options];
}

function touchable(options, element) {
    return ['touchable', options, element];
}

function scrollView(options, elements) {
    return ['scroll-view', options].concat(elements);
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
    registerFunction: function (name, fn) {
        _status_catalog.functions[name] = fn;
    },
    autorun: function (commandName) {
        _status_catalog.autorun = commandName;
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
        image: image,
        touchable: touchable,
        scrollView: scrollView,
        validationMessage: validationMessage
    },
    browse: function (url) {
        addContext(status.message_id, "web-view-url", url);
    }
};
