if(typeof StatusHttpProvider === "undefined"){
var callbackId = 0;
var callbacks = {};

function bridgeSend(data){
    WebViewBridge.send(JSON.stringify(data));
}

function sendAPIrequest(permission, params) {
    var messageId = callbackId++;
    var params = params || {};

    bridgeSend({
        type: 'api-request',
        permission: permission,
        messageId: messageId,
        host: window.location.hostname
    });

    return new Promise(function (resolve, reject) {
        params['resolve'] = resolve;
        params['reject'] = reject;
        callbacks[messageId] = params;
    });
}

function qrCodeResponse(data, callback){
    var result = data.data;
    var regex = new RegExp(callback.regex);
    if (!result) {
        if (callback.reject) {
            callback.reject(new Error("Cancelled"));
        }
    }
    else if (regex.test(result)) {
        if (callback.resolve) {
            callback.resolve(result);
        }
    } else {
        if (callback.reject) {
            callback.reject(new Error("Doesn't match"));
        }
    }
}

WebViewBridge.onMessage = function (message) {
    data = JSON.parse(message);
    var id = data.messageId;
    var callback = callbacks[id];

    if (callback) {
        if (data.type === "api-response") {
            if (data.permission == 'qr-code'){
                qrCodeResponse(data, callback);
            } else if (data.isAllowed) {
                callback.resolve(data.data);
            } else {
                callback.reject(new Error("Denied"));
            }
        } else if (data.type === "web3-send-async-callback") {
            var id = data.messageId;
            var callback = callbacks[id];
            if (callback) {
                if (callback.results) {
                    callback.results.push(data.error || data.result);
                    if (callback.results.length == callback.num)
                        callback.callback(undefined, callback.results);
                } else {
                    callback.callback(data.error, data.result);
                }
            }
        }
    }
};

var StatusAPI = function () {};

StatusAPI.prototype.getContactCode = function () {
    return sendAPIrequest('contact-code');
};

var StatusHttpProvider = function () {};

StatusHttpProvider.prototype.isStatus = true;
StatusHttpProvider.prototype.status = new StatusAPI();
StatusHttpProvider.prototype.isConnected = function () { return true; };

function web3Response (payload, result){
    return {id: payload.id,
            jsonrpc: "2.0",
            result: result};
}

function getSyncResponse (payload) {
    if (payload.method == "eth_accounts"){
        return web3Response(payload, [currentAccountAddress])
    } else if (payload.method == "eth_coinbase"){
        return web3Response(payload, currentAccountAddress)
    } else if (payload.method == "net_version"){
        return web3Response(payload, networkId)
    } else if (payload.method == "eth_uninstallFilter"){
        return web3Response(payload, true);
    } else {
        return null;
    }
}

StatusHttpProvider.prototype.send = function (payload) {
    //TODO to be compatible with MM https://github.com/MetaMask/faq/blob/master/DEVELOPERS.md#dizzy-all-async---think-of-metamask-as-a-light-client
    if (payload.method == "eth_uninstallFilter"){
        this.sendAsync(payload, function (res, err) {})
    }
    var syncResponse = getSyncResponse(payload);
    if (syncResponse){
        return syncResponse;
    } else {
        alert('You tried to send "' + payload.method + '" synchronously. Synchronous requests are not supported, sorry.');
        return null;
    }
};

StatusHttpProvider.prototype.sendAsync = function (payload, callback) {
    var syncResponse = getSyncResponse(payload);
    if (syncResponse && callback){
        callback(null, syncResponse);
    }
    else {
        var messageId = callbackId++;

        if (Array.isArray(payload))
        {
            callbacks[messageId] = {num:      payload.length,
                                    results:  [],
                                    callback: callback};
            for (var i in payload) {
                bridgeSend({type:      'web3-send-async',
                            messageId: messageId,
                            payload:   payload[i]});
            }
        }
        else
        {
            callbacks[messageId] = {callback: callback};
            bridgeSend({type:      'web3-send-async',
                        messageId: messageId,
                        payload:   payload});
        }
    }
};

StatusHttpProvider.prototype.enable = function () {
    return new Promise(function (resolve, reject) { setTimeout(resolve, 1000);});
};

StatusHttpProvider.prototype.scanQRCode = function (regex) {
    return sendAPIrequest('qr-code', {regex: regex});
};
}

var protocol = window.location.protocol
if (typeof web3 === "undefined") {
    //why do we need this condition?
    if (protocol == "https:" || protocol == "http:") {
        console.log("StatusHttpProvider");
        ethereum = new StatusHttpProvider();
        web3 = new Web3(ethereum);
        web3.eth.defaultAccount = currentAccountAddress;
    }
}
