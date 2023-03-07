(ns status-im2.contexts.chat.lightbox.animations
  (:require [react-native.reanimated :as reanimated]))

;; TODO: Abstract Reanimated methods in a better way, issue:
;; https://github.com/status-im/status-mobile/issues/15176
(defn get-val
  [animation]
  (reanimated/get-shared-value animation))

(defn set-val
  [animation value]
  (reanimated/set-shared-value animation value))

(defn use-val
  [value]
  (reanimated/use-shared-value value))

(defn animate
  ([animation value]
   (animate animation value 300))
  ([animation value duration]
   (set-val animation
            (reanimated/with-timing value
                                    (clj->js {:duration duration
                                              :easing   (get reanimated/easings :default)})))))

(defn animate-decay
  [animation velocity clamp]
  (set-val animation
           (reanimated/with-decay (clj->js {:velocity velocity
                                            :clamp    clamp}))))
