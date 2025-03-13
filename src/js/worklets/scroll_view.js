import { useAnimatedScrollHandler } from 'react-native-reanimated';

export function useAnimatedScrollHandlerWorklet(scrollY) {
  return useAnimatedScrollHandler((event) => {
    scrollY.value = event.contentOffset.y;
  });
}
