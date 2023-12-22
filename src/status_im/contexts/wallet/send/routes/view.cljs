(ns status-im.contexts.wallet.send.routes.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.contexts.wallet.send.routes.style :as style]
    [utils.i18n :as i18n]))

(defn view
  [{:keys [amount from-network to-network]}]
  [rn/view {:style style/routes-container}
   [rn/view {:style style/routes-header-container}
    [quo/section-label
     {:section         (i18n/label :t/from-label)
      :container-style (style/section-label 0)}]
    [quo/section-label
     {:section         (i18n/label :t/to-label)
      :container-style (style/section-label 64)}]]
   [rn/view {:style style/routes-inner-container}
    [quo/network-bridge
     {:amount  amount
      :network from-network
      :status  :default}]
    [quo/network-link
     {:shape           :linear
      :source          from-network
      :destination     to-network
      :container-style style/network-link}]
    [quo/network-bridge
     {:amount          amount
      :network         to-network
      :status          :default
      :container-style {:right 12}}]]])
