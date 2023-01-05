import { useDerivedValue, interpolate } from 'react-native-reanimated';

// Generic Worklets

export function applyAnimationsToStyle(animations, style) {
  return function() {
    'worklet'

    var animatedStyle = {}

    for (var key in animations) {
      if (key == "transform") {
        var transforms = animations[key];
        var animatedTransforms = []

        for (var transform of transforms) {
          var transformKey = Object.keys(transform)[0];
          animatedTransforms.push({
            [transformKey]: transform[transformKey].value
          })
        }

        animatedStyle[key] = animatedTransforms;
      } else {
        animatedStyle[key] = animations[key].value;
      }
    }

    return Object.assign(animatedStyle, style);
  };
};

export function interpolateValue(sharedValue, inputRange, outputRange, extrapolation) {
  return useDerivedValue(
    function () {
      'worklet'
      return interpolate(sharedValue.value, inputRange, outputRange, extrapolation);
    }
  );
}
