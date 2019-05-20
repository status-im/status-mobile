const {getDefaultConfig} = require("metro-config");
const modulePaths = require('packager/modulePaths');

module.exports = (async () => {
    const {
        resolver: {sourceExts, assetExts}
    } = await getDefaultConfig();
    return {
        transformer: {
            babelTransformerPath: require.resolve("react-native-svg-transformer"),
            getTransformOptions: () => {
                const moduleMap = {};
                modulePaths.forEach(path => {
                    if (fs.existsSync(path)) {
                        moduleMap[resolve(path)] = true;
                    }
                });
                return {
                    preloadedModules: moduleMap,
                    transform: {
                        experimentalImportSupport: false,
                        inlineRequires: {blacklist: moduleMap},
                    },
                }
            },
        },
        resolver: {
            assetExts: assetExts.filter(ext => ext !== "svg"),
            sourceExts: [...sourceExts, "svg"]
        }
    };
})();
