import "./app/index.js";

global.signalFunction = function(signal){

setTimeout(global.signalFunctionCLJS, 100, signal);

};