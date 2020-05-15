(ns status-im.ui.starter-pack.view
  (:require-macros [status-im.utils.views :refer [defview letsubs] :as views])
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.button :as button]
            [status-im.ui.components.colors :as colors]
            [status-im.payments.core :as payment]
            [status-im.ui.starter-pack.events :as sp]
            [status-im.i18n :as i18n]
            [status-im.react-native.resources :as resources]))

;; TODO: Show starter pack only on mainnet?
;; TODO: Show if it is not buyed already

(defview starter-pack []
  (letsubs [starter-pack-state [::sp/starter-pack-state]
            starter-pack-eligible [::sp/starter-pack-eligible]
            starter-pack-amount [::sp/starter-pack-amount]
            can-pay            [::payment/can-make-payment]
            listeners          (reagent/atom nil)
            product (reagent/atom nil)
            price (reagent/cursor product [0 "localizedPrice"])
            product-to-buy "starterpack.0"]
    {:component-did-mount
     (fn []
       (payment/get-product product-to-buy #(reset! product %))
       (reset! listeners (payment/purchase-listeners ::sp/success-buy)))
     :component-will-unmount
     (fn []
       (when @listeners
         (payment/clear-purchase-listeners @listeners)
         (reset! listeners nil)))}
    [react/view
     (prn  (= :visible starter-pack-state)
           starter-pack-eligible
           can-pay
           @price)
     (when (and (= :visible starter-pack-state)
                starter-pack-eligible
                can-pay
                @price)
       [react/view {:style {:padding             8
                            :background-color    colors/blue-light
                            :border-top-width    1
                            :border-bottom-width 1
                            :border-color        (colors/alpha colors/blue 0.1)}}
        [react/view {:style {:background-color "red"
                             :padding 8}}
         [react/text-input {:placeholder    "Payment url"
                            :on-change-text #(re-frame/dispatch [:set-in [:payment-gateway] %])}]]
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
           (i18n/label :t/starter-pack-title)]
          [react/text {:style {:font-size   15
                               :line-height 22}}
           (i18n/label :t/starter-pack-description {:price           @price
                                                    :crypto          (get-in starter-pack-amount [:tokens-amount 0])
                                                    :crypto-currency "SNT"})]]
         [react/view
          [react/touchable-highlight
           {:on-press            #(re-frame/dispatch [::sp/close-starter-pack])
            :style               {:padding 8}
            :accessibility-label :hide-home-button}
           [react/view {:style {:width            24
                                :height           24
                                :border-radius    12
                                :background-color colors/gray
                                :align-items      :center
                                :justify-content  :center}}
            [icons/icon :main-icons/close {:color colors/white}]]]]]
        [react/view {:style {:padding         8
                             :flex-direction  :row
                             :justify-content :center
                             :align-items     :center}}
         [button/button {:on-press #(re-frame/dispatch [::payment/request-payment product-to-buy])
                         :theme    :main-blue
                         :label    (i18n/label :t/buy-starter-pack {:price @price})}]]])]))
