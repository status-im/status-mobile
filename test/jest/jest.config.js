const transformIgnorePatterns = () => {
  const libs = [
    '@react-native',
    '@react-native-community',
    '@react-native-community/blur',
    'react-native',
    'react-native-background-timer',
    'react-native-gifted-charts',
    'react-native-haptic-feedback',
    'react-native-hole-view',
    'react-native-image-crop-picker',
    'react-native-languages',
    'react-native-linear-gradient',
    'react-native-permissions',
    'react-native-reanimated',
    'react-native-redash',
    'react-native-redash',
    'react-native-shake',
    'react-native-static-safe-area-insets',
    'rn-emoji-keyboard',
  ].join('|');

  return [`/node_modules/(?!(${libs})/).*/`];
};

const shouldUseSilentReporter = () => {
  return process.env.JEST_USE_SILENT_REPORTER === 'true';
};

const reporters = () => {
  let reporters = [];
  if (shouldUseSilentReporter()) {
    reporters.push(['jest-silent-reporter', { useDots: true }]);
  } else {
    reporters.push('default');
  }
  return reporters;
};

module.exports = {
  preset: 'react-native',
  setupFilesAfterEnv: [
    '@testing-library/jest-native/extend-expect',
    '../component-spec/status_im.setup.schema_preload.js',
    '../component-spec/test_helpers.component_tests_preload.js',
    '../test/jest/jestSetup.js',
  ],
  reporters: reporters(),
  setupFiles: [],
  testPathIgnorePatterns: [],
  moduleNameMapper: {
    '^[@./a-zA-Z0-9$_-]+\\.(png|jpg|jpeg|gif)$':
      '<rootDir>/../node_modules/react-native/Libraries/Image/RelativeImageStub',
  },
  testTimeout: 60000,
  transformIgnorePatterns: transformIgnorePatterns(),
  // This is a workaround after upgrading to Jest 29.7.0, otherwise we get:
  //
  //   SyntaxError: node_modules/@react-native/js-polyfills/error-guard.js:
  //   Missing semicolon. (14:4)
  //
  transform: {
    '^.+\\.(js|jsx|ts|tsx)$': ['babel-jest', { configFile: './babel.config.js' }],
  },
  globals: {
    __TEST__: true,
  },
  testEnvironment: 'node',
  rootDir: '../../component-spec',
  testMatch: ['**/*__tests__*', '**/*.component_spec.js'],
};
