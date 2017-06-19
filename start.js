const spawn = require('child_process').spawn;
var proc = spawn("statusd", ["wnode", "--http", "--httpport", "8645"]);
