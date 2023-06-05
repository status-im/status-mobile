import {
    useAnimatedStyle,
    useAnimatedSensor,
    withTiming,
    interpolate
} from 'react-native-reanimated';

const OFFSET = 30;
const PI = Math.PI;
const HALF_PI = PI / 2;

export function sensorAnimatedImage(order) {
    'worklet'
    const sensor = useAnimatedSensor(5, { interval: 100 })
    return useAnimatedStyle(function () {

        const { pitch, roll } = sensor.sensor.value;

        const top = withTiming(
            interpolate(
                pitch,
                [-HALF_PI, HALF_PI],
                [-OFFSET / order - OFFSET, OFFSET / order - OFFSET]
            ),
            { duration: 10 }
        );
        const left = withTiming(
            interpolate(
                roll,
                [-PI, PI],
                [(-OFFSET * 2) / order - OFFSET, (OFFSET * 2) / order - OFFSET]
            ),
            { duration: 10 }
        );
        const right = withTiming(
            interpolate(
                roll,
                [-PI, PI],
                [(OFFSET * 2) / order - OFFSET, (-OFFSET * 2) / order - OFFSET]
            ),
            { duration: 10 }
        );
        const bottom = withTiming(
            interpolate(
                pitch,
                [-HALF_PI, HALF_PI],
                [OFFSET / order - OFFSET, -OFFSET / order - OFFSET]
            ),
            { duration: 10 }
        );
        return {
            top,
            left,
            right,
            bottom,
        }
    })
}
