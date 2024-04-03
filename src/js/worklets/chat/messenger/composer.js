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

export function composerContainerOpacity(isComposerFocused, isEmptyInput, emptyOpacity) {
  return useDerivedValue(function () {
    'worklet';
    return isEmptyInput.value && !isComposerFocused.value
      ? withDelay(300, withTiming(emptyOpacity, { duration: 0 }))
      : 1;
  });
}

export function blurContainerElevation(isComposerFocused, isEmptyInput) {
  return useDerivedValue(function () {
    'worklet';
    return isEmptyInput.value && !isComposerFocused.value ? 10 : 0;
  });
}

export function composerElevation(isComposerFocused, isEmptyInput) {
  return useDerivedValue(function () {
    'worklet';
    return isEmptyInput.value && !isComposerFocused.value ? 0 : 10;
  });
}
