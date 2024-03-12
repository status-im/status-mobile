import { useDerivedValue, withTiming } from 'react-native-reanimated';

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

export function jumpToButtonOpacity(scrollDownButtonOpacity, isComposerFocused) {
  return useDerivedValue(function () {
    'worklet';
    return withTiming(scrollDownButtonOpacity.value == 1 || isComposerFocused.value ? 0 : 1);
  });
}

export function jumpToButtonPosition(scrollDownButtonOpacity, isComposerFocused) {
  return useDerivedValue(function () {
    'worklet';
    return withTiming(scrollDownButtonOpacity.value == 1 || isComposerFocused.value ? 35 : 0);
  });
}
