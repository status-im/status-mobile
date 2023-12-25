(ns status-im.contexts.wallet.send.routes.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.contexts.wallet.common.utils :as utils]
    [status-im.contexts.wallet.send.routes.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn routes
  [{:keys [amount from-network to-network status]}]
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
      :status  status}]
    [quo/network-link
     {:shape           :linear
      :source          from-network
      :destination     to-network
      :container-style style/network-link}]
    [quo/network-bridge
     {:amount          amount
      :network         to-network
      :status          status
      :container-style {:right 12}}]]])

(defn view
  [{:keys [amount route networks input-value]}]
  (let [loading-suggested-routes? (rf/sub [:wallet/wallet-send-loading-suggested-routes?])
        from-network              (utils/id->network (get-in route [:From :chainId]))
        to-network                (utils/id->network (get-in route [:To :chainId]))]
    [rn/scroll-view
     {:content-container-style {:flex-grow       1
                                :align-items     :center
                                :justify-content :center}}
     (when (not (empty? input-value))
       (if (and (not loading-suggested-routes?) route)
         [routes
          {:amount       amount
           :status       :default
           :from-network from-network
           :to-network   to-network}]
         [routes
          {:status       :loading
           :from-network (:network-name (nth (seq networks) 1))
           :to-network   (:network-name (nth (seq networks) 1))}]))]))
