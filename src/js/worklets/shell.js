import { useDerivedValue, withTiming, withSequence, withDelay, Easing } from 'react-native-reanimated';

// Shell Worklets

// Home Stack States
const CLOSE_WITH_ANIMATION = 0;
const OPEN_WITH_ANIMATION = 1;
const CLOSE_WITHOUT_ANIMATION = 3;
const OPEN_WITHOUT_ANIMATION = 4;

// Derived Values
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

export function bottomTabIconColor (stackId, selectedStackId, homeStackState,
				    passThrough, selectedTabColor, defaultColor,
				    passThroughColor) {
  return useDerivedValue(
    function () {
      'worklet'
      var homeStackStateValue = homeStackState.value;
      if (selectedStackId.value == stackId &&
	  (homeStackStateValue == OPEN_WITH_ANIMATION ||
	   homeStackStateValue == OPEN_WITHOUT_ANIMATION)){
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

export function bottomTabsHeight(homeStackState, height, extendedHeight) {
  return useDerivedValue(
    function () {
      'worklet'
      switch (homeStackState.value) {
      case OPEN_WITH_ANIMATION:
	return withTiming(extendedHeight, defaultDurationAndEasing);
	break;
      case CLOSE_WITH_ANIMATION:
	return withTiming(height, defaultDurationAndEasing);
	break;
      case OPEN_WITHOUT_ANIMATION:
	return extendedHeight;
	break;
      case CLOSE_WITHOUT_ANIMATION:
	return height;
	break;
      }
    }
  )
}


// Home Stack

const shellAnimationTime = 200;

const defaultDurationAndEasing = {
  duration: shellAnimationTime,
  easing: Easing.bezier(0, 0, 1, 1),
}

export function homeStackOpacity (homeStackState) {
  return useDerivedValue(
    function () {
      'worklet'
      switch (homeStackState.value) {
      case OPEN_WITH_ANIMATION:
	return withTiming(1, defaultDurationAndEasing);
	break;
      case CLOSE_WITH_ANIMATION:
	return withTiming(0, defaultDurationAndEasing);
	break;
      case OPEN_WITHOUT_ANIMATION:
	return 1;
	break;
      case CLOSE_WITHOUT_ANIMATION:
	return 0;
	break;	
      }
    }
  );
}

export function homeStackTop (homeStackState, top) {
  return useDerivedValue(
    function () {
      'worklet'
      switch (homeStackState.value) {
      case OPEN_WITH_ANIMATION:
	return withTiming(0, defaultDurationAndEasing);
	break;
      case CLOSE_WITH_ANIMATION:
	return withTiming(top, defaultDurationAndEasing);
	break;
      case OPEN_WITHOUT_ANIMATION:
	return 0;
	break;
      case CLOSE_WITHOUT_ANIMATION:
	return top;
	break;	
      }
    }
  );
}

export function homeStackLeft (selectedStackId, animateHomeStackLeft, homeStackState, left) {
  return useDerivedValue(
    function () {
      'worklet'
      if (animateHomeStackLeft.value) {
	var leftValue = left[selectedStackId.value];
	switch (homeStackState.value) {
	case OPEN_WITH_ANIMATION:
	  return withSequence(withTiming(leftValue, {duration: 0}), withTiming(0, defaultDurationAndEasing))
	  break;
	case CLOSE_WITH_ANIMATION:
	  return withTiming(leftValue, defaultDurationAndEasing);
	  break;
	case OPEN_WITHOUT_ANIMATION:
	  return 0;
	  break;
	case CLOSE_WITHOUT_ANIMATION:
	  return leftValue;
	  break;	
	}
      } else {
	return 0;
      }
    }
  );
}

export function homeStackPointer (homeStackState) {
  return useDerivedValue(
    function () {
      'worklet'
      var homeStackStateValue = homeStackState.value;
      return (homeStackStateValue == OPEN_WITH_ANIMATION ||
	      homeStackStateValue == OPEN_WITHOUT_ANIMATION) ? "auto" : "none";
    }
  );
}

export function homeStackScale (homeStackState, minimizeScale) {
  return useDerivedValue(
    function () {
      'worklet'
      switch (homeStackState.value) {
      case OPEN_WITH_ANIMATION:
	return withTiming(1, defaultDurationAndEasing);
	break;
      case CLOSE_WITH_ANIMATION:
	return withTiming(minimizeScale, defaultDurationAndEasing);
	break;
      case OPEN_WITHOUT_ANIMATION:
	return 1;
	break;
      case CLOSE_WITHOUT_ANIMATION:
	return minimizeScale;
	break;	
      }
    }
  );
}
