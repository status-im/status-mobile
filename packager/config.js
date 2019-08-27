const modulePaths = require('./modulePaths');
const resolve = require('path').resolve;
const fs = require('fs');
const { getDefaultConfig } = require("metro-config");

// Update the following line if the root folder of your app is somewhere else.
const ROOT_FOLDER = resolve(__dirname, '..');

module.exports = (async () => {
    const {
        resolver: { sourceExts, assetExts }
    } = await getDefaultConfig();
    return {
        transformer: {
            getTransformOptions: async () => {
                const moduleMap = {};
                modulePaths.forEach(path => {
                    if (fs.existsSync(path)) {
                        moduleMap[resolve(path)] = true;
                    }
                });
                return {
                    preloadedModules: moduleMap,
                    transform: { inlineRequires: { blacklist: moduleMap } },
                }
            },
        },
        projectRoot:ROOT_FOLDER,
        resolver: {
            assetExts: assetExts.filter(ext => ext !== "svg"),
            sourceExts: [...sourceExts, "svg"]
        }
    };
})();
