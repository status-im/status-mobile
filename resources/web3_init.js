var callbackId = 0;
var callbacks = {};

function httpCallback(id, data) {
    var result = data;
    var error = null;

    console.log(data);

    try {
        result = JSON.parse(data);
    } catch(e) {
        error = {message: "InvalidResponse"};
    }


    callbacks[id](error, result);
}

var StatusHttpProvider = function (host, timeout) {
    this.host = host || 'http://localhost:8545';
    this.timeout = timeout || 0;
};

StatusHttpProvider.prototype.send = function (payload) {
    throw new Error('You tried to send "'+ payload.method +'" synchronously. Synchronous requests are not supported, sorry.');
};

StatusHttpProvider.prototype.sendAsync = function (payload, callback) {

    var messageId = callbackId++;
    callbacks[messageId] = callback;
    if(typeof StatusBridge == "undefined") {
        var data = {
            payload: JSON.stringify(payload),
            callbackId: JSON.stringify(messageId)
        };

        webkit.messageHandlers.sendRequest.postMessage(JSON.stringify(data));
    } else {
        StatusBridge.sendRequest(JSON.stringify(messageId), JSON.stringify(payload));
    }
};

/**
 * Synchronously tries to make Http request
 *
 * @method isConnected
 * @return {Boolean} returns true if request haven't failed. Otherwise false
 */
StatusHttpProvider.prototype.isConnected = function() {
    try {
        this.sendAsync({
            id: 9999999999,
            jsonrpc: '2.0',
            method: 'net_listening',
            params: []
        }, function(){});
        return true;
    } catch(e) {
        return false;
    }
};

web3 = new Web3(new StatusHttpProvider("http://localhost:8545"));
