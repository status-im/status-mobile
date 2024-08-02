module.exports = {
  project: {
    android: {
      // https://github.com/reactwg/react-native-new-architecture/discussions/135
      unstable_reactLegacyComponentNames: ['BVLinearGradient', 'CKCameraManager', 'FastImageView'],
    },
  },
  dependencies: {
    'react-native-config': {
      platforms: {
        ios: null,
      },
    },
    'react-native-image-resizer': {
      platforms: {
        ios: null,
      },
    },
    'react-native-status-keycard': {
      platforms: {
        android: null,
        ios: null,
      },
    },
    '@react-native-community/blur': {
      platforms: {
        android: null,
      },
    },
  },
};
