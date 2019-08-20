#!/usr/bin/env node

/**
 * Copyright (C) 2016, Canonical Ltd.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 *
 */

 console.debug = console.log;


var net = require('net');
var repl = require('repl');
var vm = require('vm');
var util = require('util');
var Buffer = require('buffer').Buffer;

var DEBUG = 1;

function rnUbuntuServer(readable, writable) {
  console.reportErrorsAsExceptions = false;  // XXX:
  var sandbox = { console: console, util: util };
  vm.createContext(sandbox);

  var state = 'start';
  var length = 0;
  var buffer = new Buffer(0);

  var internalEval = function(code) {
    DEBUG > 3 && console.error("-- internalEval: executing script(length=" + code.length + "): " + code.slice(0, 80) + " ... " + code.slice(-80));
    DEBUG > 3 && console.error("-- before sandbox=" + util.inspect(sandbox, { colors: true, depth: null }));
    var result = vm.runInContext(code, sandbox);
    DEBUG > 3 && console.error("-- internalEval: result = " + result);
    DEBUG > 3 && console.error("-- after sandbox=" + util.inspect(sandbox, { colors: true, depth: null }));
    return result;
  };

  var sendResponse = function(result) {
    function sendResponsePacket(response) {
      const sizeBuf = new Buffer(4);
      const dataBuf = new Buffer(response);
      sizeBuf.writeUInt32LE(dataBuf.length, 0);
      writable.write(sizeBuf);
      writable.write(dataBuf);
    }

    var stringifiedResult = JSON.stringify(result);
    DEBUG > 3 && console.error("-- sending result=" + stringifiedResult);
    if (stringifiedResult === undefined) {
      sendResponsePacket('undefined');
      return;
    }
    sendResponsePacket(stringifiedResult);
  }

  readable.on('error', function (exc) {
    console.warn("ignoring exception: " + exc);
  });

  readable.on('data', function(chunk) {
    DEBUG > 2 && console.error("-- Data received from RN Client: state = " + state)
    DEBUG > 2 && console.error("-- chunk length: " + chunk.length)
    DEBUG > 2 && console.error("-- buffer length(original): " + buffer.length)

    if (chunk == null || state === 'eof')
      return;

    buffer = Buffer.concat([buffer, chunk]);
    DEBUG > 2 && console.error("-- buffer length(concat): " + buffer.length)

    while(true) {
      if (state === 'start') {
        if (buffer.length < 4)
          return;
        length = buffer.readUInt32LE(0);
        DEBUG > 2 && console.error("-- New Packet: length=" + length);

        if (buffer.length >= length + 4) {
          var result = internalEval(buffer.toString('utf8', 4, length + 4));
          var tmpBuffer = new Buffer(buffer.length - 4 - length);
          buffer.copy(tmpBuffer, 0, length + 4, buffer.length);
          buffer = tmpBuffer;
          sendResponse(result);
        } else {
          state = 'script';
        }
      }

      if (state === 'script') {
        DEBUG > 2 && console.error("-- Packet length: " + length);
        if (buffer.length >= length + 4) {
          var result = internalEval(buffer.toString('utf8', 4, length + 4));
          var tmpBuffer = new Buffer(buffer.length - 4 - length);
          buffer.copy(tmpBuffer, 0, length + 4, buffer.length);
          buffer = tmpBuffer;
          state = 'start';
          sendResponse(result);
        } else {
          return;
        }
      }
    }
  });

  readable.on('end', function() {
    state = 'eof';
    DEBUG && console.error("-- Session ended");
  });
}

var closeDangerousConnection = function(sock) {
  var remoteAddress = sock.remoteAddress;
  if(remoteAddress.indexOf("127.0.0.1") == -1) {
    console.log("WARN: connection not from localhost, will be closed: ", remoteAddress);
    sock.destroy();
    return true;
  } else {
    console.log("Connection from: ", remoteAddress);
    return false;
  }
}

if (process.argv.indexOf('--pipe') != -1) {
  console.log = console.error
  rnUbuntuServer(process.stdin, process.stdout);
} else {
  var port = process.env['REACT_SERVER_PORT'] || 5000;
  process.argv.forEach((val, index) => {
    if (val == '--port') {
      port = process.argv[++index];
    }
  });

  var server = net.createServer((sock) => {
    DEBUG && console.error("-- Connection from RN client");
    if(!closeDangerousConnection(sock))
      rnUbuntuServer(sock, sock);
  }).listen(port, function() { console.error("-- Server starting") });
}
