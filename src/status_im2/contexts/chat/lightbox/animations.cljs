(ns status-im2.contexts.chat.lightbox.animations
  (:require [react-native.reanimated :as reanimated]))

;; TODO: Abstract Reanimated methods in a better way, issue:
;; https://github.com/status-im/status-mobile/issues/15176
(def get-val reanimated/get-shared-value)

(def set-val reanimated/set-shared-value)

(def use-val reanimated/use-shared-value)

(defn animate
  ([animation value]
   (animate animation value 300))
  ([animation value duration]
   (set-val animation
            (reanimated/with-timing value
                                    (clj->js {:duration duration
                                              ;;commented out to upgrade react-native-reanimated to v3
                                              ;;and react-native to 0.72
                                              ;;TODO: replace this with an updated implementation
                                              ;                                              :easing
                                              ;                                              (reanimated/default-easing)
                                             })))))

(def animate-decay reanimated/animate-shared-value-with-decay)
