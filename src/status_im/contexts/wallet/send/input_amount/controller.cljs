(ns status-im.contexts.wallet.send.input-amount.controller
  (:require
    [utils.re-frame :as rf]))

(rf/reg-sub
 :layers/ui
 :<- [:layers]
 :-> :ui)

(rf/reg-sub
 :ui/send
 :<- [:layers/ui]
 :-> :send)

(rf/reg-sub
 :ui/send-input-amount-screen
 :<- [:ui/send]
 :-> :input-amount-screen)

(rf/reg-sub
 :controller/send-input-amount-screen
 :<- [:ui/send-input-amount-screen]
 :-> :controller)

(rf/reg-sub
 :screen-data/send-input-amount-screen
 :<- [:controller/send-input-amount-screen]
 (fn [controller]
   {:crypto-currency? (:crypto-currency? controller)}))

(rf/reg-event-fx
 :send-input-amount-screen/swap-between-fiat-and-crypto
 (fn [{:keys [db]}]
   {:db (update-in db [:layers :ui :send :input-amount-screen :controller :crypto-currency?] not)}))

(comment
  (rf/dispatch [:send-input-amount-screen/swap-between-fiat-and-crypto])
  (rf/sub [:screen-data/send-input-amount-screen])
  (rf/sub [:controller/send-input-amount-screen])
)

#_(rf/reg-sub
   :wallet/wallet-send-enough-assets?
   :<- [:wallet/wallet-send]
   :-> :enough-assets?)

#_(rf/reg-sub
   :wallet/wallet-send-token
   :<- [:wallet/wallet-send]
   :<- [:wallet/network-details]
   :<- [:wallet/wallet-send-disabled-from-chain-ids]
   (fn [[wallet-send networks disabled-from-chain-ids]]
     (let [token                    (:token wallet-send)
           disabled-from-chain-ids? (set disabled-from-chain-ids)
           enabled-from-chain-ids   (->> networks
                                         (map :chain-id)
                                         (remove disabled-from-chain-ids?)
                                         set)]
       (some-> token
               (assoc :networks          (network-utils/network-list token networks)
                      :available-balance (utils/calculate-total-token-balance token)
                      :total-balance     (utils/calculate-total-token-balance
                                          token
                                          enabled-from-chain-ids))))))
