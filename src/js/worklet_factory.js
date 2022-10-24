import { useDerivedValue, withTiming, withSequence, withDelay, Easing } from 'react-native-reanimated';

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

// Switcher Worklets

export function stackOpacity (stackId, selectedStackId) {
  return useDerivedValue(
    function () {
      'worklet'
      return selectedStackId.value == stackId ? 1 : 0;
    }
  );
}

export function stackPointer (stackId, selectedStackId) {
  return useDerivedValue(
    function () {
      'worklet'
      return selectedStackId.value == stackId ? "auto" : "none";
    }
  );
}

export function bottomTabIconColor (stackId, selectedStackId, passThrough, selectedTabColor, defaultColor, passThroughColor) {
  return useDerivedValue(
    function () {
      'worklet'
      if (selectedStackId.value == stackId){
	return selectedTabColor;
      }
      else if (passThrough.value){
	return passThroughColor;
      }
      else {
	return defaultColor;
      }
    }
  );
}


// Home Stack

const defaultDurationAndEasing = {
  duration: 300,
  easing: Easing.bezier(0, 0, 1, 1),
}

export function homeStackOpacity (homeStackOpen) {
  return useDerivedValue(
    function () {
      'worklet'
      return withTiming(homeStackOpen.value ? 1 : 0, defaultDurationAndEasing);
    }
  );
}

export function homeStackTop (homeStackOpen, top) {
  return useDerivedValue(
    function () {
      'worklet'
      return withTiming(homeStackOpen.value ? 0 : top, defaultDurationAndEasing);
    }
  );
}

export function homeStackLeft (selectedStackId, animateHomeStackLeft, homeStackOpen, left) {
  return useDerivedValue(
    function () {
      'worklet'
      if (animateHomeStackLeft.value) {
	var leftValue = left[selectedStackId.value];
	if (homeStackOpen.value) {
	  return withSequence(withTiming(leftValue, {duration: 0}), withTiming(0, defaultDurationAndEasing))
	} else {
	  return withTiming(leftValue, defaultDurationAndEasing);
	}
      } else {
	return 0;
      }
    }
  );
}

export function homeStackPointer (homeStackOpen) {
  return useDerivedValue(
    function () {
      'worklet'
      return homeStackOpen.value ? "auto" : "none";
    }
  );
}

export function homeStackScale (homeStackOpen, minimizeScale) {
  return useDerivedValue(
    function () {
      'worklet'
      return withTiming(homeStackOpen.value ? 1 : minimizeScale, defaultDurationAndEasing);
    }
  );
}
