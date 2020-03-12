(ns status-im.ui.starter-pack.popover
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.button :as button]
            [status-im.ui.screens.home.styles :as styles]
            [status-im.ui.components.colors :as colors]
            [status-im.payments.core :as payment]
            [status-im.ui.starter-pack.events :as sp]
            [status-im.ui.components.list.views :as list]
            [status-im.ethereum.tokens :as tokens]
            [status-im.react-native.resources :as resources]))

(defn success-popover []
  [react/view
   [react/view {:style {}}
    [react/image {:source (resources/get-image :starter-pack)
                  :style  {:width  76
                           :height 80}}]]
   [react/view {:style {:padding 8
                        :flex    1}}
    [react/text {:style {:line-height 24
                         :font-size   17
                         :font-weight "500"}}
     "You purchased the Crypto Starter Pack"]
    [react/text {:style {:font-size   15
                         :line-height 22}}
     "Once the transaction is complete, Status account will be credited with the purchased assets. This might take a minute."]]
   [react/view {:style {:border-radius      8
                        :border-width       1
                        :border-color       "#EEF2F5"
                        :padding-vertical   8
                        :padding-horizontal 12}}
    [react/view {:style {:flex-direction  :row
                         :align-items     :center
                         :justify-content :space-between}}
     [react/view {:style {:flex-direction   :row
                          :padding-vertical 4}}
      [react/image {:source (resources/get-image :SNT-asset)
                    :style  {:width        20
                             :height       20
                             :margin-right 6}}]
      [react/text {:style {:font-size 13}} "SNT"]]
     [react/text {:style {:font-size 13}} "300"]]
    [react/view {:style {:flex-direction  :row
                         :justify-content :space-between}}
     [react/view {:style {:flex-direction :row}}
      [react/image {:source (resources/get-image :ETH-asset)
                    :style  {:width        20
                             :height       20
                             :margin-right 6}}]
      [react/text {:style {:font-size 13}} "ETH"]]
     [react/text {:style {:font-size 13}} "0.0014"]]
    [react/view {:style {:flex-direction  :row
                         :justify-content :space-between}}
     [react/view {:style {:flex-direction :row}}
      [react/image {:source (resources/get-image :tozemoon-asset)
                    :style  {:width        20
                             :height       20
                             :margin-right 6}}]
      [react/text {:style {:font-size 13}} "Tozemoon"]]
     [react/text {:style {:font-size 13}} "20 stickers"]]]
   [react/view
    [button/button {:on-press #(re-frame/dispatch [:hide-popover])
                    :label    :t/ok-got-it}]]])
