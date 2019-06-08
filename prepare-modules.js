var fs = require("fs");
var path = require('path');
var dirs = ["status-modules/cljs", "status-modules/resources"];

dirs.forEach(dir => {
    fs.readdir(dir, (err, files) => {
        if (files) {
            files.forEach(file => {
                if (file.endsWith("-raw.js")) {
                    const filePath = path.resolve(dir, file);
                    fs.readFile(filePath, "utf8", function (err, data) {
                        if (err) throw err;
                        fs.writeFile(filePath.replace("-raw.js", ".js"),
                            ("module.exports=`" + data.replace(/[\\$'"]/g, "\\$&") + "`;"),
                            function (err) {
                                if (err) {
                                    return console.log(err);
                                }
                            });
                    });
                }
            });
        }
    });
});
