(ns quo.components.banners.banner.view
  (:require
    [quo.components.banners.banner.style :as style]
    [quo.components.counter.counter.view :as counter]
    [quo.components.icon :as icons]
    [quo.components.markdown.text :as text]
    [quo.foundations.colors :as colors]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]))

(defn view
  [{:keys [hide-pin? latest-pin-text pins-count on-press]}]
  (let [theme (quo.theme/use-theme)]
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
        [counter/view {:type :secondary} pins-count]]])))
