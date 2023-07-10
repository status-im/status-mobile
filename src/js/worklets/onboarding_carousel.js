import { useDerivedValue, withTiming, Easing } from 'react-native-reanimated';

const slideAnimationDuration = 300;
const totalPages = 4;
const pageSize = 100 / totalPages;

const easeOut = {
  duration: slideAnimationDuration,
  easing: Easing.bezier(0, 0, 0.58, 1),
};

// Derived Values
export function dynamicProgressBarWidth(staticProgressBarWidth, progress) {
  return useDerivedValue(function () {
    'worklet';
    return (staticProgressBarWidth * (progress.value || 0)) / 100;
  });
}

export function carouselLeftPosition(windowWidth, progress, isDragging, dragAmount) {
  return useDerivedValue(function () {
    'worklet';
    const progressValue = progress.value;
    const dragAmountValue = dragAmount.value;

    const baseOffset = (Math.floor(progressValue / pageSize) % totalPages) * windowWidth;
    const adjustedOffset = baseOffset === 0 && dragAmountValue > 0 ? baseOffset : -baseOffset + dragAmountValue;

    return isDragging.value ? withTiming(adjustedOffset, easeOut) : withTiming(-baseOffset, easeOut);
  });
}
