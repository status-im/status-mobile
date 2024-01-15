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
  [{:keys [amount routes loading-networks]}]
  (let [loading-suggested-routes? (rf/sub [:wallet/wallet-send-loading-suggested-routes?])
        candidates                (:candidates routes)]
    (if (or (and (not-empty loading-networks) loading-suggested-routes?) (not-empty candidates))
      [rn/flat-list
       {:data                    (if loading-suggested-routes? loading-networks candidates)
        :content-container-style style/routes-container
        :header                  [rn/view {:style style/routes-header-container}
                                  [quo/section-label
                                   {:section         (i18n/label :t/from-label)
                                    :container-style (style/section-label 0)}]
                                  [quo/section-label
                                   {:section         (i18n/label :t/to-label)
                                    :container-style (style/section-label 64)}]]
        :render-fn               (fn [item]
                                   [route-item
                                    {:amount       amount
                                     :status       (if loading-suggested-routes? :loading :default)
                                     :from-network (if loading-suggested-routes?
                                                     (utils/id->network item)
                                                     (utils/id->network (get-in item [:from :chain-id])))
                                     :to-network   (if loading-suggested-routes?
                                                     (utils/id->network item)
                                                     (utils/id->network (get-in item
                                                                                [:to :chain-id])))}])}]
      [rn/view {:style style/empty-container}
       (when (and (not (nil? candidates)) (not loading-suggested-routes?))
         [quo/text (i18n/label :t/no-routes-found)])])))
