(ns status-im.contexts.wallet.send.save-address.view
  (:require
   [quo.core :as quo]
   [re-frame.core :as rf]
   [react-native.core :as rn]
   [status-im.contexts.wallet.send.save-address.style :as style]))

(defn view []
  [:<>
   [quo/gradient-cover {:customization-color :blue
                        :style style/absolutify}]
   [rn/view {:style style/absolutify}
    [quo/page-nav
     {:type                :no-title
      :background          :blur
      :icon-name           :i/close
      :on-press            #(rf/dispatch [:navigate-back])
      :accessibility-label :save-address-top-bar}]]
   ])
