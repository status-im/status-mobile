(ns quo2.components.info.network-breakdown
  (:require [quo2.components.icon :as icons]
            [quo.react-native :as rn]
            [quo2.foundations.colors :as colors]
            [quo2.components.markdown.text :as text]
            [status-im.i18n.i18n :as i18n]))

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
                                     colors/black
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
         [rn/view {:style {:flex-direction :row}
                   :key   idx}
          [rn/view
           [rn/view {:style {:flex-direction  :row
                             :align-items     :center
                             :justify-content :space-between}}
            [rn/view {:flex-direction :row
                      :align-items    :center}
             [text/text {:size   :paragraph-2
                         :weight :medium
                         :style  {:color (colors/theme-colors
                                          colors/black
                                          colors/white)}}
              [icons/icon icon
               {:no-color        true
                :size            40
                :container-style {:width  12
                                  :height 12}}]
              (str " " conversion)]]
            (when-not (= last-item-idx
                         idx)
              [rn/view {:style {:border-right-width 1
                                :margin-horizontal  8
                                :border-right-color (colors/theme-colors
                                                     colors/neutral-40
                                                     colors/neutral-50)
                                :height             "45%"}}])]
           [text/text {:weight :medium
                       :size   :label
                       :color  (colors/theme-colors
                                colors/neutral-40
                                colors/neutral-50)
                       :style  {:margin-left 12
                                :margin-top  -3}}
            (str " " (-> :t/on-network
                         (i18n/label {:network network})))]]])
       network-conversions))]])