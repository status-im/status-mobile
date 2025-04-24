import { useDerivedValue, withTiming, withSequence, withDelay } from 'react-native-reanimated';

// Derived values for each stack (communities, chat, wallet, browser)
export function stackOpacity(stackId, selectedStackId) {
  return useDerivedValue(function () {
    'worklet';
    return selectedStackId.value == stackId ? 1 : 0;
  });
}

export function stackZIndex(stackId, selectedStackId) {
  return useDerivedValue(function () {
    'worklet';
    return selectedStackId.value == stackId ? 10 : 9;
  });
}
