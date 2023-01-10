(ns status-im2.contexts.chat.lightbox.common
  (:require [react-native.reanimated :as reanimated]))

(def flat-list-ref (atom nil))

(def small-list-ref (atom nil))

(def scroll-index-lock? (atom false))

(def insets-atom (atom nil))

(def top-view-height 56)

(defn set-val-timing
  [animation value]
  (reanimated/set-shared-value animation (reanimated/with-timing value)))

(defn use-val
  [value]
  (reanimated/use-shared-value value))
