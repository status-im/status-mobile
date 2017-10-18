if(typeof StatusHttpProvider === "undefined"){
var callbackId = 0;
var callbacks = {};

function httpCallback(id, data) {
    var result = data;
    var error = null;

    try {
        result = JSON.parse(data);
    } catch (e) {
        error = {message: "InvalidResponse"};
    }

    if (callbacks[id]) {
        callbacks[id](error, result);
    }
}

var StatusHttpProvider = function (host, timeout) {
    this.host = host || 'http://localhost:8545';
    this.timeout = timeout || 0;
};

StatusHttpProvider.prototype.send = function (payload) {
    if (typeof StatusBridge == "undefined") {
        if (window.location.protocol == "https:") {
            throw new Error('You tried to send "' + payload.method + '" synchronously. Synchronous requests are not supported, sorry.');
        }

        var request = this.prepareRequest(false);

        try {
            request.send(JSON.stringify(payload));
        } catch (error) {
            throw errors.InvalidConnection(this.host);
        }

        var result = request.responseText;

        try {
            result = JSON.parse(result);
        } catch (e) {
            throw errors.InvalidResponse(request.responseText);
        }

        return result;
    } else {
        result = StatusBridge.sendRequestSync(this.host, JSON.stringify(payload));

        try {
            result = JSON.parse(result);
        } catch (e) {
            throw new Error("InvalidResponse: " + result);
        }

        return result;
    }
};

StatusHttpProvider.prototype.prepareRequest = function () {
    var request = new XMLHttpRequest();

    request.open('POST', this.host, false);
    request.setRequestHeader('Content-Type', 'application/json');
    return request;
};

function sendAsync(payload, callback) {

    var messageId = callbackId++;
    callbacks[messageId] = callback;
    if (typeof StatusBridge == "undefined") {
        var data = {
            payload: JSON.stringify(payload),
            callbackId: JSON.stringify(messageId),
            host: this.host
        };

        webkit.messageHandlers.sendRequest.postMessage(JSON.stringify(data));
    } else {
        StatusBridge.sendRequest(this.host, JSON.stringify(messageId), JSON.stringify(payload));
    }
};

StatusHttpProvider.prototype.sendAsync = sendAsync;

/**
 * Synchronously tries to make Http request
 *
 * @method isConnected
 * @return {Boolean} returns true if request haven't failed. Otherwise false
 */
StatusHttpProvider.prototype.isConnected = function () {
    try {
        this.sendAsync({
            id: 9999999999,
            jsonrpc: '2.0',
            method: 'net_listening',
            params: []
        }, function () {
        });
        return true;
    } catch (e) {
        return false;
    }
};
}

var protocol = window.location.protocol
var address = providerAddress || "http://localhost:8545";
console.log(protocol);
if (typeof web3 === "undefined") {
    if (protocol == "https:" || protocol == "http:") {
        console.log("StatusHttpProvider");
        web3 = new Web3(new StatusHttpProvider(address));
    }
}
