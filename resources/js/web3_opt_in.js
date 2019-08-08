if(typeof ReadOnlyProvider === "undefined"){
var callbackId = 0;
var callbacks = {};
var currentAccountAddress;

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
        params: params
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
                if (data.permission == 'web3') {
                    currentAccountAddress = data.data[0];
                }
                callback.resolve(data.data);
            } else {
                callback.reject({code: 4001, message: "User denied authorizing any accounts for the dapp."});
            }
        } else if (data.type === "web3-send-async-callback") {
            var id = data.messageId;
            var callback = callbacks[id];
            if (callback) {
                if (callback.beta){
                    if (data.error || data.result.error){
                        callback.reject(data.error || data.result.error);
                    }
                    else{
                        callback.resolve(data.result.result);
                    }
                }
                else if (callback.results) {
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

function web3Response (payload, result){
    return {id: payload.id,
            jsonrpc: "2.0",
            result: result};
}

function getSyncResponse (payload) {
    if (payload.method == "eth_accounts" && currentAccountAddress){
        return web3Response(payload, [currentAccountAddress])
    } else if (payload.method == "eth_coinbase" && currentAccountAddress){
        return web3Response(payload, currentAccountAddress)
    } else if (payload.method == "net_version"){
        return web3Response(payload, networkId)
    } else if (payload.method == "eth_uninstallFilter"){
        return web3Response(payload, true);
    } else {
        return null;
    }
}

var StatusAPI = function () {};

StatusAPI.prototype.getContactCode = function () {
    return sendAPIrequest('contact-code');
};

var ReadOnlyProvider = function () {};

ReadOnlyProvider.prototype.isStatus = true;
ReadOnlyProvider.prototype.status = new StatusAPI();
ReadOnlyProvider.prototype.isConnected = function () { return true; };

ReadOnlyProvider.prototype.enable = function () {
    return sendAPIrequest('web3');
};

ReadOnlyProvider.prototype.scanQRCode = function (regex) {
    return sendAPIrequest('qr-code', {regex: regex});
};

ReadOnlyProvider.prototype.send = function (payload) {
    if (payload.method == "eth_uninstallFilter"){
        this.sendAsync(payload, function (res, err) {})
    }
    var syncResponse = getSyncResponse(payload);
    if (syncResponse){
        return syncResponse;
    } else {
        return web3Response(payload, null);
    }
};

function sendAsync (payload, callback) {
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
               bridgeSend({type:      'web3-send-async-read-only',
                           messageId: messageId,
                           payload:   payload[i]});
           }
       }
       else
       {
           callbacks[messageId] = {callback: callback};
           bridgeSend({type:      'web3-send-async-read-only',
                       messageId: messageId,
                       payload:   payload});
       }

   }
};

ReadOnlyProvider.prototype.sendAsync = sendAsync;

var ReadOnlyBetaProvider = function () {};

ReadOnlyBetaProvider.prototype.isStatus = true;
ReadOnlyBetaProvider.prototype.status = new StatusAPI();

//web3.js Backwards Compatibility
ReadOnlyBetaProvider.prototype.isConnected = function () { return true; };
ReadOnlyBetaProvider.prototype.sendAsync = sendAsync;

ReadOnlyBetaProvider.prototype.scanQRCode = function (regex) {
    return sendAPIrequest('qr-code', {regex: regex});
};

ReadOnlyBetaProvider.prototype.send = function (method, params = []) {

    if (!method || typeof method !== 'string') {
      return new Error('Method is not a valid string.');
    }

    if (!(params instanceof Array)) {
      return new Error('Params is not a valid array.');
    }

    if (method == 'eth_requestAccounts'){
        return sendAPIrequest('web3');
    }

    var messageId = callbackId++;
    var payload = {id:      messageId,
                   jsonrpc: "2.0",
                   method:  method,
                   params:  params};

    bridgeSend({type:      'web3-send-async-read-only',
                messageId: messageId,
                payload:   payload});

    return new Promise(function (resolve, reject) {
                           callbacks[messageId] = {beta:    true,
                                                   resolve: resolve,
                                                   reject:  reject};
                       });
};
}

console.log("ReadOnlyProvider");
ethereum = new ReadOnlyProvider();
console.log("ReadOnlyBetaProvider");
ethereumBeta = new ReadOnlyBetaProvider();
