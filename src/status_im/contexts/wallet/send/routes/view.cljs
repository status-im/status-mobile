(ns status-im.contexts.wallet.send.routes.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.contexts.wallet.common.utils :as utils]
    [status-im.contexts.wallet.send.routes.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn loaded-routes
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

(defn view
  [{:keys [amount route]}]
  (let [loading-suggested-routes? (rf/sub [:wallet/wallet-send-loading-suggested-routes?])
        from-network              (utils/id->network (get-in route [:From :chainId]))
        to-network                (utils/id->network (get-in route [:To :chainId]))]
    [rn/scroll-view
     {:content-container-style {:flex-grow       1
                                :align-items     :center
                                :justify-content :center}}
     (cond loading-suggested-routes?
           [quo/text "Loading routes"]
           (and (not loading-suggested-routes?) route)
           [loaded-routes
            {:amount       amount
             :from-network from-network
             :to-network   to-network}]
           (and (not loading-suggested-routes?) (nil? route))
           [quo/text "Route not found"])]))
