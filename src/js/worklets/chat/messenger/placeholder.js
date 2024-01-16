import { useDerivedValue } from 'react-native-reanimated';

export function placeholderOpacity(isCalculationsComplete) {
  return useDerivedValue(function () {
    'worklet';
    return isCalculationsComplete.value ? 0 : 1;
  });
}

export function placeholderZIndex(isCalculationsComplete) {
  return useDerivedValue(function () {
    'worklet';
    return isCalculationsComplete.value ? 0 : 2;
  });
}
