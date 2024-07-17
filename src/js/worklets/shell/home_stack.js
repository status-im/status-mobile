import { useDerivedValue, withTiming, withSequence, withDelay } from 'react-native-reanimated';
import * as constants from './constants';

// Derived values for each stack (communities, chat, wallet, browser)
export function stackOpacity(stackId, selectedStackId) {
  return useDerivedValue(function () {
    'worklet';
    return selectedStackId.value == stackId ? 1 : 0;
  });
}

export function stackZIndex(stackId, selectedStackId) {
  return useDerivedValue(function () {
    'worklet';
    return selectedStackId.value == stackId ? 10 : 9;
  });
}

// Derived values for home stack (container)
export function homeStackOpacity(homeStackState) {
  return useDerivedValue(function () {
    'worklet';
    switch (homeStackState.value) {
      case constants.OPEN_WITH_ANIMATION:
        return withTiming(1, constants.LINEAR_EASING);
      case constants.CLOSE_WITH_ANIMATION:
        return withTiming(0, constants.LINEAR_EASING);
      case constants.OPEN_WITHOUT_ANIMATION:
        return 1;
      case constants.CLOSE_WITHOUT_ANIMATION:
        return 0;
    }
  });
}

export function homeStackTop(homeStackState, top) {
  return useDerivedValue(function () {
    'worklet';
    switch (homeStackState.value) {
      case constants.OPEN_WITH_ANIMATION:
        return withTiming(0, constants.LINEAR_EASING);
      case constants.CLOSE_WITH_ANIMATION:
        return withTiming(top, constants.LINEAR_EASING);
      case constants.OPEN_WITHOUT_ANIMATION:
        return 0;
      case constants.CLOSE_WITHOUT_ANIMATION:
        return top;
    }
  });
}

export function homeStackLeft(selectedStackId, animateHomeStackLeft, homeStackState, left) {
  return useDerivedValue(function () {
    'worklet';
    if (animateHomeStackLeft.value) {
      var leftValue = left[selectedStackId.value];
      switch (homeStackState.value) {
        case constants.OPEN_WITH_ANIMATION:
          return withSequence(withTiming(leftValue, { duration: 0 }), withTiming(0, constants.LINEAR_EASING));
        case constants.CLOSE_WITH_ANIMATION:
          return withTiming(leftValue, constants.LINEAR_EASING);
        case constants.OPEN_WITHOUT_ANIMATION:
          return 0;
        case constants.CLOSE_WITHOUT_ANIMATION:
          return leftValue;
      }
    } else {
      return 0;
    }
  });
}

export function homeStackPointer(homeStackState) {
  return useDerivedValue(function () {
    'worklet';
    var homeStackStateValue = homeStackState.value;
    return homeStackStateValue == constants.OPEN_WITH_ANIMATION ||
      homeStackStateValue == constants.OPEN_WITHOUT_ANIMATION
      ? 'auto'
      : 'none';
  });
}

export function homeStackScale(homeStackState, minimizeScale) {
  return useDerivedValue(function () {
    'worklet';
    switch (homeStackState.value) {
      case constants.OPEN_WITH_ANIMATION:
        return withTiming(1, constants.LINEAR_EASING);
      case constants.CLOSE_WITH_ANIMATION:
        return withTiming(minimizeScale, constants.LINEAR_EASING);
      case constants.OPEN_WITHOUT_ANIMATION:
        return 1;
      case constants.CLOSE_WITHOUT_ANIMATION:
        return minimizeScale;
    }
  });
}

export function homeStackBorderRadius(homeStackState) {
  return useDerivedValue(function () {
    'worklet';
    switch (homeStackState.value) {
      case constants.OPEN_WITH_ANIMATION:
        return withDelay(constants.SHELL_ANIMATION_TIME, withTiming(0, { duration: 0 }));
      case constants.CLOSE_WITH_ANIMATION:
      case constants.CLOSE_WITHOUT_ANIMATION:
        return 20;
      case constants.OPEN_WITHOUT_ANIMATION:
        return 0;
    }
  });
}
