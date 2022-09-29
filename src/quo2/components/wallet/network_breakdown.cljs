(ns quo2.components.wallet.network-breakdown
  (:require [quo.react-native :as rn]
            [quo2.foundations.colors :as colors]
            [quo2.components.markdown.text :as text]
            [quo2.components.wallet.network-amount :refer [network-amount]]))

(defn network-breakdown
  [{:keys [top-value network-conversions]}]
  [rn/view {:style {:background-color   (colors/theme-colors
                                         colors/white
                                         colors/neutral-95)
                    :padding-horizontal 40
                    :padding-top        24
                    :border-radius      16
                    :height             126
                    :width              390
                    :overflow           :hidden}}
   [rn/view {:style {:border-bottom-width 1
                     :border-bottom-color (colors/theme-colors
                                           colors/neutral-20
                                           colors/neutral-70)
                     :padding-vertical    6}}
    [text/text {:weight :medium
                :style  {:color     (colors/theme-colors
                                     colors/neutral-100
                                     colors/white)
                         :font-size 19}}
     (str top-value)]]
   [rn/scroll-view {:horizontal true
                    :style      {:text-align :center
                                 :margin-top 6
                                 :width      340}}
    (let [last-item-idx (-> network-conversions
                            count
                            dec)]
      (map-indexed
       (fn [idx {:keys [conversion network icon]}]
         [rn/view {:style (cond-> {:flex-direction :row}
                            (not= idx 0) (assoc :margin-left -4))
                   :key   idx}
          [network-amount {:show-right-border? (not= idx last-item-idx)
                           :icon               icon
                           :network-name       network
                           :eth-value          conversion}]])
       network-conversions))]])
