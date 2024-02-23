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

export function containerOpacity(isComposerFocused, isEmptyInput, emptyOpacity) {
  return useDerivedValue(function () {
    'worklet';
    return isEmptyInput.value && !isComposerFocused.value ? emptyOpacity : 1;
  });
}

export function blurContainerElevation(containerOpacity) {
  return useDerivedValue(function () {
    'worklet';
    return containerOpacity.value == 1 ? 0 : 10;
  });
}

export function composerElevation(containerOpacity) {
  return useDerivedValue(function () {
    'worklet';
    return containerOpacity.value == 1 ? 10 : 0;
  });
}

export function backgroundOverlayOpacity(inputContainerHeight) {
  return useDerivedValue(function () {
    'worklet';
    return inputContainerHeight.value > 100 ? 1 : 0; // TODO - Make it dynamic
  });
}
