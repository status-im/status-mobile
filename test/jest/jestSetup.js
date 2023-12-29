const WebSocket = require('ws');
const { NativeModules } = require('react-native');

require('@react-native-async-storage/async-storage/jest/async-storage-mock');
require('react-native-gesture-handler/jestSetup');
require('react-native-reanimated/src/reanimated2/jestUtils').setUpTests();

jest.mock('@react-native-async-storage/async-storage', () => mockAsyncStorage);

jest.mock('react-native-fs', () => ({
  default: {},
}));

jest.mock('react-native-navigation', () => ({
  getNavigationConstants: () => ({ constants: [] }),
  Navigation: {
    constants: async () => ({
      statusBarHeight: 10,
      topBarHeight: 10,
      bottomTabsHeight: 10,
    }),
  },
}));

jest.mock('react-native-background-timer', () => ({}));

jest.mock('react-native-languages', () => ({
  RNLanguages: {
    language: 'en',
    languages: ['en'],
  },
  default: {
    language: 'en',
    locale: 'en',
  },
}));

jest.mock('react-native-static-safe-area-insets', () => ({
  default: {
    safeAreaInsetsTop: 0,
    safeAreaInsetsBottom: 0,
  },
}));

jest.mock('react-native-permissions', () => require('react-native-permissions/mock'));

jest.mock('@react-native-community/audio-toolkit', () => ({
  Recorder: jest.fn().mockImplementation(() => ({
    prepare: jest.fn(),
    record: jest.fn(),
    toggleRecord: jest.fn(),
    pause: jest.fn(),
    stop: jest.fn(),
    on: jest.fn(),
  })),
  Player: jest.fn().mockImplementation(() => ({
    prepare: jest.fn(),
    playPause: jest.fn(),
    play: jest.fn(),
    pause: jest.fn(),
    stop: jest.fn(),
    seek: jest.fn(),
    on: jest.fn(),
  })),
  MediaStates: {
    DESTROYED: -2,
    ERROR: -1,
    IDLE: 0,
    PREPARING: 1,
    PREPARED: 2,
    SEEKING: 3,
    PLAYING: 4,
    RECORDING: 4,
    PAUSED: 5,
  },
  PlaybackCategories: {
    Playback: 1,
    Ambient: 2,
    SoloAmbient: 3,
  },
}));

jest.mock('i18n-js', () => ({
  ...jest.requireActual('i18n-js'),
  t: (label) => `tx:${label}`,
}));

jest.mock('react-native-blob-util', () => ({
  default: {
    config: jest.fn().mockReturnValue({
      fetch: jest.fn(),
    }),
  },
}));

jest.mock('react-native-reanimated', () => require('react-native-reanimated/mock'));

jest.mock('react-native-static-safe-area-insets', () => ({
  default: {
    safeAreaInsetsTop: 0,
    safeAreaInsetsBottom: 0,
  },
}));

NativeModules.ReactLocalization = {
  language: 'en',
  locale: 'en',
};
global.navigator = {
  userAgent: 'node',
};

global.WebSocket = WebSocket;
