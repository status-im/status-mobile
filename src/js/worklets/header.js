import { useDerivedValue, withTiming } from 'react-native-reanimated';

export function headerContentOpacity(scrollY, threshold) {
  return useDerivedValue(function () {
    'worklet';
    if (scrollY.value < threshold) {
      return withTiming(0);
    } else {
      return withTiming(1);
    }
  });
}

export function headerContentPosition(scrollY, threshold, topBarHeight) {
  return useDerivedValue(function () {
    'worklet';
    return scrollY.value < threshold ? withTiming(topBarHeight) : withTiming(0);
  });
}
