import {
    useAnimatedStyle,
    useAnimatedSensor,
    withTiming,
    interpolate,
    SensorType
} from 'react-native-reanimated';

const PI = Math.PI;

export function sensorAnimatedImage(zIndex, offset) {
    'worklet'
    const sensor = useAnimatedSensor(SensorType.ACCELEROMETER, { interval: 30 })
    return useAnimatedStyle(() => {
        let { x, y, z } = sensor.sensor.value;
        const pitch = Math.abs((Math.atan2(y, z) * 180) / PI) * -1;
        const roll = (Math.atan2(-x, Math.sqrt(y * y + z * z)) * 180) / PI;

        const top = withTiming(
            interpolate(pitch, Platform.OS === 'ios' ? [-180, 0] : [0, -180], [
                -offset / zIndex - offset,
                (offset * 2) / zIndex - offset,
            ]),
            { duration: 10 }
        );
        const left = withTiming(
            interpolate(roll, Platform.OS === 'ios' ? [90, -90] : [-90, 90], [
                (-offset * 2.5) / zIndex - offset,
                (offset * 2.5) / zIndex - offset,
            ]),
            { duration: 10 }
        );

        return {
            top,
            left,
        };
    })
}
