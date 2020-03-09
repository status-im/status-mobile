(ns status-im.ui.starter-pack.view
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.home.styles :as styles]
            [status-im.ui.components.colors :as colors]
            [status-im.payments.core :as payment]
            [status-im.ui.starter-pack.events :as sp]
            [status-im.react-native.resources :as resources]))

(defn starter-pack []
  (fn []
    (let [starter-pack-state (re-frame/subscribe [::sp/starter-pack-state])]
      (when (= :visible @starter-pack-state)
        [react/view {:style {:padding             8
                             :background-color    "#ECEFFC"
                             :border-top-width    1
                             :border-bottom-width 1
                             :border-color        "rgba(67, 96, 223, 0.1)"}}
         [react/view {:style {:flex-direction :row}}
          [react/view {:style {}}
           [react/image {:source (resources/get-image :starter-pack)
                         :style  {:width  76
                                  :height 80}}]]
          [react/view {:style {:padding 8
                               :flex    1}}
           [react/text {:style {:line-height 24
                                :font-size   17
                                :font-weight "500"}}
            "Crypto Starter Pack"]
           [react/text {:style {:font-size   15
                                :line-height 22}}
            "Spend $5 and get the Tozemoon Sticker Pack and 300 SNT!"]]
          [react/view
           [react/touchable-highlight
            {:on-press            #(re-frame/dispatch [::sp/close-starter-pack])
             :style               {:padding 8}
             :accessibility-label :hide-home-button}
            [react/view {:style styles/close-icon-container}
             [icons/icon :main-icons/close {:color colors/white}]]]]]
         [react/view {:style {:padding 8}}

          [react/touchable-highlight {:on-press #(re-frame/dispatch [::payment/request-payment])}
           [react/view {:style {:background-color "black"
                                :height           44
                                :border-radius    8
                                :align-items      :center
                                :justify-content  :center
                                :flex             1}}
            [react/text {:style {:color "white"}}
             "Here PAY button"]]]]]))))
