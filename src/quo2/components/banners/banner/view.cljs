(ns quo2.components.banners.banner.view
  (:require [quo2.components.banners.banner.style :as style]
            [quo2.components.counter.counter.view :as counter]
            [quo2.components.icon :as icons]
            [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [quo2.theme :as quo.theme]))

(defn- view-internal
  [{:keys [hide-pin? latest-pin-text pins-count on-press theme]}]
  (when (pos? pins-count)
    [rn/touchable-opacity
     {:accessibility-label :pinned-banner
      :style               style/container
      :active-opacity      1
      :on-press            on-press}
     (when-not hide-pin?
       [rn/view {:style style/icon}
        [icons/icon :i/pin
         {:color (colors/theme-colors colors/neutral-100 colors/white theme)
          :size  20}]])
     [rn/view {:style (style/text hide-pin?)}
      [text/text
       {:number-of-lines 1
        :size            :paragraph-2}
       latest-pin-text]]
     [rn/view
      {:accessibility-label :pins-count
       :style               style/counter}
      [counter/view {:type :secondary} pins-count]]]))

(def view (quo.theme/with-theme view-internal))
