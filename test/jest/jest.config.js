module.exports = {
    preset: 'react-native',
    setupFilesAfterEnv: [
        '@testing-library/jest-native/extend-expect',
        '../test/jest/jestSetup.js',
    ],
    setupFiles: [],
    testPathIgnorePatterns: [],
    moduleNameMapper: {
        '^[@./a-zA-Z0-9$_-]+\\.(png|jpg|jpeg|gif)$':
            '<rootDir>/../node_modules/react-native/Libraries/Image/RelativeImageStub',
    },
    testTimeout: 60000,
    transformIgnorePatterns: [
        '/node_modules/(?!(@react-native|react-native-haptic-feedback|react-native-redash|react-native-image-crop-picker|@react-native-community|react-native-linear-gradient|react-native-background-timer|react-native|rn-emoji-keyboard|react-native-languages|react-native-shake|react-native-reanimated|react-native-redash|react-native-permissions|@react-native-community/blur|react-native-static-safe-area-insets|react-native-webview)/).*/',
    ],
    globals: {
        __TEST__: true,
    },
    testEnvironment: 'node',
    rootDir: '../../component-spec',
    testMatch: ['**/*__tests__*', '**/*.component_spec.js'],
};
