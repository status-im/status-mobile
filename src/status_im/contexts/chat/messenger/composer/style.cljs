(ns status-im.contexts.chat.messenger.composer.style
  (:require
    [quo.foundations.colors :as colors]
    [quo.foundations.typography :as typography]
    [react-native.reanimated :as reanimated]
    [status-im.contexts.chat.messenger.composer.constants :as constants]))

(def border-top-radius 20)

(def bar-container
  {:height          constants/bar-container-height
   :left            0
   :right           0
   :top             0
   :z-index         1
   :justify-content :center
   :align-items     :center})

(defn bar
  [theme]
  {:width            32
   :height           4
   :border-radius    100
   :background-color (colors/theme-colors colors/neutral-100-opa-5 colors/white-opa-10 theme)})

(defn input-text
  [theme]
  (assoc typography/paragraph-1
         :color               (colors/theme-colors :black :white theme)
         :text-align-vertical :top
         :top                 0
         :left                0
         :max-height          150))

(defn shell-button
  [translate-y opacity]
  (reanimated/apply-animations-to-style
   {:transform [{:translate-y translate-y}]
    :opacity   opacity}
   {}))

(def scroll-to-bottom-button
  {:position :absolute
   :right    0
   :left     0})
