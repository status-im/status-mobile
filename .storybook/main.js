const path = require('path');

const config = {
  stories: ['../storybook-target/*_story.js'],
  addons: [
    '@storybook/addon-links',
    '@storybook/addon-essentials',
    '@storybook/addon-interactions',
    '@storybook/addon-react-native-web',
  ],
  framework: {
    name: '@storybook/react-webpack5',
    options: {},
  },
  staticDirs: ['../resources'],
  features: {
    interactionsDebugger: true,
    storyStoreV7: false,
  },
};
export default config;
