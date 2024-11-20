import { useDerivedValue, withTiming } from 'react-native-reanimated';

export function bottomTabIconColor(stackId, selectedStackId, selectedTabColor, defaultColor) {
  return useDerivedValue(function () {
    'worklet';
    if (selectedStackId.value == stackId) {
      return selectedTabColor;
    } else {
      return defaultColor;
    }
  });
}
