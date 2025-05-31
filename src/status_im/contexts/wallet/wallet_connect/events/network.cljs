(ns status-im.contexts.wallet.wallet-connect.events.network
  (:require [status-im.contexts.wallet.networks.db :as networks.db]
            [status-im.contexts.wallet.wallet-connect.modals.change-network.view :as
             change-network-modal]
            [status-im.contexts.wallet.wallet-connect.utils.data-store :as
             data-store]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(rf/reg-event-fx :wallet-connect/show-activate-request-network-sheet
 (fn [_ [{:keys [on-success]}]]
   {:fx [[:dispatch
          [:show-bottom-sheet
           {:content (fn []
                       [change-network-modal/view
                        {:on-success on-success}])}]]]}))

(rf/reg-event-fx :wallet-connect/activate-request-network
 (fn [{:keys [db]} [{:keys [deactivate-chain-id]}]]
   (let [event                 (data-store/get-db-current-request-event db)
         chain-id              (data-store/get-request-chain-id event)
         activate-network-name (networks.db/get-network-name db chain-id)]
     {:fx [(if deactivate-chain-id
             [:dispatch
              [:wallet/deactivate-and-activate-another-network
               {:activate-chain-id   chain-id
                :deactivate-chain-id deactivate-chain-id
                :on-success          #(rf/dispatch [:toasts/upsert
                                                    {:type :positive
                                                     :text (i18n/label
                                                            :t/network-activated-and-deactivated
                                                            {:network-activated activate-network-name
                                                             :network-deactivated
                                                             (networks.db/get-network-name
                                                              db
                                                              deactivate-chain-id)})}])}]]
             [:dispatch
              [:wallet/toggle-network-active chain-id
               #(rf/dispatch [:toasts/upsert
                              {:type :positive
                               :text (i18n/label :t/network-activated
                                                 {:network activate-network-name})}])]])]})))
