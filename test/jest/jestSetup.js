const WebSocket = require('ws');
const { NativeModules } = require('react-native');

require('@react-native-async-storage/async-storage/jest/async-storage-mock');
require('react-native-gesture-handler/jestSetup');


jest.mock('react-native-reanimated', () => {
    const Reanimated = require('react-native-reanimated/mock');
    // The mock for `call` immediately calls the callback which is incorrect
    // So we override it with a no-op
    Reanimated.default.call = () => { };

    return Reanimated;
});

jest.mock('@react-native-async-storage/async-storage', () => mockAsyncStorage);

jest.mock('react-native-navigation', () => ({
    getNavigationConstants:
        () => ({ constants: [] }),
    Navigation: { constants: async () => { } }
}));

jest.mock("react-native-background-timer", () => ({}))

jest.mock('react-native-languages', () => ({
    RNLanguages: {
        language: 'en',
        languages: ['en'],
    },
    default: {
        language: 'en',
        locale: 'en'
    },
}));

NativeModules.ReactLocalization = {
    language: 'en',
    locale: 'en'
};
global.navigator = {
    userAgent: 'node',
}

global.WebSocket = WebSocket