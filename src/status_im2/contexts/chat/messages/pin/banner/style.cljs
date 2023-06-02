(ns status-im2.contexts.chat.messages.pin.banner.style
  (:require [react-native.reanimated :as reanimated]))

(defn pinned-banner
  [top-offset]
  {:position :absolute
   :left     0
   :right    0
   :top      top-offset})

(defn animated-pinned-banner
  [top-offset enabled? animation]
  (reanimated/apply-animations-to-style
    (when enabled?
      {:opacity animation})
    (pinned-banner top-offset)))