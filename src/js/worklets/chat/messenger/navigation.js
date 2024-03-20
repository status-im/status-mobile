import { useDerivedValue, withTiming, interpolate } from 'react-native-reanimated';

export function navigationHeaderOpacity(distanceFromListTop, isAllLoaded, isCalculationsComplete, startPosition) {
  return useDerivedValue(function () {
    'worklet';
    const isCalculationsCompleteValue = isCalculationsComplete.value;
    if (distanceFromListTop.value < startPosition && isAllLoaded.value) {
      return isCalculationsCompleteValue ? withTiming(0) : 0;
    } else {
      return isCalculationsCompleteValue ? withTiming(1) : 1;
    }
  });
}

export function navigationHeaderPosition(distanceFromListTop, isAllLoaded, topBarHeight, startPosition) {
  return useDerivedValue(function () {
    'worklet';
    return distanceFromListTop.value < startPosition && isAllLoaded.value ? withTiming(topBarHeight) : withTiming(0);
  });
}

export function navigationButtonsCompleteOpacity(isCalculationComplete) {
  return useDerivedValue(function () {
    'worklet';
    return isCalculationComplete.value ? withTiming(1) : 0;
  });
}

export function interpolateNavigationViewOpacity(props) {
  return useDerivedValue(function () {
    'worklet';
    const {
      'all-loaded?': isAllLoaded,
      'end-position': endPosition,
      'start-position': startPosition,
      'distance-from-list-top': distanceFromListTop,
    } = props;
    if (isAllLoaded.value) {
      return interpolate(distanceFromListTop.value, [startPosition, endPosition], [0, 1], {
        extrapolateLeft: 'clamp',
        extrapolateRight: 'clamp',
      });
    } else {
      return 1;
    }
  });
}
