import {
  useDerivedValue,
  useAnimatedStyle,
  interpolate,
  scrollTo,
  withTiming,
  cancelAnimation,
  withDecay,
} from 'react-native-reanimated';

import { Platform } from 'react-native';

export function useStartScrollValue(isCollapsed, collapseThreshold) {
  return useDerivedValue(() => {
    return isCollapsed ? -collapseThreshold.value : 0;
  });
}

export function useScrollValue(isCollapsed, collapseThreshold) {
  return useDerivedValue(() => {
    return isCollapsed ? collapseThreshold.value : 0;
  });
}

export function useDerivedValueAdd(sharedValue, value) {
  return useDerivedValue(() => {
    return sharedValue.value + value;
  });
}

export function useDerivedValueMul(sharedValue, value) {
  return useDerivedValue(() => {
    return sharedValue.value * value;
  });
}

export function useLogoStyles({
  initialState,
  scrollAmount,
  collapseThreshold,
  sheetDisplacementThreshold,
  textMovementThreshold,
}) {
  return useAnimatedStyle(() => {
    const isFirstDisplacement = initialState.value === 'expanded' || scrollAmount.value < collapseThreshold.value;
    if (isFirstDisplacement) {
      return {
        transform: [
          { translateX: 20 },
          { translateY: interpolate(scrollAmount.value, [0, collapseThreshold.value], [0, -42.5], 'clamp') },
          { scale: interpolate(scrollAmount.value, [0, textMovementThreshold.value], [1, 0.4], 'clamp') },
        ],
      };
    } else {
      return {
        transform: [
          { translateX: 20 },
          {
            translateY: interpolate(
              scrollAmount.value,
              [collapseThreshold.value, sheetDisplacementThreshold.value],
              [-42.5, -50.5],
              'clamp',
            ),
          },
          { scale: 0.4 },
        ],
      };
    }
  });
}

export function useSheetStyles({ initialState, scrollAmount, collapseThreshold, sheetDisplacementThreshold }) {
  return useAnimatedStyle(() => {
    const isFirstDisplacement = initialState.value === 'expanded' || scrollAmount.value < collapseThreshold.value;
    if (isFirstDisplacement) {
      return {
        transform: [{ translateY: interpolate(scrollAmount.value, [0, collapseThreshold.value], [40, 0], 'clamp') }],
        borderTopLeftRadius: 20,
        borderTopRightRadius: 20,
      };
    } else {
      const radius = interpolate(
        scrollAmount.value,
        [collapseThreshold.value, sheetDisplacementThreshold.value],
        [20, 0],
        'clamp',
      );
      return {
        transform: [
          {
            translateY: interpolate(
              scrollAmount.value,
              [collapseThreshold.value, sheetDisplacementThreshold.value],
              [0, -8],
              'clamp',
            ),
          },
        ],
        borderTopLeftRadius: radius,
        borderTopRightRadius: radius,
      };
    }
  });
}

export function useNameStyles({ initialState, scrollAmount, collapseThreshold, textMovementThreshold }) {
  return useAnimatedStyle(() => {
    let horizontalPosition;
    if (initialState.value === 'collapsed') {
      horizontalPosition = 40;
    } else if (initialState.value === 'expanded') {
      horizontalPosition = 0;
    } else {
      horizontalPosition = interpolate(
        scrollAmount.value,
        [textMovementThreshold.value, collapseThreshold.value],
        [0, 40],
        'clamp',
      );
    }

    let verticalPosition;
    if (initialState.value === 'collapsed') {
      verticalPosition = -44.5;
    } else if (initialState.value === 'expanded') {
      verticalPosition = 0;
    } else {
      verticalPosition = interpolate(
        scrollAmount.value,
        [textMovementThreshold.value, collapseThreshold.value],
        [0, -44.5],
        'clamp',
      );
    }

    return {
      marginRight: horizontalPosition,
      transform: [{ translateX: horizontalPosition }, { translateY: verticalPosition }],
    };
  });
}

export function useInfoStyles({ initialState, scrollAmount, collapseThreshold, infoOpacityThresholdFactor }) {
  return useAnimatedStyle(() => {
    let opacity;
    if (initialState.value === 'collapsed') {
      opacity = 0;
    } else if (initialState.value === 'expanded') {
      opacity = 1;
    } else {
      const infoOpacityThreshold = collapseThreshold.value * infoOpacityThresholdFactor;
      opacity = interpolate(scrollAmount.value, [0, infoOpacityThreshold], [1, 0.2], 'extend');
    }
    return {
      opacity: opacity,
    };
  });
}

export function useChannelsStyles({
  scrollAmount,
  headerHeight,
  collapseThreshold,
  sheetDisplacementThreshold,
  expandHeaderLimit,
}) {
  return useAnimatedStyle(() => {
    const headerDisplacement = (headerHeight.value - 55.5) * -1;
    const firstDisplacement = scrollAmount.value < collapseThreshold.value;
    const secondDisplacement = scrollAmount.value > sheetDisplacementThreshold.value;
    if (firstDisplacement) {
      return {
        transform: [
          {
            translateY: interpolate(
              scrollAmount.value,
              [0, collapseThreshold.value],
              [39, headerDisplacement],
              'clamp',
            ),
          },
        ],
      };
    } else if (secondDisplacement) {
      return {
        transform: [
          {
            translateY: interpolate(
              scrollAmount.value,
              [sheetDisplacementThreshold.value, expandHeaderLimit.value],
              [headerDisplacement - 8, headerDisplacement - 64],
              'clamp',
            ),
          },
        ],
      };
    } else {
      return {
        transform: [
          {
            translateY: interpolate(
              scrollAmount.value,
              [collapseThreshold.value, sheetDisplacementThreshold.value],
              [headerDisplacement, headerDisplacement - 8],
              'clamp',
            ),
          },
        ],
      };
    }
  });
}

