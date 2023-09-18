import { useDerivedValue, withTiming, withSequence, withDelay, Easing, runOnJS } from 'react-native-reanimated';
import * as constants from './constants';

// Derived Values
export function screenLeft(screenState, screenWidth, switcherCardLeftPosition) {
  return useDerivedValue(function () {
    'worklet';
    switch (screenState.value) {
      case constants.CLOSE_SCREEN_WITH_SLIDE_ANIMATION:
        return withTiming(screenWidth, constants.EASE_OUT_EASING);
      case constants.OPEN_SCREEN_WITH_SLIDE_ANIMATION:
        return withTiming(0, constants.EASE_OUT_EASING);
      case constants.CLOSE_SCREEN_WITHOUT_ANIMATION:
        return screenWidth;
      case constants.OPEN_SCREEN_WITHOUT_ANIMATION:
        // Note - don't use return 0; its not working in ios
        // https://github.com/software-mansion/react-native-reanimated/issues/3296#issuecomment-1573900172
        return withSequence(withTiming(-1, { duration: 0 }), withTiming(0, { duration: 0 }));
      case constants.CLOSE_SCREEN_WITH_SHELL_ANIMATION:
        return withTiming(switcherCardLeftPosition, constants.EASE_OUT_EASING);
      case constants.OPEN_SCREEN_WITH_SHELL_ANIMATION:
        return withTiming(0, constants.EASE_OUT_EASING);
      default:
        return screenWidth;
    }
  });
}

export function screenTop(screenState, switcherCardTopPosition) {
  return useDerivedValue(function () {
    'worklet';
    switch (screenState.value) {
      case constants.CLOSE_SCREEN_WITH_SHELL_ANIMATION:
        return withTiming(switcherCardTopPosition, constants.EASE_OUT_EASING);
      case constants.OPEN_SCREEN_WITH_SHELL_ANIMATION:
        return withTiming(0, constants.EASE_OUT_EASING);
      default:
        return 0;
    }
  });
}

export function screenWidth(screenState, screenWidth, switcherCardSize) {
  return useDerivedValue(function () {
    'worklet';
    switch (screenState.value) {
      case constants.CLOSE_SCREEN_WITH_SHELL_ANIMATION:
        return withTiming(switcherCardSize, constants.EASE_OUT_EASING);
      case constants.OPEN_SCREEN_WITH_SHELL_ANIMATION:
        return withSequence(
          withTiming(switcherCardSize, { duration: 0 }),
          withTiming(screenWidth, constants.EASE_OUT_EASING),
        );
      default:
        return screenWidth;
    }
  });
}

export function screenHeight(screenState, screenHeight, switcherCardSize) {
  return useDerivedValue(function () {
    'worklet';
    switch (screenState.value) {
      case constants.CLOSE_SCREEN_WITH_SHELL_ANIMATION:
        return withTiming(switcherCardSize, constants.EASE_OUT_EASING);
      case constants.OPEN_SCREEN_WITH_SHELL_ANIMATION:
        return withSequence(
          withTiming(switcherCardSize, { duration: 0 }),
          withTiming(screenHeight, constants.EASE_OUT_EASING),
        );
      default:
        return screenHeight;
    }
  });
}

export function screenZIndex(screenState) {
  return useDerivedValue(function () {
    'worklet';
    switch (screenState.value) {
      case constants.CLOSE_SCREEN_WITH_SHELL_ANIMATION:
      case constants.CLOSE_SCREEN_WITH_SLIDE_ANIMATION:
        return withDelay(constants.SHELL_ANIMATION_TIME, withTiming(-1, { duration: 0 }));
      case constants.CLOSE_SCREEN_WITHOUT_ANIMATION:
        return -1;
      default:
        return 1;
    }
  });
}

export function screenBorderRadius(screenState) {
  return useDerivedValue(function () {
    'worklet';
    switch (screenState.value) {
      case constants.OPEN_SCREEN_WITH_SHELL_ANIMATION:
        return withDelay(constants.SHELL_ANIMATION_TIME, withTiming(0, { duration: 0 }));
      case constants.OPEN_SCREEN_WITH_SLIDE_ANIMATION:
      case constants.OPEN_SCREEN_WITHOUT_ANIMATION:
        return 0;
      case constants.CLOSE_SCREEN_WITH_SLIDE_ANIMATION:
        return withDelay(constants.SHELL_ANIMATION_TIME, withTiming(20, { duration: 0 }));
      case constants.CLOSE_SCREEN_WITHOUT_ANIMATION:
      case constants.CLOSE_SCREEN_WITH_SHELL_ANIMATION:
        return 20;
    }
  });
}

export function screenGestureOnUpdate(screenLeft) {
  return function (event) {
    'worklet';
    const absoluteX = event.absoluteX;
    if (absoluteX !== null) {
      screenLeft.value = event.absoluteX;
    }
  };
}

export function screenGestureOnEnd(data) {
  return function (event) {
    'worklet';

    const {
      'screen-left': screenLeft,
      'screen-state': screenState,
      'screen-width': screenWidth,
      'left-velocity': leftVelocity,
      'right-velocity': rightVelocity,
      'screen-closed-callback': screenClosedCallback,
    } = data;
    const absoluteX = event.absoluteX ?? 0;
    const velocityX = event.velocityX ?? 0;
    const closeScreen = velocityX > rightVelocity || (velocityX > leftVelocity && absoluteX >= screenWidth / 2);

    // Velocity (points/sec) = Distance/time
    var animationVelocity = (screenWidth * 1000) / constants.SHELL_ANIMATION_TIME;

    if (Math.abs(velocityX) > animationVelocity) {
      animationVelocity = velocityX; // Faster fling
    }

    const newDistance = closeScreen ? screenWidth - absoluteX : absoluteX;
    const animationTime = (newDistance * 1000) / animationVelocity;

    screenLeft.value = withTiming(closeScreen ? screenWidth : 0, {
      duration: animationTime,
      easing: Easing.bezier(0, 0, 0.58, 1),
    });

    if (closeScreen) {
      runOnJS(screenClosedCallback)(animationTime);
    }
  };
}
