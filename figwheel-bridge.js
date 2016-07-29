/*
 * Originally taken from https://github.com/decker405/figwheel-react-native
 *
 * @providesModule figwheel-bridge
 */

var CLOSURE_UNCOMPILED_DEFINES = null;
var debugEnabled = false;

var config = {
    basePath: "target/",
    googBasePath: 'goog/',
    serverPort: 8081
};

var React = require('react');
var ReactNative = require('react-native');
var WebSocket = require('WebSocket');
var self;
var scriptQueue = [];
var serverHost = null; // will be set dynamically
var fileBasePath = null; // will be set dynamically
var evaluate = eval; // This is needed, direct calls to eval does not work (RN packager???)
var externalModules = {};
var evalListeners = [ // Functions to be called after each js file is loaded and evaluated
    function (url) {
        if (url.indexOf('jsloader') > -1) {
            shimJsLoader();
        }
    },
    function (url) {
        if (url.indexOf('/figwheel/client/socket') > -1) {
            setCorrectWebSocketImpl();
        }
    }];

var figwheelApp = function (platform, devHost) {
    return React.createClass({
        getInitialState: function () {
            return {loaded: false}
        },
        render: function () {
            if (!this.state.loaded) {
                var plainStyle = {flex: 1, alignItems: 'center', justifyContent: 'center'};
                return (
                    <ReactNative.View style={plainStyle}>
                        <ReactNative.Text>Waiting for Figwheel to load files.</ReactNative.Text>
                    </ReactNative.View>
                );
            }
            return this.state.root;
        },
        componentDidMount: function () {
            var app = this;
            if (typeof goog === "undefined") {
                loadApp(platform, devHost, function (appRoot) {
                    app.setState({root: appRoot, loaded: true})
                });
            }
        }
    })
};

function logDebug(msg) {
    if (debugEnabled) {
        console.log(msg);
    }
}

// evaluates js code ensuring proper ordering
function customEval(url, javascript, success, error) {
    if (scriptQueue.length > 0) {
        if (scriptQueue[0] === url) {
            try {
                evaluate(javascript);
                logDebug('Evaluated: ' + url);
                scriptQueue.shift();
                evalListeners.forEach(function (listener) {
                    listener(url)
                });
                success();
            } catch (e) {
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
    logDebug('(asyncImportScripts) Importing: ' + url);
    scriptQueue.push(url);
    fetch(url)
        .then(function (response) {
            return response.text()
        })
        .then(function (responseText) {
            return customEval(url, responseText, success, error);
        })
        .catch(function (error) {
            console.error(error);
            return error();
        });
}

function syncImportScripts(url, success, error) {
    try {
        importScripts(url);
        logDebug('Evaluated: ' + url);
        evalListeners.forEach(function (listener) {
            listener(url)
        });
        success();
    } catch (e) {
        console.error(e);
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

    logDebug('(importJs) Importing: ' + file);
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

function compileWarningsToYellowBox() {
    var log = window.console.log;
    var compileWarningRx = /Figwheel: Compile/;
    window.console.log = function (msg) {
        if (compileWarningRx.test(msg)) {
            console.warn(msg);
        } else {
            log.apply(window.console, arguments);
        }
    };
}

function serverBaseUrl(host) {
    return "http://" + host + ":" + config.serverPort
}

function setCorrectWebSocketImpl() {
    figwheel.client.socket.get_websocket_imp = function () {
        return WebSocket;
    };
}

function loadApp(platform, devHost, onLoadCb) {
    serverHost = devHost;
    fileBasePath = config.basePath + platform;

    // callback when app is ready to get the reloadable component
    var mainJs = '/env/' + platform + '/main.js';
    evalListeners.push(function (url) {
        if (url.indexOf(mainJs) > -1) {
            onLoadCb(env[platform].main.root_el);
            console.info('Done loading Clojure app');
        }
    });

    if (typeof goog === "undefined") {
        console.info('Loading Closure base.');
        interceptRequire();
        compileWarningsToYellowBox();
        importJs('goog/base.js', function () {
            shimBaseGoog();
            importJs('cljs_deps.js');
            importJs('goog/deps.js', function () {
                // This is needed because of RN packager
                // seriously React packager? why.
                var googreq = goog.require;

                googreq('figwheel.connect');
            });
        });
    }
}

function startApp(appName, platform, devHost) {
    ReactNative.AppRegistry.registerComponent(
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