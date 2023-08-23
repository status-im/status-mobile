import { useDerivedValue, runOnJS as reaRunOnJS, runOnJS } from 'react-native-reanimated';

export function useTranslateY(initialTranslationY, bottomSheetDy, panY) {
  return useDerivedValue(() => {
    return initialTranslationY - (bottomSheetDy.value - panY.value);
  });
}

export function useBackgroundOpacity(translateY, backgroundHeight, windowHeight, opacity) {
  return useDerivedValue(() => {
    const calculatedOpacity = ((translateY.value - windowHeight) / -backgroundHeight) * opacity;

    return Math.max(Math.min(calculatedOpacity, opacity), 0);
  });
}
