import {useEffect, useState} from "react"
import {Dimensions} from "react-native"
import {useAnimatedScrollHandler, useDerivedValue, interpolate, runOnJS} from "react-native-reanimated"

export const SCREEN_WIDTH = Dimensions.get('window').width

export function useSwitcherScrollHandler(translateX, updateCircularData) {
  return useAnimatedScrollHandler(
    (event) => {
      if (event.contentOffset.x < 0) return

      runOnJS(updateCircularData)()

      translateX.value = event.contentOffset.x
    })
}

export function useCircularTabsData(translateX, tabsCount, data) {
  const swipedTabsCount = translateX.value / SCREEN_WIDTH

  return swipedTabsCount <= data.length - tabsCount * 0.75 ? data : undefined
}

export function useHeaderOffsetLeft(translateX, tabsCount, headerMeasures) {
  const [initialHeaderMeasures, setInitialHeaderMeasures] = useState()

  const swipedTabsCount = useDerivedValue(() => Math.floor(translateX.value / SCREEN_WIDTH))

  const initialOffset = useDerivedValue(() => {
    let offsetLeft = 0

    const sliceCount = Number.isSafeInteger(swipedTabsCount.value) ? 0 : 1

    for (let i = 0; i < swipedTabsCount.value - sliceCount; i++) {
      offsetLeft += initialHeaderMeasures[i % tabsCount]
    }

    return offsetLeft
  })

  useEffect(() => {
    if (
      initialHeaderMeasures == undefined &&
      headerMeasures != undefined &&
      Array.isArray(headerMeasures) &&
      !headerMeasures.includes(null)) {
      setInitialHeaderMeasures(headerMeasures)
    }
  }, [headerMeasures])

  return useDerivedValue(() => {
    if (initialHeaderMeasures == undefined) return 0

    return -1 * (initialOffset.value + interpolate(
      translateX.value / SCREEN_WIDTH,
      [swipedTabsCount.value, swipedTabsCount.value + 0.9999999999999999999999999],
      [0, initialHeaderMeasures[swipedTabsCount.value % tabsCount]]
    ))
  })
}

export function useHeaderTextOpacity(translateX, tabIndex) {
  return useDerivedValue(() => {
    return interpolate(
      translateX.value / SCREEN_WIDTH,
      [tabIndex - 1, tabIndex, tabIndex + 1],
      [0.5, 1, 0.5],
      {extrapolateLeft: 'clamp', extrapolateRight: 'clamp'}
    )
  })
}

