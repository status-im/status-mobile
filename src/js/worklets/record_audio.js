import { useDerivedValue } from 'react-native-reanimated';

const MAX_SCALE = 1.8;

export function ringScale(scale, subtract) {
  return useDerivedValue(function () {
    'worklet';
    const value = scale.value;
    const maxDelta = MAX_SCALE - 1;
    const maxDeltaDiff = 1 - maxDelta;
    const maxVirtualScale = MAX_SCALE + maxDelta;
    const decimals = value - Math.floor(value);
    const normalizedValue = value >= maxVirtualScale ? decimals + (parseInt(value) - 1) * maxDeltaDiff + 1 : value;
    return (normalizedValue - subtract > MAX_SCALE ? normalizedValue - maxDelta : normalizedValue) - subtract;
  });
}
