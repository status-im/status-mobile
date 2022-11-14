module.exports = {

    "env": {
        "development": {
            "presets": [
                "module:metro-react-native-babel-preset"
            ],
            "plugins": [
                "react-native-reanimated/plugin"
            ]
        },
        "production":{
            "presets": [
                "module:metro-react-native-babel-preset"
            ],
            "plugins": [
                "react-native-reanimated/plugin"
            ]
        },
        "test": {
            "presets": [
                "module:metro-react-native-babel-preset",
                '@babel/preset-react',
                [
                    '@babel/preset-env',
                    {
                        targets: {
                            node: '14',
                        },
                    },
                ],
            ],
            "plugins": [
                "react-native-reanimated/plugin"
            ],
        }
    }
}