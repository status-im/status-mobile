import { useDerivedValue, withTiming, Easing } from 'react-native-reanimated';

const slideAnimationDuration = 300;

const easeOut = {
  duration: slideAnimationDuration,
  easing: Easing.bezier(0, 0, 0.58, 1),
};

// Derived Values
export function dynamicProgressBarWidth(staticProgressBarWidth, progress) {
  return useDerivedValue(function () {
    'worklet';
    return (staticProgressBarWidth * (progress.value || 0)) / 100;
  });
}

export function carouselLeftPosition(windowWidth, progress) {
  return useDerivedValue(function () {
    'worklet';
    const progressValue = progress.value;
    switch (true) {
      case progressValue < 25:
        return 0;
      case progressValue === 25:
        return withTiming(-windowWidth, easeOut);
      case progressValue < 50:
        return -windowWidth;
      case progressValue === 50:
        return withTiming(-2 * windowWidth, easeOut);
      case progressValue < 75:
        return -2 * windowWidth;
      case progressValue === 75:
        return withTiming(-3 * windowWidth, easeOut);
      case progressValue < 100:
        return -3 * windowWidth;
      case progressValue === 100:
        return withTiming(-4 * windowWidth, easeOut);
      default:
        return 0;
    }
  });
}
