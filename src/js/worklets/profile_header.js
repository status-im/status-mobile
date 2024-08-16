import { useAnimatedStyle, withTiming } from 'react-native-reanimated';

export function profileHeaderAnimation(scrollY, threshold, topBarHeight) {
  return useAnimatedStyle(() => {
    'worklet';
    const opacity = scrollY.value < threshold ? withTiming(0) : withTiming(1);
    const translateY = scrollY.value < threshold ? withTiming(topBarHeight) : withTiming(0);

    return {
      opacity: opacity,
      transform: [{ translateY: translateY }],
    };
  });
}
