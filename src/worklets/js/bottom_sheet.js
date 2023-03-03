import {useDerivedValue, runOnJS as reaRunOnJS, runOnJS} from "react-native-reanimated";

export function useTranslateY(initialTranslationY, bottomSheetDy, panY) {
  return useDerivedValue(() => {
    return initialTranslationY - (bottomSheetDy.value - panY.value)
  })
}

export function useBackgroundOpacity(translateY, backgroundHeight, windowHeight) {
  return useDerivedValue(() => {
    const opacity = ((translateY.value - windowHeight) / -backgroundHeight) * 0.5

    return Math.max(Math.min(opacity, 0.5), 0)
  })
}
