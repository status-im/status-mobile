import { withTiming, runOnJS } from 'react-native-reanimated';

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
