(ns status-im.contexts.wallet.send.routes.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.contexts.wallet.common.utils :as utils]
    [status-im.contexts.wallet.send.routes.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn route-item
  [{:keys [amount from-network to-network status]}]
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
     :container-style {:right 12}}]])

(defn view
  [{:keys [amount routes]}]
  (let [loading-suggested-routes? (rf/sub [:wallet/wallet-send-loading-suggested-routes?])
        candidates                (:candidates routes)]
    (if (and (not loading-suggested-routes?) (not-empty candidates))
      [rn/flat-list
       {:data                    candidates
        :content-container-style style/routes-container
        :header                  [rn/view {:style style/routes-header-container}
                                  [quo/section-label
                                   {:section         (i18n/label :t/from-label)
                                    :container-style (style/section-label 0)}]
                                  [quo/section-label
                                   {:section         (i18n/label :t/to-label)
                                    :container-style (style/section-label 64)}]]
        :render-fn               (fn [route]
                                   [route-item
                                    {:amount       amount
                                     :status       :default
                                     :from-network (utils/id->network (get-in route [:from :chain-id]))
                                     :to-network   (utils/id->network (get-in route [:to :chain-id]))}])}]
      [rn/view {:style style/empty-container}
       (if loading-suggested-routes?
         [rn/activity-indicator]
         (when (not (nil? candidates))
           [quo/text (i18n/label :t/no-routes-found)]))])))
