#!/usr/bin/env node

var fs = require('fs-extra');
var child = require('child_process');
var platform = process.argv[2];
var deviceType = process.argv[3];
var rn = JSON.parse(fs.readFileSync('.re-natal'));
var fileName = "worker." + platform + ".js";

function resolveIosDevHost(deviceType) {
    if (deviceType === 'simulator') {
        return 'localhost';
    } else {
        return child
            .execSync('ipconfig getifaddr en0', {stdio: ['pipe', 'pipe', 'ignore']})
            .toString()
            .trim()
    }
}

function resolveAndroidDevHost(deviceType) {
    allowedTypes = {
        'real': 'localhost',
        'avd': '10.0.2.2',
        'genymotion': '10.0.3.2'
    };

    return allowedTypes[deviceType];
}

function resolveDevHost(platform, deviceType) {
    if(platform === 'android') {
        return resolveAndroidDevHost(deviceType);
    } else {
        return resolveIosDevHost(deviceType);
    }
}

function scanImageDir(dir) {
    fnames = fs.readdirSync(dir)
        .map(function (fname) {
            return dir + "/" + fname;
        })
        .filter(function (path) {
            return fs.statSync(path).isFile();
        })
        .filter(function (path) {
            return removeExcludeFiles(path);
        })
        .map(function (path) {
            return path.replace(/@2x|@3x/i, '');
        })
        .filter(function (v, idx, slf) {
            return slf.indexOf(v) == idx;
        });

    dirs = fs.readdirSync(dir)
        .map(function (fname) {
            return dir + "/" + fname;
        })
        .filter(function (path) {
            return fs.statSync(path).isDirectory();
        }).filter (function (path) {
            return path != dir;
        });

    return fnames.concat(scanImages(dirs));
}

function removeExcludeFiles(file) {
    excludedFileNames = [".DS_Store"];
    return excludedFileNames.reduce(function (res, ex) {
        return !(!res || file.indexOf(ex) !== -1);
    });
}

function scanImages(dirs) {
    imgs = [];
    if(dirs.length === 0) {
        return imgs;
    }

    for (i = 0; i <  dirs.length; i++) {
        imgs = imgs.concat(scanImageDir(dirs[i]));
    }

    return imgs;
}

function createIndexFile(platform, deviceType) {
    var index = "var modules={};\n";
    var modules = rn["worker-modules"];
    modules.push("react-native");
    modules.push("react");
    modules.push("create-react-class");
    images = scanImages(rn["imageDirs"])
        .map(function (img) {
           return "./" + img;
        });

    modules = modules.concat(images);
    for (var i = 0, len = modules.length; i < len; i++) {
        var module = modules[i];
        index += `modules['${module}']=require('${module}');\n`;
    }

    index += `var devHost = '${resolveDevHost(platform, deviceType)}';\n`;
    index += `require('worker-bridge').withModules(modules).loadApp(devHost, '${platform}');\n`;

    return index;
}

fs.writeFile(
    fileName,
    createIndexFile(platform, deviceType),
    function (err){
        if(err) {
            return console.log(err);
        }

        console.log(fileName + " created!");
    });


