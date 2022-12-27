(ns quo2.components.banners.banner.view
  (:require [quo2.components.banners.banner.style :as style]
            [quo2.components.counter.counter :as counter]
            [quo2.components.icon :as icons]
            [quo2.components.markdown.text :as text]
            [react-native.core :as rn]))

(defn banner
  [{:keys [show-pin? latest-pin-text pins-count on-press]}]
  [rn/touchable-opacity
   {:accessibility-label :pinned-banner
    :style               style/container
    :active-opacity      1
    :on-press            on-press}
   (when show-pin? [icons/icon :i/pin {:size 20}])
   [text/text
    {:number-of-lines 1
     :size            :paragraph-2
     :style           {:margin-left 10 :margin-right 50}}
    latest-pin-text]
   [rn/view
    {:accessibility-label :pins-count
     :style               style/counter}
    (when (pos? pins-count) [counter/counter {:type :secondary} pins-count])]])