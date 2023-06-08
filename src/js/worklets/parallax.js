import {
    useAnimatedStyle,
    useAnimatedSensor,
    withTiming,
    interpolate,
    SensorType
} from 'react-native-reanimated';
import { Platform } from 'react-native';

const PI = Math.PI;

export function sensorAnimatedImage(zIndex, offset, stretch) {
    'worklet'
    const sensor = useAnimatedSensor(SensorType.ROTATION, { interval: 30 })
    return useAnimatedStyle(function () {

        const { qx, qz, qw, qy, pitch } = sensor.sensor.value;

        const roll = Math.asin(-2.0 * (qx * qz - qw * qy))

        const top = withTiming(
            interpolate(
                pitch,
                Platform.OS === 'ios' ? [-PI, PI] : [PI, -PI],
                [(-offset * 3) / zIndex - offset + (offset - stretch), (offset * 3) / zIndex - offset + (offset - stretch)]),
            { duration: 10 }
        );
        const left = withTiming(
            interpolate(
                roll,
                [1, -1],
                [(-offset * 2) / zIndex - offset + (offset - stretch), (offset * 2) / zIndex - offset + (offset - stretch)]),
            { duration: 10 }
        );

        return {
            transform: [{ translateX: left },
            { translateY: top }]
        }
    })
}
