/*
 * Originally taken from https://github.com/decker405/figwheel-react-native
 *
 * @providesModule figwheel-bridge
 */

var CLOSURE_UNCOMPILED_DEFINES = null;

var config = {
    basePath: "target/",
    googBasePath: 'goog/',
    serverPort: 8081
};

var React = require('react-native');
var self;
var scriptQueue = [];
var serverHost = null; // will be set dynamically
var fileBasePath = null; // will be set dynamically
var evaluate = eval; // This is needed, direct calls to eval does not work (RN packager???)
var externalModules = {};
var evalListeners = []; // functions to be called when a script is evaluated

var figwheelApp = function (platform, devHost) {
    return React.createClass({
        getInitialState: function () {
            return {loaded: false}
        },
        render: function () {
            if (!this.state.loaded) {
                var plainStyle = {flex: 1, alignItems: 'center', justifyContent: 'center'};
                return (
                    <React.View style={plainStyle}>
                        <React.Text>Waiting for Figwheel to load files.</React.Text>
                    </React.View>
                );
            }
            return this.state.root;
        },
        componentDidMount: function () {
            var app = this;
            if (typeof goog === "undefined") {
                loadApp(platform, devHost, function(appRoot) {
                    app.setState({root: appRoot, loaded: true})
                });
            }
        }
    })
};

// evaluates js code ensuring proper ordering
function customEval(url, javascript, success, error) {
    if (scriptQueue.length > 0) {
        if (scriptQueue[0] === url) {
            try {
                evaluate(javascript);
                console.info('Evaluated: ' + url);
                scriptQueue.shift();
                evalListeners.forEach(function (listener) {
                    listener(url)
                });
                success();
            } catch (e) {
                console.error('Evaluation error in: ' + url);
                console.error(e);
                error();
            }
        } else {
            setTimeout(function () {
                customEval(url, javascript, success, error)
            }, 5);
        }
    } else {
        console.error('Something bad happened...');
        error()
    }
}

var isChrome = function () {
    return typeof importScripts === "function"
};

function asyncImportScripts(url, success, error) {
    console.info('(asyncImportScripts) Importing: ' + url);
    scriptQueue.push(url);
    fetch(url)
        .then(function (response) {
            return response.text()
        })
        .then(function (responseText) {
            return customEval(url, responseText, success, error);
        })
        .catch(function (error) {
            console.error('Error loading script, please check your config setup.');
            console.error(error);
            return error();
        });
}

function syncImportScripts(url, success, error) {
    try {
        importScripts(url);
        console.info('Evaluated: ' + url);
        evalListeners.forEach(function (listener) {
            listener(url)
        });
        success();
    } catch (e) {
        error()
    }
}

// Loads js file sync if possible or async.
function importJs(src, success, error) {
    if (typeof success !== 'function') {
        success = function () {
        };
    }
    if (typeof error !== 'function') {
        error = function () {
        };
    }

    var file = fileBasePath + '/' + src;

    console.info('(importJs) Importing: ' + file);
    if (isChrome()) {
        syncImportScripts(serverBaseUrl("localhost") + '/' + file, success, error);
    } else {
        asyncImportScripts(serverBaseUrl(serverHost) + '/' + file, success, error);
    }
}

function interceptRequire() {
    var oldRequire = window.require;
    console.info("Shimming require");
    window.require = function (id) {
        console.info("Requiring: " + id);
        if (externalModules[id]) {
            return externalModules[id];
        }
        return oldRequire(id);
    };
}

// do not show debug messages in yellow box
function debugToLog() {
    console.debug = console.log;
}

function serverBaseUrl(host) {
    return "http://" + host + ":" + config.serverPort
}

function loadApp(platform, devHost, onLoadCb) {
    serverHost = devHost;
    fileBasePath = config.basePath + platform;

    evalListeners.push(function (url) {
        if (url.indexOf('jsloader') > -1) {
            shimJsLoader();
        }
    });

    // callback when app is ready to get the reloadable component
    var mainJs = '/env/' + platform + '/main.js';
    evalListeners.push(function (url) {
        if (url.indexOf(mainJs) > -1) {
            onLoadCb(env[platform].main.root_el);
            console.log('Done loading Clojure app');
        }
    });

    if (typeof goog === "undefined") {
        console.log('Loading Closure base.');
        interceptRequire();
        importJs('goog/base.js', function () {
            shimBaseGoog();
            fakeLocalStorageAndDocument();
            importJs('cljs_deps.js');
            importJs('goog/deps.js', function () {
                debugToLog();
                // This is needed because of RN packager
                // seriously React packager? why.
                var googreq = goog.require;

                googreq('figwheel.connect');
            });
        });
    }
}

function startApp(appName, platform, devHost) {
    React.AppRegistry.registerComponent(
        appName, () => figwheelApp(platform, devHost));
}

function withModules(moduleById) {
    externalModules = moduleById;
    return self;
}

// Goog fixes
function shimBaseGoog() {
    console.info('Shimming goog functions.');
    goog.basePath = 'goog/';
    goog.writeScriptSrcNode = importJs;
    goog.writeScriptTag_ = function (src, optSourceText) {
        importJs(src);
        return true;
    };
    goog.inHtmlDocument_ = function () {
        return true;
    };
}

function fakeLocalStorageAndDocument() {
    window.localStorage = {};
    window.localStorage.getItem = function () {
        return 'true';
    };
    window.localStorage.setItem = function () {
    };

    window.document = {};
    window.document.body = {};
    window.document.body.dispatchEvent = function () {
    };
    window.document.createElement = function () {
    };

    if (typeof window.location === 'undefined') {
        window.location = {};
    }
    console.debug = console.warn;
    window.addEventListener = function () {
    };
    // make figwheel think that heads-up-display divs are there
    window.document.querySelector = function (selector) {
        return {};
    };
    window.document.getElementById = function (id) {
        return {style:{}};
    };
}

// Figwheel fixes
// Used by figwheel - uses importScript to load JS rather than <script>'s
function shimJsLoader() {
    console.info('==== Shimming jsloader ====');
    goog.net.jsloader.load = function (uri, options) {
        var deferred = {
            callbacks: [],
            errbacks: [],
            addCallback: function (cb) {
                deferred.callbacks.push(cb);
            },
            addErrback: function (cb) {
                deferred.errbacks.push(cb);
            },
            callAllCallbacks: function () {
                while (deferred.callbacks.length > 0) {
                    deferred.callbacks.shift()();
                }
            },
            callAllErrbacks: function () {
                while (deferred.errbacks.length > 0) {
                    deferred.errbacks.shift()();
                }
            }
        };

        // Figwheel needs this to be an async call,
        //    so that it can add callbacks to deferred
        setTimeout(function () {
            importJs(uri.getPath(),
                deferred.callAllCallbacks,
                deferred.callAllErrbacks);
        }, 1);

        return deferred;
    };
}

self = {
    withModules: withModules,
    start: startApp
};

module.exports = self;