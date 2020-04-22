var path = require("path");
const defaultResolvePath = require('babel-plugin-module-resolver').resolvePath;
const generator = require("./node_modules/react-native-desktop-qt/babel/babel-config-generator.js");

module.exports = generator.create(path.resolve("./node_modules/react-native"), path.resolve("./node_modules/react-native-desktop-qt"));
