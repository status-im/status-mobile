(ns status-im.contexts.wallet.send.save-address.view
  (:require
   [quo.core :as quo]
   [re-frame.core :as rf]
   [reagent.core :as reagent]
   [react-native.core :as rn]
   [status-im.contexts.wallet.send.save-address.style :as style]
   [utils.i18n :as i18n]
   ))

(defn view []
  (let [[address-label set-address-label] (rn/use-state "")
        [address-color set-address-color] (rn/use-state :blue)]
    [:<>
     [quo/page-nav
      {:type                :no-title
       :background          :blur
       :icon-name           :i/close
       :on-press            #(rf/dispatch [:navigate-back])
       :accessibility-label :save-address-top-bar}]

     [quo/user-avatar
      {:full-name           address-label
       :customization-color address-color
       :size                :big}]

     [quo/title-input
      {:blur?               true
       :size                :heading-1
       :placeholder         (i18n/label :t/address-name)
       :default-value       address-label
       :on-change-text      set-address-label
       :customization-color address-color}]

     [quo/divider-line {}]
     [quo/color-picker
      {:default-selected address-color
       :on-change        set-address-color
       }]
     [quo/divider-line {}]
     ]))


(comment
  ,)
