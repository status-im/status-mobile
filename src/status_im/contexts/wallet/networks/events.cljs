(ns status-im.contexts.wallet.networks.events
  (:require [re-frame.core :as rf]
            [status-im.contexts.profile.db :as profile.db]
            [status-im.contexts.settings.wallet.network-settings.max-active-networks-sheet :as
             max-active-networks-sheet]
            [status-im.contexts.wallet.networks.db :as networks.db]
            [status-im.contexts.wallet.networks.effects]
            [status-im.contexts.wallet.networks.filter :as networks.filter]
            [status-im.contexts.wallet.rpc-data-store.networks :as rpc-data-store.networks]
            [utils.debounce :as debounce]))

(rf/reg-event-fx
 :wallet/get-networks
 (fn [_]
   {:fx [[:effects.wallet/get-networks
          {:on-success [:wallet/get-networks-success]
           :on-error   [:wallet/log-rpc-error {:event :wallet/get-networks}]}]]}))

(rf/reg-event-fx
 :wallet/get-networks-success
 (fn [{:keys [db]} [data]]
   (let [networks (rpc-data-store.networks/rpc->networks data)]
     {:db (-> db
              (assoc-in [:wallet :networks/chain-ids-by-mode]
                        (rpc-data-store.networks/network-chain-ids networks))
              (assoc-in [:wallet :networks/by-id]
                        (rpc-data-store.networks/networks-by-id networks)))
      :fx [[:dispatch [:wallet/on-networks-loaded]]]})))

(rf/reg-event-fx
 :wallet/on-networks-loaded
 (fn [_]
   {:fx [[:dispatch [:wallet.tokens/get-token-list]]
         [:dispatch [:wallet/check-new-networks-seen]]]}))

(rf/reg-event-fx :wallet/toggle-network-active
 (fn [{:keys [db]} [chain-id on-success]]
   (let [{:keys [active? deactivatable?]} (networks.db/get-network-details db chain-id)
         max-active-reached?              (networks.db/max-active-networks-reached? db)
         should-activate?                 (not active?)]
     (if (and should-activate? max-active-reached?)
       {:fx [[:dispatch
              [:show-bottom-sheet
               {:content max-active-networks-sheet/view
                :theme   :dark
                :shell?  true}]]]}
       (when deactivatable?
         {:fx [[:dispatch
                [:wallet/update-network-active chain-id should-activate?]]
               [:effects.wallet/set-network-active
                {:chain-id   chain-id
                 :active?    should-activate?
                 :on-success #(do (debounce/debounce-and-dispatch
                                   [:wallet/on-active-networks-change]
                                   500)
                                  (when on-success (on-success)))
                 :on-error   #(rf/dispatch
                               [:wallet/update-network-active chain-id active?])}]]})))))

(rf/reg-event-fx :wallet/deactivate-and-activate-another-network
 (fn [_ [{:keys [activate-chain-id deactivate-chain-id on-success]}]]
   {:fx [[:effects.wallet/deactivate-and-activate-another-network
          {:activate-chain-id   activate-chain-id
           :deactivate-chain-id deactivate-chain-id
           :on-success          (fn []
                                  (rf/dispatch [:wallet/update-network-active deactivate-chain-id false])
                                  (rf/dispatch [:wallet/update-network-active activate-chain-id true])
                                  (rf/dispatch [:wallet/on-active-networks-change])
                                  (when on-success (on-success)))}]]}))

(rf/reg-event-fx :wallet/on-active-networks-change
 (fn [_]
   {:fx [[:dispatch [:wallet/reset-accounts-tokens]]
         [:dispatch [:wallet/reload-cached-balances]]
         [:dispatch [:wallet/reload-collectibles]]
         [:dispatch [:wallet/check-new-networks-seen]]]}))

(rf/reg-event-fx :wallet/update-network-active
 (fn [{:keys [db]} [chain-id active?]]
   {:db (assoc-in db
         [:wallet :networks/by-id chain-id :active?]
         active?)}))

(rf/reg-event-fx
 :wallet/navigate-to-chain-explorer
 (fn [{:keys [db]} [chain-id address]]
   (let [explorer-link (networks.db/get-block-explorer-address-url db chain-id address)]
     {:fx [[:dispatch [:hide-bottom-sheet]]
           [:dispatch [:browser.ui/open-url explorer-link]]]})))

(rf/reg-event-fx
 :wallet/toggle-testnet-mode
 (fn [{:keys [db]}]
   (let [testnet? (profile.db/testnet? db)]
     {:fx [[:dispatch
            [:profile.settings/profile-update :test-networks-enabled? (not testnet?)
             {:on-success #(rf/dispatch
                            [:wallet/toggle-testnet-mode-success (not testnet?)])}]]]})))

(rf/reg-event-fx
 :wallet/toggle-testnet-mode-success
 (fn [{:keys [db]} [testnet?]]
   {:db (assoc-in db [:profile/profile :test-networks-enabled?] testnet?)
    :fx [[:dispatch [:profile/toggle-testnet-mode-banner]]
         [:dispatch [:wallet/on-active-networks-change]]
         [:dispatch [:wallet.tokens/reset-tokens]]
         [:dispatch [:wallet.tokens/get-token-list]]]}))

(rf/reg-event-fx
 :wallet/mark-new-networks-as-seen
 (fn [{:keys [db]}]
   {:db (assoc-in db [:wallet :ui :networks/new-marked-as-seen?] true)
    :fx [[:effects.wallet/store-new-networks-as-seen
          {:chain-ids (networks.db/get-new-chain-ids db)}]]}))

(rf/reg-event-fx
 :wallet/check-new-networks-seen
 (fn [{:keys [db]}]
   {:fx [[:effects.wallet/new-networks-seen?
          {:chain-ids  (networks.db/get-new-chain-ids db)
           :on-success #(rf/dispatch [:wallet/update-new-networks-seen %])}]]}))

(rf/reg-event-fx
 :wallet/update-new-networks-seen
 (fn [{:keys [db]} [new-networks-seen?]]
   {:db (assoc-in db [:wallet :ui :networks/new-marked-as-seen?] new-networks-seen?)}))

(rf/reg-event-fx
 :wallet/filter-network-balances
 (fn [{:keys [db]} [network-filter]]
   {:db (update-in db
                   [:wallet :ui :networks/network-filter]
                   networks.filter/toggle
                   network-filter)}))

(rf/reg-event-fx
 :wallet/reset-network-balances-filter
 (fn [{:keys [db]}]
   {:db (update-in db [:wallet :ui] dissoc :networks/network-filter)}))
