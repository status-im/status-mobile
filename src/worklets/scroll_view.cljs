(ns worklets.scroll-view)

(def scroll-worklet-js (js/require "../src/worklets/js/scroll_view.js"))

(defn use-animated-scroll-handler
  [scroll-y]
  (.useAnimatedScrollHandlerWorklet ^js scroll-worklet-js scroll-y))