export function useScrollTo({ animatedRef, scrollAmount, expandHeaderLimit }) {
  const isAndroid = Platform.OS === 'android';
  return useDerivedValue(() => {
    scrollTo(animatedRef, 0, scrollAmount.value - expandHeaderLimit.value, isAndroid);
  });
}

export function useHeaderOpacity({ scrollAmount, collapseThreshold, sheetDisplacementThreshold }) {
  return useDerivedValue(() => {
    return interpolate(
      scrollAmount.value,
      [collapseThreshold.value, sheetDisplacementThreshold.value],
      [0, 1],
      'clamp',
    );
  });
}

export function useOppositeHeaderOpacity(headerOpacity) {
  return useDerivedValue(() => {
    return 1 - headerOpacity.value;
  });
}

export function useNavContentOpacity({
  scrollAmount,
  navbarContentThresholdFactor,
  sheetDisplacementThreshold,
  expandHeaderLimit,
}) {
  return useDerivedValue(() => {
    const navbarContentThreshold = sheetDisplacementThreshold.value + navbarContentThresholdFactor;
    return interpolate(scrollAmount.value, [navbarContentThreshold, expandHeaderLimit.value], [0, 1], 'clamp');
  });
}

export function onScrollAnimationEnd(
  scrollAmount,
  scrollStart,
  expandHeaderThreshold,
  snapHeaderThreshold,
  expandHeaderLimit,
  animationDuration,
) {
  'worklet';
  return function () {
    'worklet';
    const duration = { duration: animationDuration };
    if (scrollAmount.value > snapHeaderThreshold && scrollAmount.value <= expandHeaderThreshold) {
      scrollStart.value = withTiming(-expandHeaderThreshold, duration);
      scrollAmount.value = withTiming(expandHeaderThreshold, duration);
    }

    if (scrollAmount.value > expandHeaderThreshold) {
      scrollStart.value = -scrollAmount.value;
    }

    if (scrollAmount.value <= snapHeaderThreshold) {
      scrollStart.value = withTiming(0, duration);
      scrollAmount.value = withTiming(0, duration);
    }

    if (scrollAmount.value > expandHeaderThreshold && scrollAmount.value < expandHeaderLimit) {
      if (scrollAmount.value >= (expandHeaderLimit - expandHeaderThreshold) * 0.65 + expandHeaderThreshold) {
        scrollAmount.value = withTiming(expandHeaderLimit, duration);
        scrollStart.value = withTiming(-expandHeaderLimit, duration);
      } else {
        scrollAmount.value = withTiming(expandHeaderThreshold, duration);
        scrollStart.value = withTiming(-expandHeaderThreshold, duration);
      }
    }
  };
}

export function onPanStart(scrollStart, scrollAmount) {
  return function () {
    'worklet';
    cancelAnimation(scrollStart);
    cancelAnimation(scrollAmount);
    scrollStart.value = -1 * scrollAmount.value;
  };
}

export function onPanUpdate({ scrollStart, scrollAmount, maxScroll, expandHeaderLimit }) {
  return function (event) {
    'worklet';
    const newScrollAmount = -1 * (event.translationY + scrollStart.value);
    if (newScrollAmount <= 0) {
      scrollAmount.value = 0;
    } else {
      const limit = expandHeaderLimit.value + maxScroll.value;
      scrollAmount.value = newScrollAmount <= limit ? newScrollAmount : limit;
    }
  };
}

export function onPanEnd({
  scrollStart,
  scrollAmount,
  maxScroll,
  expandHeaderLimit,
  collapseThreshold,
  snapHeaderThresholdFactor,
  animationDuration,
}) {
  const isIOS = Platform.OS === 'ios';
  return function (event) {
    'worklet';
    scrollStart.value = -scrollAmount.value;
    const snapHeaderThreshold = collapseThreshold.value * snapHeaderThresholdFactor;
    const endAnimation = onScrollAnimationEnd(
      scrollAmount,
      scrollStart,
      collapseThreshold.value,
      snapHeaderThreshold,
      expandHeaderLimit.value,
      animationDuration,
    );
    if (scrollAmount.value < expandHeaderLimit.value) {
      endAnimation();
    } else {
      const maxValue = maxScroll.value + expandHeaderLimit.value;
      const decelerationRate = isIOS ? { deceleration: 0.998 } : { deceleration: 0.996 };

      scrollStart.value = withDecay({
        ...{
          velocity: event.velocityY,
          clamp: [-1 * maxValue, 0],
        },
        ...decelerationRate,
      });
      scrollAmount.value = withDecay(
        {
          ...{
            velocity: -1 * event.velocityY,
            clamp: [0, maxValue],
          },
          ...decelerationRate,
        },
        endAnimation,
      );
    }
  };
}
