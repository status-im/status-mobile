(ns utils.worklets.scroll-view)

(def scroll-worklet-js (js/require "../src/js/worklets/scroll_view.js"))

(defn use-animated-scroll-handler
  [scroll-y]
  (.useAnimatedScrollHandlerWorklet ^js scroll-worklet-js scroll-y))
