import { useAnimatedReaction, withTiming, runOnJS } from 'react-native-reanimated';
import { useState } from "react"

export function messagesListOnScroll(distanceFromListTop, chatListScrollY, callback) {
  return function (event) {
    'worklet';
    const currentY = event.contentOffset.y;
    const layoutHeight = event.layoutMeasurement.height;
    const contentSizeY = event.contentSize.height - layoutHeight;
    const newDistance = contentSizeY - currentY;
    distanceFromListTop.value = newDistance;
    chatListScrollY.value = currentY;
    runOnJS(callback)(layoutHeight, newDistance);
  };
}

export function useMessagesScrolledToTop(distanceFromListTop, threshold) {
    const [scrolledToTop, setScrolledToTop] = useState(false)

    useAnimatedReaction(function () {
	return distanceFromListTop.value <= threshold;
    }, function (current, previous) {
	if(current !== previous && current !== scrolledToTop) {
	    runOnJS(setScrolledToTop)(current)
	}
    }, [scrolledToTop])

    return scrolledToTop
}
