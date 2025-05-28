console.log('TESTING INJECTION');

(function () {
  // Internal storage for pending calls
  const _callbacks = {};
  let _nextId = 1;

  // Minimal EventEmitter for provider.on(...)
  const _listeners = {};

  // 1️⃣ Define our fake provider
  const fakeProvider = {
    // EIP-1193 request()
    request({ method, params }) {
      return new Promise((resolve, reject) => {
        const id = _nextId++;
        _callbacks[id] = { resolve, reject };
        // send to RN side
        window.ReactNativeWebView.postMessage(JSON.stringify({ topic: 'rpc', id, method, params }));
      });
    },

    // EIP-1193 event subscription
    on(eventName, handler) {
      _listeners[eventName] = _listeners[eventName] || [];
      _listeners[eventName].push(handler);
    },
  };

  // window.ReactNativeWebView.onMessage = function (ev) {
  //   console.log('MESSAGE INCOMING', ev);
  //   let msg;
  //   try {
  //     msg = JSON.parse(ev);
  //   } catch (e) {
  //     alert('ERROR parsing:', e, ev);
  //     return;
  //   }

  //   console.log('message', msg);
  //   // RPC response
  //   if (msg.type === 'rpcResponse' && _callbacks[msg.id]) {
  //     const { resolve, reject } = _callbacks[msg.id];

  //     console.log('resolving rcpResponse', msg.id, _callbacks[msg.id]);
  //     delete _callbacks[msg.id];
  //     msg.error ? reject(msg.error) : resolve(msg.result);
  //   }

  //   // Emitted event from RN
  //   else if (msg.type === 'emitEvent' && _listeners[msg.event]) {
  //     _listeners[msg.event].forEach((fn) => fn(msg.data));
  //   }
  // };

  //2️⃣ Listen for RN → WebView messages
  document.addEventListener('message', (ev) => {
    console.log('Message received from RN', ev.data);

    let msg;
    try {
      msg = JSON.parse(ev.data);
    } catch (e) {
      console.error('ERROR parsing:', e, ev);
      return;
    }

    // RPC response
    if (msg.type === 'rpcResponse' && _callbacks[msg.id]) {
      const { resolve, reject } = _callbacks[msg.id];
      delete _callbacks[msg.id];
      msg.error ? reject(msg.error) : resolve(msg.result);
    }

    // Emitted event from RN
    else if (msg.type === 'emitEvent' && _listeners[msg.event]) {
      _listeners[msg.event].forEach((fn) => fn(msg.data));
    }
  });

  // 3️⃣ Expose to the page
  window.__RN_WALLET_PROVIDER__ = fakeProvider;
  window.ethereum = fakeProvider; // if Dapp expects `window.ethereum`

  // 4️⃣ (Optional) announce via EIP-6963 discovery
  const info = {
    uuid: window.__WALLET_UUID__,
    name: window.__WALLET_NAME__,
    icon: window.__WALLET_ICON__,
    rdns: window.__WALLET_RDNS__,
  };
  const detail = { info, provider: fakeProvider };
  window.dispatchEvent(new CustomEvent('eip6963:announceProvider', { detail }));
  window.addEventListener('eip6963:requestProvider', () =>
    window.dispatchEvent(new CustomEvent('eip6963:announceProvider', { detail })),
  );
})();
