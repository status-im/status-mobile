if(typeof StatusHttpProvider === "undefined"){
var callbackId = 0;
var callbacks = {};

function bridgeSend(data){
    WebViewBridge.send(JSON.stringify(data));
}

window.addEventListener('message', function (event) {
    if (!event.data || !event.data.type) { return; }
    if (event.data.type === 'STATUS_API_REQUEST') {
        bridgeSend({
            type: 'status-api-request',
            permissions: event.data.permissions,
            host: window.location.hostname
        });
    }
});

WebViewBridge.onMessage = function (message) {
    data = JSON.parse(message);

    if (data.type === "navigate-to-blank")
        window.location.href = "about:blank";

    else if (data.type === "status-api-success")
    {
        window.dispatchEvent(new CustomEvent('statusapi', { detail: { permissions: data.keys,
                                                                      data:        data.data
                                                                    } }));
    }

    else if (data.type === "web3-send-async-callback")
    {
        var id = data.messageId;
        var callback = callbacks[id];
        if (callback) {
            if (callback.results)
            {
                callback.results.push(data.error || data.result);
                if (callback.results.length == callback.num)
                    callback.callback(undefined, callback.results);
            }
            else
            {
                callback.callback(data.error, data.result);
            }
        }
    }

    else if (data.type === "scan-qr-code-callback")
    {
        var id = data.data.messageId;
        var callback = callbacks[id];
        if (callback) {
            var result = data.result;
            var regex = new RegExp(callback.regex);
            if (regex.test(result)) {
                if (callback.resolve) {
                    callback.resolve(result);
                }
            } else {
                if (callback.reject) {
                    callback.reject(result);
                }
            }
        }
    }
};

var StatusHttpProvider = function () {};

StatusHttpProvider.prototype.isStatus = true;
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

StatusHttpProvider.prototype.scanQRCode = function (regex) {
    return new Promise(function (resolve, reject) {
        var messageId = callbackId++;
        callbacks[messageId] = {resolve: resolve, reject: reject, regex: regex};
        bridgeSend({type:  'scan-qr-code',
                    messageId: messageId});
    });
};


StatusHttpProvider.prototype.enable = function () {
    return new Promise(function (resolve, reject) { setTimeout(resolve, 1000);});
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
