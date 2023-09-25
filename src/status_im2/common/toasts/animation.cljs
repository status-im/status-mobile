(ns status-im2.common.toasts.animation
  (:require [react-native.gesture :as gesture]
            [react-native.reanimated :as reanimated]))

(def slide-out-up-animation
  (-> ^js reanimated/slide-out-up-animation
      (.springify)
      (.damping 20)
      (.stiffness 12)))

(def slide-in-up-animation
  (-> ^js reanimated/slide-in-up-animation
      (.springify)
      (.damping 20)
      (.stiffness 150)))

(def linear-transition
  (-> ^js reanimated/linear-transition
      .springify
      (.damping 20)
      (.stiffness 170)))

(defn- reset-translate-y
  ([translate-y]
   (reset-translate-y translate-y 0))
  ([translate-y spring-value]
   (reanimated/animate-shared-value-with-spring
    translate-y
    spring-value
    {:mass 1 :damping 20 :stiffness 300})))

(defn- dismiss
  [translate-y set-dismissed-locally close-toast]
  (reset-translate-y translate-y -500)
  (set-dismissed-locally)
  (close-toast))

(defn on-update-gesture
  [translate-y set-dismissed-locally close-toast]
  (fn [^js evt]
    (let [evt-translation-y (.-translationY evt)
          pan-down?         (> evt-translation-y 100)
          pan-up?           (< evt-translation-y -30)]
      (cond
        pan-down? (reset-translate-y translate-y)
        pan-up?   (dismiss translate-y set-dismissed-locally close-toast)
        :else     (reanimated/set-shared-value translate-y evt-translation-y)))))

(defn pan-gesture
  [{:keys [clear-timer create-timer translate-y close-toast set-dismissed-locally
           dismissed-locally?]}]
  (-> (gesture/gesture-pan)
      (gesture/on-start clear-timer)
      (gesture/on-update (on-update-gesture translate-y set-dismissed-locally close-toast))
      (gesture/on-end #(when-not dismissed-locally?
                         (reanimated/set-shared-value translate-y 0)
                         (create-timer)))))
