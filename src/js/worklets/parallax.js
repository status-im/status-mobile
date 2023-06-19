import {
    useAnimatedStyle,
    useAnimatedSensor,
    withTiming,
    interpolate,
    SensorType
} from 'react-native-reanimated';
import { Platform } from 'react-native';

const PI = Math.PI;

/*
sensorAnimatedImage uses the pitch and roll of a device to move the content on screen in the respective direction. 

This approach mostly follows the example here:
https://github.com/notJust-dev/iOSLockScreen/blob/main/src/components/SensorAnimatedImage.js

which is explained in better detail in this video 
https://www.youtube.com/watch?v=iEBoZDHCN5Y&t=2205s
there is a bug with the pitch and roll calculations provided directly from the sensor data so the calculation had to
be done using the quaternions directly.
*/
export function sensorAnimatedImage(zIndex, offset, stretch) {
    'worklet'
    const rotationSensor = useAnimatedSensor(SensorType.ROTATION, { interval: 30 })
    return useAnimatedStyle(function () {

        const { qx, qz, qw, qy, pitch } = rotationSensor.sensor.value;

        const roll = Math.asin(-2.0 * (qx * qz - qw * qy))

        const translateY = withTiming(
            interpolate(
                pitch,
                Platform.OS === 'ios' ? [-PI, PI] : [PI, -PI],
                [(-offset * 3) / zIndex - offset + (offset - stretch), (offset * 3) / zIndex - offset + (offset - stretch)]),
            { duration: 10 }
        );
        const translateX = withTiming(
            interpolate(
                roll,
                [1, -1],
                [(-offset * 2) / zIndex - offset + (offset - stretch), (offset * 2) / zIndex - offset + (offset - stretch)]),
            { duration: 10 }
        );

        return {
            transform: [
                { translateX },
                { translateY }
            ]
        }
    })
}
