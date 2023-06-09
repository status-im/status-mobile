import { useDerivedValue, interpolate, interpolateColor } from 'react-native-reanimated';

// Generic Worklets

// 1. kebab-case styles are not working for worklets
//    so we have to convert kebab case styles into camel case styles
// 2. remove keys with nil value, else useAnimatedStyle will throw an error
//    https://github.com/status-im/status-mobile/issues/14756
export function applyAnimationsToStyle(animations, style) {
  return function() {
    'worklet'

    var animatedStyle = {}

    // Normal Style
    for (var key in style) {
      if (key == "transform") {
	var transforms = style[key];
	var filteredTransforms = []

	for (var transform of transforms) {
	  var transformKey = Object.keys(transform)[0];
	  var transformValue = transform[transformKey];
	  if(transformValue !== null) {
	    filteredTransforms.push(
	      {[transformKey.replace(/-./g, x=>x[1].toUpperCase())]: transformValue}
	    );
	  }
	}

	animatedStyle[key] = filteredTransforms;
      } else {
	var value = style[key];
	if (value !== null) {
	  animatedStyle[key.replace(/-./g, x=>x[1].toUpperCase())] = value;
	}
      }
    }

    // Animations
    for (var key in animations) {
      if (key == "transform") {
        var transforms = animations[key];
        var animatedTransforms = []

        for (var transform of transforms) {
          var transformKey = Object.keys(transform)[0];
	  var transformValue = transform[transformKey].value;
	  if (transformValue !== null) {
            animatedTransforms.push(
	      {[transformKey.replace(/-./g, x=>x[1].toUpperCase())]: transformValue}
	    );
	  }
        }

        animatedStyle[key] = animatedTransforms;
      } else {
	var animatedValue = animations[key].value;
	if (animatedValue !== null) {
          animatedStyle[key.replace(/-./g, x=>x[1].toUpperCase())] = animatedValue;
	}
      }
    }

    return animatedStyle;
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

export function interpolateColorValue(sharedValue, inputRange, outputRange, space, options) {
  return useDerivedValue(
    function () {
      'worklet'
      return interpolateColor(sharedValue.value, inputRange, outputRange, space, options);
    }
  );
}
