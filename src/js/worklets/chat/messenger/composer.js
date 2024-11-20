import { useDerivedValue, withTiming, withDelay } from 'react-native-reanimated';

export function scrollDownButtonOpacity(chatListScrollY, isComposerFocused, windowHeight) {
  return useDerivedValue(function () {
    'worklet';
    if (isComposerFocused.value) {
      return 0;
    } else {
      return chatListScrollY.value > windowHeight * 0.75 ? 1 : 0;
    }
  });
}
