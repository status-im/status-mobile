const { getDefaultConfig, mergeConfig } = require('@react-native/metro-config');
/**
 * Metro configuration
 * https://reactnative.dev/docs/metro
 *
 * @type {import('metro-config').MetroConfig}
 */
const config = {
  transformer: {
    getTransformOptions: async () => ({
      transform: {
        experimentalImportSupport: false,
        inlineRequires: true,
      },
    }),
  },
  resolver: {
    resolverMainFields: ['react-native', 'browser', 'main'],
    nodeModulesPaths: ['./node_modules'],
    extraNodeModules: require('node-libs-react-native'),
    unstable_enableSymlinks: true,
  },
};
module.exports = mergeConfig(getDefaultConfig(__dirname), config);
