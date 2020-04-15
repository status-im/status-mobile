(ns status-im.ui.starter-pack.popover
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.button :as button]
            [status-im.i18n :as i18n]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.starter-pack.events :as sp]
            [status-im.react-native.resources :as resources]))

(defn perk [{:keys [image name value]}]
  [react/view {:style {:flex-direction   :row
                       :align-items      :center
                       :padding-vertical 4
                       :justify-content  :space-between}}
   [react/view {:style {:flex-direction :row}}
    [react/image {:source (resources/get-image image)
                  :style  {:width        20
                           :height       20
                           :margin-right 6}}]
    [react/text {:style {:font-size 13}} name]]
   [react/text {:style {:font-size 13}} (str value)]])

(defn success-popover []
  (fn []
    (let [starter-pack-amount @(re-frame/subscribe [::sp/starter-pack-amount])]
      [react/view
       [react/view {:style {:align-items        :center
                            :padding-vertical   8
                            :padding-horizontal 16}}
        [react/view
         [react/image {:source (resources/get-image :starter-pack)
                       :style  {:width  76
                                :height 80}}]]
        [react/view {:style {:padding 8}}
         [react/text {:style {:line-height   24
                              :typography    :title-bold
                              :text-align    :center
                              :margin-bottom 8}}
          (i18n/label :t/starter-pack-success-title)]
         [react/text {:style {:font-size   15
                              :text-align  :center
                              :line-height 22}}
          (i18n/label :t/starter-pack-success-description)]]
        [react/view {:style {:border-radius      8
                             :border-width       1
                             :border-color       colors/gray-lighter
                             :width              "100%"
                             :padding-vertical   8
                             :padding-horizontal 12}}
         [perk {:image :SNT-asset
                :name  "SNT"
                :value (get-in starter-pack-amount [:tokens-amount 0])}]
         [perk {:image :ETH-asset
                :name  "ETH"
                :value (get starter-pack-amount :eth-amount)}]
         [perk {:image :tozemoon-asset
                :name  "Tozemoon"
                :value "20 stickers"}]]
        [react/view {:style {:margin-vertical 8}}
         [button/button {:on-press #(re-frame/dispatch [:hide-popover])
                         :label    :t/ok-got-it}]]]])))
