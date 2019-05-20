const modulePaths = require('./modulePaths');
const resolve = require('path').resolve;
const fs = require('fs');

// Update the following line if the root folder of your app is somewhere else.
const ROOT_FOLDER = resolve(__dirname, '..');

const config = {
    transformer: {
        getTransformOptions: () => {
            const moduleMap = {};
            /*modulePaths.forEach(path => {
                if (fs.existsSync(path)) {
                    moduleMap[resolve(path)] = true;
                }
            });*/
            return {
                preloadedModules: moduleMap,
                transform: { inlineRequires: { blacklist: moduleMap } },
            };
        },
    },
    projectRoot:ROOT_FOLDER,
};

module.exports = config;
