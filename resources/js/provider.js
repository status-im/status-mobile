if(typeof EthereumProvider === "undefined"){
var callbackId = 0;
var callbacks = {};

bridgeSend = function (data) {
    ReactNativeWebView.postMessage(JSON.stringify(data));
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

function Unauthorized() {
  this.name = "Unauthorized";
  this.id = 4100;
  this.message = "The requested method and/or account has not been authorized by the user.";
}
Unauthorized.prototype = Object.create(Error.prototype);

function UserRejectedRequest() {
  this.name = "UserRejectedRequest";
  this.id = 4001;
  this.message = "The user rejected the request.";
}
UserRejectedRequest.prototype = Object.create(Error.prototype);

ReactNativeWebView.onMessage = function (message)
{
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
                callback.reject(new UserRejectedRequest());
            }
        }
        else if (data.type === "web3-send-async-callback")
        {
            if (callback.beta)
            {
                if (data.error)
                {
                    if (data.error.code == 4100)
                        callback.reject(new Unauthorized());
                    else
                        //TODO probably if rpc returns empty result we need to call resolve with empty data?
                        callback.reject(data.error);
                }
                else{
                // TODO : according to https://github.com/ethereum/EIPs/blob/master/EIPS/eip-1193.md#examples
                // TODO : we need to return data.result.result here, but for some reason some dapps (uniswap)
                // TODO : expects jsonrpc
                    callback.resolve(data.result);
                }
            }
            else if (callback.results)
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
};

function web3Response (payload, result){
    return {id: payload.id,
            jsonrpc: "2.0",
            result: result};
}

function getSyncResponse (payload) {
    if (payload.method == "eth_accounts" && (typeof currentAccountAddress !== "undefined")) {
        return web3Response(payload, [currentAccountAddress])
    } else if (payload.method == "eth_coinbase" && (typeof currentAccountAddress !== "undefined")) {
        return web3Response(payload, currentAccountAddress)
    } else if (payload.method == "net_version" || payload.method == "eth_chainId"){
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

var EthereumProvider = function () {};

EthereumProvider.prototype.isStatus = true;
EthereumProvider.prototype.status = new StatusAPI();
EthereumProvider.prototype.isConnected = function () { return true; };

EthereumProvider.prototype.enable = function () {
    return sendAPIrequest('web3');
};

EthereumProvider.prototype.scanQRCode = function (regex) {
    return sendAPIrequest('qr-code', {regex: regex});
};

//Support for legacy send method
EthereumProvider.prototype.sendSync = function (payload)
{
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

EthereumProvider.prototype.send = function (method, params = [])
{
    if (!method) {
      return new Error('Request is not valid.');
    }

    if (!(params instanceof Array)) {
      return new Error('Params is not a valid array.');
    }

    //Support for legacy send method
    if (typeof method !== 'string') {
      return this.sendSync(method);
    }

    if (method == 'eth_requestAccounts'){
        return sendAPIrequest('web3');
    }

    var syncResponse = getSyncResponse({method: method});
    if (syncResponse){
        return new Promise(function (resolve, reject) {
                                   resolve(syncResponse);
                               });
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

//Support for legacy sendAsync method
EthereumProvider.prototype.sendAsync = function (payload, callback)
{
  var syncResponse = getSyncResponse(payload);
  if (syncResponse && callback) {
      callback(null, syncResponse);
  }
  else
  {
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
}

ethereum = new EthereumProvider();
