import {
  useDerivedValue,
  interpolate,
  interpolateColor,
  Extrapolation,
  useAnimatedStyle,
} from 'react-native-reanimated';

const CLAMP_MIN = 0;
const CLAMP_MAX = 60;
const BLUR_MIN = 1;
const BLUR_MAX = 15;

export const useBlurAmount = (sharedValue) =>
  useDerivedValue(() =>
    parseInt(interpolate(sharedValue.value, [CLAMP_MIN, CLAMP_MAX], [BLUR_MIN, BLUR_MAX], Extrapolation.CLAMP)),
  );

export function useLayerOpacity(sharedValue, from, to) {
  return useAnimatedStyle(() => ({
    flex: 1,
    justifyContent: 'flex-end',
    backgroundColor: interpolateColor(sharedValue.value, [0, 60], [from, to], 'RGB'),
  }));
}
