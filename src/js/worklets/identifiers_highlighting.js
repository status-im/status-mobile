import { useAnimatedStyle, useDerivedValue } from 'react-native-reanimated';

export function background(color, progress) {
  return useDerivedValue(
    function() {
      'worklet'  
      const r = parseInt(color.substring(1, 3), 16);
      const g = parseInt(color.substring(3, 5), 16);
      const b = parseInt(color.substring(5, 7), 16);

      return progress.value <= 25 ? `rgba(${r}, ${g}, ${b}, .4)` : `rgba(${r}, ${g}, ${b}, .2)`;
    }
  )
}

export function opacity(progress) {
  return useDerivedValue(
    function() {
      'worklet'
      return progress.value <= 25 ? 1 : 0.3;
    }
  )
}

export function avatarOpacity(progress) {
  return useDerivedValue(
    function () {
      'worklet'
      const progressValue = progress.value;

      switch (true) {
        case progressValue <= 25:
          return 1;
        case progressValue <= 50:
          return 0.1;
        default:
          return 0.3;
      }
    }
  )
}

export function ringOpacity(progress) {
  return useDerivedValue(
    function () {
      'worklet'
      return progress.value <= 50 ? 1 : 0.3;
    }
  )
}

export function userHashOpacity(progress) {
  return useDerivedValue(
    function () {
      'worklet'
      const progressValue = progress.value;
      switch (true) {
        case progressValue <= 25:
          return 1;
        case progressValue <= 50:
          return 0.3;
        case progressValue <= 75:
          return 1;
        default:
          return 0.3;
      }
    }
  )
}

export function userHashColor(progress) {
  return useDerivedValue(
    function () {
      'worklet'
      const progressValue = progress.value;
      switch (true) {
        case progressValue <= 25:
          return 'rgba(255, 255, 255, .6)';
        case progressValue <= 50:
          return 'rgba(255, 255, 255, .6)';
        case progressValue <= 75:
          return 'rgb(255, 255, 255)';
        default:
          return 'rgba(255, 255, 255, .6)';
      }
    }
  )
}

export function emojiHashStyle(progress) {
  return useAnimatedStyle(
    function () {
      'worklet'
      const progressValue = progress.value;

      switch (true) {
        case progressValue <= 25:
          return { opacity: 1 };
        case progressValue <= 75:
          return { opacity: 0.3 }
        default:
          return { opacity: 1 };
      }
    }
  )
}
