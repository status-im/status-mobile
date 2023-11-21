import { useDerivedValue } from 'react-native-reanimated';

export function infoLayout(input, isTop) {
  return useDerivedValue(function () {
    'worklet';
    return isTop ? input.value : -input.value;
  });
}
