(ns status-im.contexts.profile.settings.header.header-shape
  (:require [quo.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.reanimated :as reanimated]
            [react-native.svg :as svg]
            [status-im.contexts.profile.settings.header.style :as style]))

(defn left-radius
  [background-color]
  [svg/svg {:width "20" :height "20" :viewBox "0 0 20 20" :fill "none"}
   [svg/path
    {:d    "M20 0C7 2 0 10 0 20V0H15Z"
     :fill background-color}]])

(defn right-radius
  [background-color]
  [svg/svg {:width "20" :height "21" :viewBox "0 0 20 21" :fill "none"}
   [svg/path
    {:d    "M20 20V0H0C11 0 20 9 20 20Z"
     :fill background-color}]])

(defn view
  [{:keys [scroll-y customization-color theme]}]
  (let [background-color  (colors/resolve-color customization-color theme 40)
        opacity-animation (reanimated/interpolate scroll-y
                                                  [0 45 50]
                                                  [1 1 0])]
    [:<>
     [rn/view {:style (style/header-middle-shape background-color)}]
     [reanimated/view {:style (style/radius-container opacity-animation)}
      [left-radius background-color]
      [right-radius background-color]]]))
