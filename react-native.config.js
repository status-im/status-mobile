const rndi = process.env.GOOGLE_FREE == 1 ? {platforms: {android: null}} : {};

module.exports = {
    dependencies: {
        'react-native-dialogs': {
            platforms: {
                android: null,
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
        'react-native-device-info': rndi,
    },
};
