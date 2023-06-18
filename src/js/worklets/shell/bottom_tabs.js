import { useDerivedValue, withTiming } from 'react-native-reanimated';
import * as constants from './constants';

export function bottomTabIconColor(stackId, selectedStackId, homeStackState,
				   passThrough, selectedTabColor, defaultColor,
				   passThroughColor) {
  return useDerivedValue(
    function () {
      'worklet'
      var homeStackStateValue = homeStackState.value;
      if (selectedStackId.value == stackId &&
	  (homeStackStateValue == constants.OPEN_WITH_ANIMATION ||
	   homeStackStateValue == constants.OPEN_WITHOUT_ANIMATION)){
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
      case constants.OPEN_WITH_ANIMATION:
	return withTiming(extendedHeight, constants.LINEAR_EASING);
	break;
      case constants.CLOSE_WITH_ANIMATION:
	return withTiming(height, constants.LINEAR_EASING);
	break;
      case constants.OPEN_WITHOUT_ANIMATION:
	return extendedHeight;
	break;
      case constants.CLOSE_WITHOUT_ANIMATION:
	return height;
	break;
      }
    }
  )
}
