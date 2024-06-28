(ns status-im.contexts.chat.messenger.composer.style
  (:require
    [quo.foundations.colors :as colors]
    [quo.foundations.typography :as typography]
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]
    [status-im.contexts.chat.messenger.composer.constants :as constants]
    [status-im.contexts.shell.jump-to.constants :as shell.constants]
    [status-im.feature-flags :as ff]))

(def border-top-radius 20)

(defn shadow
  [theme]
  (when platform/ios?
    {:shadow-radius  20
     :shadow-opacity (colors/theme-colors 0.1 0.7 theme)
     :shadow-color   colors/neutral-100
     :shadow-offset  {:width 0 :height (colors/theme-colors -4 -8 theme)}}))

(def composer-sheet-and-jump-to-container
  {:position :absolute
   :bottom   0
   :left     0
   :right    0})

(defn sheet-container
  [insets {:keys [container-opacity composer-elevation]} theme]
  (reanimated/apply-animations-to-style
   {:opacity   container-opacity
    :elevation composer-elevation}
   (merge
    {:border-top-left-radius  border-top-radius
     :border-top-right-radius border-top-radius
     :padding-horizontal      20
     :background-color        (colors/theme-colors colors/white colors/neutral-95 theme)
     :z-index                 3
     :padding-bottom          (:bottom insets)}
    (shadow theme))))

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

(defn input-container
  [height max-height]
  (reanimated/apply-animations-to-style
   {:height height}
   {:max-height max-height
    :z-index    1}))

(defn input-view
  [{:keys [recording?]}]
  {:overflow   :hidden
   :z-index    1
   :flex       1
   :display    (if @recording? :none :flex)
   :min-height constants/input-height})

(defn input-text
  [{:keys [saved-emoji-kb-extra-height]}
   {:keys [focused? maximized?]}
   {:keys [max-height theme]}]
  (assoc typography/paragraph-1
         :color               (colors/theme-colors :black :white theme)
         :text-align-vertical :top
         :position            (if @saved-emoji-kb-extra-height :relative :absolute)
         :top                 0
         :left                0
         :right               (when (or focused? platform/ios?) 0)
         :max-height          max-height
         :padding-bottom      (when @maximized? 0)))

(defn background
  [opacity background-y window-height]
  (reanimated/apply-animations-to-style
   {:opacity   opacity
    :transform [{:translate-y background-y}]}
   {:position         :absolute
    :left             0
    :right            0
    :bottom           0
    :height           window-height
    :background-color colors/neutral-95-opa-70}))

(defn blur-container
  [composer-default-height {:keys [blur-container-elevation]}]
  [{:elevation blur-container-elevation}
   {:position                :absolute
    :left                    0
    :right                   0
    :bottom                  0
    :height                  composer-default-height
    :border-top-right-radius border-top-radius
    :border-top-left-radius  border-top-radius
    :overflow                :hidden}])

(defn blur-view
  [theme]
  {:style       {:flex 1}
   :blur-radius (if platform/ios? 20 10)
   :blur-type   theme
   :blur-amount 20})

(defn shell-button
  [translate-y opacity]
  (reanimated/apply-animations-to-style
   {:transform [{:translate-y translate-y}]
    :opacity   opacity}
   {}))

(defn shell-button-container
  []
  {:z-index 1
   :top     (if (ff/enabled? ::ff/shell.jump-to)
              0
              (- shell.constants/floating-shell-button-height))})

(def scroll-to-bottom-button
  {:position :absolute
   :right    0
   :left     0})
