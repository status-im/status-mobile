import { useAnimatedStyle, useDerivedValue } from 'react-native-reanimated';

export function backgroundStyle(color, progress) {
  return useAnimatedStyle(
    function() {
      'worklet'
      const progressValue = progress.value;      
      const r = parseInt(color.substring(1, 3), 16);
      const g = parseInt(color.substring(3, 5), 16);
      const b = parseInt(color.substring(5, 7), 16);

      switch (true) {
        case progressValue <= 25:
          return { backgroundColor: `rgba(${r}, ${g}, ${b}, .4)` };
        case progressValue <= 100:
          return { backgroundColor: `rgba(${r}, ${g}, ${b}, .2)` };
      }
    }
  )
}

export function opacity(progress) {
  return useDerivedValue(
    function() {
      'worklet'
      const progressValue = progress.value;

      switch(true) {
        case progressValue <= 25:
          return 1;
        case progressValue <= 100:
          return 0.3;
      }
    }
  )
}

export function ringStyle(progress) {
  return useAnimatedStyle(
    function () {
      'worklet'
      const progressValue = progress.value;

      switch (true) {
        case progressValue <= 50:
          return { opacity: 1 };
        default:
          return { opacity: 0.3 };
      }
    }
  )
}

export function userHashStyle(color, progress) {
  return useAnimatedStyle(
    function() {
      'worklet'
      const progressValue = progress.value;
      const r = parseInt(color.substring(1, 3), 16);
      const g = parseInt(color.substring(3, 5), 16);
      const b = parseInt(color.substring(5, 7), 16);

      switch (true) {
        case progressValue <= 25:
          return { color: `rgba(${r}, ${g}, ${b}, .6)`, opacity: 1 };
        case progressValue <= 50:
          return { color: `rgba(${r}, ${g}, ${b}, .6)`, opacity: 0.3 };
        case progressValue <= 75:
          return { color: `red`, opacity: 1 };
        case progressValue <= 100:
          return { color: `rgba(${r}, ${g}, ${b}, .6)`, opacity: 0.3 };
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
