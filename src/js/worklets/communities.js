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

export function useLogoStyles({
  scrollAmount,
  expandHeaderThreshold,
  sheetDisplacementThreshold,
  textMovementThreshold,
}) {
  return useAnimatedStyle(() => {
    const firstDisplacement = scrollAmount.value < expandHeaderThreshold;
    if (firstDisplacement) {
      return {
        transform: [
          { translateX: 20 },
          { translateY: interpolate(scrollAmount.value, [0, expandHeaderThreshold], [0, -42.5], 'clamp') },
          { scale: interpolate(scrollAmount.value, [0, textMovementThreshold], [1, 0.4], 'clamp') },
        ],
      };
    } else {
      return {
        transform: [
          { translateX: 20 },
          {
            translateY: interpolate(
              scrollAmount.value,
              [expandHeaderThreshold, sheetDisplacementThreshold],
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

export function useSheetStyles({ scrollAmount, expandHeaderThreshold, sheetDisplacementThreshold }) {
  return useAnimatedStyle(() => {
    const firstDisplacement = scrollAmount.value < expandHeaderThreshold;
    if (firstDisplacement) {
      return {
        transform: [{ translateY: interpolate(scrollAmount.value, [0, expandHeaderThreshold], [40, 0], 'clamp') }],
        borderTopLeftRadius: 20,
        borderTopRightRadius: 20,
      };
    } else {
      const radius = interpolate(
        scrollAmount.value,
        [expandHeaderThreshold, sheetDisplacementThreshold],
        [20, 0],
        'clamp',
      );
      return {
        transform: [
          {
            translateY: interpolate(
              scrollAmount.value,
              [expandHeaderThreshold, sheetDisplacementThreshold],
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

export function useNameStyles({ scrollAmount, expandHeaderThreshold, textMovementThreshold }) {
  return useAnimatedStyle(() => {
    const animationProgress = interpolate(
      scrollAmount.value,
      [textMovementThreshold, expandHeaderThreshold],
      [0, 40],
      'clamp',
    );
    return {
      marginRight: animationProgress,
      transform: [
        { translateX: animationProgress },
        {
          translateY: interpolate(
            scrollAmount.value,
            [textMovementThreshold, expandHeaderThreshold],
            [0, -44.5],
            'clamp',
          ),
        },
      ],
    };
  });
}

export function useInfoStyles({ scrollAmount, infoOpacityThreshold }) {
  return useAnimatedStyle(() => {
    return {
      opacity: interpolate(scrollAmount.value, [0, infoOpacityThreshold], [1, 0.2], 'extend'),
    };
  });
}

export function useChannelsStyles({
  scrollAmount,
  headerHeight,
  expandHeaderThreshold,
  sheetDisplacementThreshold,
  expandHeaderLimit,
}) {
  return useAnimatedStyle(() => {
    const headerDisplacement = (headerHeight.value - 55.5) * -1;
    const firstDisplacement = scrollAmount.value < expandHeaderThreshold;
    const secondDisplacement = scrollAmount.value > sheetDisplacementThreshold;
    if (firstDisplacement) {
      return {
        transform: [
          {
            translateY: interpolate(scrollAmount.value, [0, expandHeaderThreshold], [39, headerDisplacement], 'clamp'),
          },
        ],
      };
    } else if (secondDisplacement) {
      return {
        transform: [
          {
            translateY: interpolate(
              scrollAmount.value,
              [sheetDisplacementThreshold, expandHeaderLimit],
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
              [expandHeaderThreshold, sheetDisplacementThreshold],
              [headerDisplacement, headerDisplacement - 8],
              'clamp',
            ),
          },
        ],
      };
    }
  }, [headerHeight.value]);
}

export function useScrollTo({ animatedRef, scrollAmount, expandHeaderLimit }) {
  const isAndroid = Platform.OS === 'android';
  return useDerivedValue(() => {
    scrollTo(animatedRef, 0, scrollAmount.value - expandHeaderLimit, isAndroid);
  });
}

export function useHeaderOpacity({ scrollAmount, expandHeaderThreshold, sheetDisplacementThreshold }) {
  return useDerivedValue(() => {
    return interpolate(scrollAmount.value, [expandHeaderThreshold, sheetDisplacementThreshold], [0, 1], 'clamp');
  });
}

export function useOppositeHeaderOpacity(headerOpacity) {
  return useDerivedValue(() => {
    return 1 - headerOpacity.value;
  });
}

export function useNavContentOpacity({ scrollAmount, sheetDisplacementThreshold, expandHeaderLimit }) {
  return useDerivedValue(() => {
    return interpolate(scrollAmount.value, [sheetDisplacementThreshold, expandHeaderLimit], [0, 1], 'clamp');
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
      const limit = expandHeaderLimit + maxScroll.value;
      scrollAmount.value = newScrollAmount <= limit ? newScrollAmount : limit;
    }
  };
}

export function onPanEnd({
  scrollStart,
  scrollAmount,
  maxScroll,
  expandHeaderLimit,
  expandHeaderThreshold,
  snapHeaderThreshold,
  animationDuration,
}) {
  const isIOS = Platform.OS === 'ios';
  return function (event) {
    'worklet';
    scrollStart.value = -scrollAmount.value;
    const endAnimation = onScrollAnimationEnd(
      scrollAmount,
      scrollStart,
      expandHeaderThreshold,
      snapHeaderThreshold,
      expandHeaderLimit,
      animationDuration,
    );
    if (scrollAmount.value < expandHeaderLimit) {
      endAnimation();
    } else {
      const maxValue = maxScroll.value + expandHeaderLimit;
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
