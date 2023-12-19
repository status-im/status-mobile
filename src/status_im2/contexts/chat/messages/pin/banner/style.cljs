(ns status-im2.contexts.chat.messages.pin.banner.style
  (:require
    [react-native.reanimated :as reanimated]
    [status-im2.contexts.chat.messages.constants :as messages.constants]))

(def container
  {:position :absolute
   :overflow :hidden
   :left     0
   :right    0
   :height   messages.constants/pinned-banner-height})

(defn container-animated-style
  [top-offset banner-opacity]
  (reanimated/apply-animations-to-style
   {:opacity banner-opacity}
   (assoc container :top top-offset)))
