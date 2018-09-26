if(typeof ReadOnlyProvider === "undefined"){
var callbackId = 0;
var callbacks = {};
var ethereumPromise = {};

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

    if (data.type === "status-api-success")
    {
        if (data.keys == 'WEB3')
        {
            ethereumPromise.allowed = true;
            window.currentAccountAddress = data.data["WEB3"];
            ethereumPromise.resolve();
        }
        else
        {
            window.dispatchEvent(new CustomEvent('statusapi', { detail: { permissions: data.keys,
                                                                          data:        data.data
                                                                        } }));
        }
    }

    else if (data.type === "web3-permission-request-denied")
    {
        ethereumPromise.reject(new Error("Denied"));
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
};

function web3Response (payload, result){
    return {id: payload.id,
            jsonrpc: "2.0",
            result: result};
}

function getSyncResponse (payload) {
    console.log("getSyncResponse " + payload.method + " !")
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

var ReadOnlyProvider = function () {};

ReadOnlyProvider.prototype.isStatus = true;
ReadOnlyProvider.prototype.isConnected = function () { return true; };

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

ReadOnlyProvider.prototype.enable = function () {
    bridgeSend({
        type: 'status-api-request',
        permissions: ['WEB3'],
        host: window.location.hostname
    });
    return new Promise(function (resolve, reject) {
                        ethereumPromise.resolve = resolve;
                        ethereumPromise.reject = reject;
                        });
};

ReadOnlyProvider.prototype.sendAsync = function (payload, callback) {

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
                           payload:   payload[i],
                           host:      window.location.hostname});
           }
       }
       else
       {
           callbacks[messageId] = {callback: callback};
           bridgeSend({type:      'web3-send-async-read-only',
                       messageId: messageId,
                       payload:   payload,
                       host:      window.location.hostname});
       }

   }
};

}

console.log("ReadOnlyProvider");
ethereum = new ReadOnlyProvider();