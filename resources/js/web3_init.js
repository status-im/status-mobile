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

function syncResponse(payload, result){
    return {id: payload.id,
            jsonrpc: "2.0",
            result: result};
}

StatusHttpProvider.prototype.send = function (payload) {
    //TODO to be compatible with MM https://github.com/MetaMask/faq/blob/master/DEVELOPERS.md#dizzy-all-async---think-of-metamask-as-a-light-client
    if (payload.method == "eth_accounts"){
        return syncResponse(payload, [currentAccountAddress])
    } else if (payload.method == "eth_coinbase"){
        return syncResponse(payload, currentAccountAddress)
    } else if (payload.method == "net_version"){
        return syncResponse(payload, networkId)
    } else {
        alert('You tried to send "' + payload.method + '" synchronously. Synchronous requests are not supported, sorry.');
        return null;
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
        web3.eth.defaultAccount = currentAccountAddress;
    }
}
