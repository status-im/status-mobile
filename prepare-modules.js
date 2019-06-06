var fs = require("fs");

var modules = [
    "i18n",
    "network"
];

modules.forEach(
    function (moduleName) {
        fs.readFile(`status-modules/cljs/${moduleName}-raw.js`, "utf8", function (err, data) {
            if (err) throw err;
            fs.writeFile(`status-modules/cljs/${moduleName}.js`,
                ("module.exports=`" + data.replace(/[\\$'"]/g, "\\$&") + "`;"),
                function (err) {
                    if (err) {
                        return console.log(err);
                    }
                });
        });
    });

