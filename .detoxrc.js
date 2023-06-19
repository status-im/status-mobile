module.exports = {
  testRunner: 'jest',
  testRegex: '\\.visual\\.js$',
  'runner-config': 'visual-test/config.json',
  devices: {
    simulator: {
      type: 'ios.simulator',
      device: {
        type: 'iPhone 11 Pro',
      },
    },
  },
  apps: {
    'ios.release': {
      name: 'StatusIm',
      type: 'ios.app',
      binaryPath: 'ios/build/Build/Products/Release-iphonesimulator/StatusIm.app',
      build: 'make release-ios',
    },
    'ios.debug': {
      name: 'StatusIm',
      type: 'ios.app',
      binaryPath: process.env.TEST_BINARY_PATH,
      build: "make run-ios SIMULATOR='iPhone 11 Pro'",
    },
  },
  configurations: {
    'ios.sim.release': {
      device: 'simulator',
      app: 'ios.release',
    },
    'ios.sim.debug': {
      device: 'simulator',
      app: 'ios.debug',
    },
  },
};
