var Mocks = require('../target/mocks/mocks.js');
var Module = require('module');
process.env.TZ = 'Europe/Amsterdam'

const originalLoader = Module._load;

/*
  Hook `require` so that RN abuse of require does not break when running tests in nodejs.
*/

Module._load = function hookedLoader(request, parent, isMain) {
    if (request.match(/.jpeg|.jpg|.png$/)) {
        return { uri: request };
    }

    return originalLoader(request, parent, isMain);
};

var originalRequire = Module.prototype.require;

Module.prototype.require = function(req){
    module = Mocks.mocks(req);
    if (module == null) {
        return originalRequire.apply(this, arguments);
    }
    else {
        return module;
    }
};

