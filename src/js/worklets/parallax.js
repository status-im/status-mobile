import {
    useAnimatedStyle,
    useAnimatedSensor,
    withTiming,
    interpolate,
    SensorType
} from 'react-native-reanimated';

const PI = Math.PI;
const HALF_PI = PI / 2;

export function sensorAnimatedImage(zIndex, offset) {
    'worklet'
    const sensor = useAnimatedSensor(5, { interval: 10 })
    return useAnimatedStyle(function () {

        const { qx, qz, qw, qy, pitch } = sensor.sensor.value;

        const roll = Math.asin(-2.0 * (qx * qz - qw * qy))

        const top = withTiming(
            interpolate(
                pitch,
                [-PI, PI],
                [-offset / zIndex - offset, offset / zIndex - offset]),
            { duration: 10 }
        );
        const left = withTiming(
            interpolate(
                roll,
                [-1, 1],
                [(-offset * 2) / zIndex - offset, (offset * 2) / zIndex - offset]),
            { duration: 10 }
        );

        return {
            transform: [{ translateX: left },
            { translateY: top }]
        }
    })
}
