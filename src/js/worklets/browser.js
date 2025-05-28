import { useDerivedValue, useSharedValue, scrollTo } from 'react-native-reanimated';

export function useScrollTab({ animatedRef, xTranslation, animate }) {
  useDerivedValue(() => {
    scrollTo(animatedRef, xTranslation.value, 0, animate);
  });
}
