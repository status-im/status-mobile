import { useDerivedValue } from 'react-native-reanimated';

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

export function switcherCloseButtonOpacity (switcherButtonOpacity) {  
  return useDerivedValue(
    function () {
      'worklet'
      return 1 - switcherButtonOpacity.value;
    }
  );
}

export function switcherScreenRadius (switcherScreenSize) {
  return useDerivedValue(
    function () {
      'worklet'
      return switcherScreenSize.value/2;
    }
  );
}

export function switcherScreenBottomPosition (switcherScreenRadius, switcherPressedRadius, initalPosition) {
  return useDerivedValue(
    function () {
      'worklet'
      return initalPosition + switcherPressedRadius - switcherScreenRadius.value;
    }
  );
}

export function switcherContainerBottomPosition (switcherScreenBottom, heightOffset) {
  return useDerivedValue(
    function () {
      'worklet'
      return - (switcherScreenBottom.value + heightOffset);
    }
  );
}
