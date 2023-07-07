(ns status-im.commands.core
  (:require [re-frame.core :as re-frame]
            [status-im.ethereum.core :as ethereum]
            [utils.re-frame :as rf]))

(rf/defn handle-prepare-accept-request-address-for-transaction
  {:events [::prepare-accept-request-address-for-transaction]}
  [{:keys [db]} message]
  {:db                    (assoc db
                                 :commands/select-account
                                 {:message message
                                  :from    (ethereum/get-default-account (:profile/wallet-accounts db))})
   :show-select-acc-sheet nil})

(rf/defn set-selected-account
  {:events [::set-selected-account]}
  [{:keys [db]} _ account]
  {:db (-> (assoc-in db [:commands/select-account :from] account)
           (assoc :bottom-sheet/show? false))})

(rf/defn handle-accept-request-address-for-transaction
  {:events [::accept-request-address-for-transaction]}
  [{:keys [db]} message-id address]
  {:db            (dissoc db :commands/select-account)
   :json-rpc/call [{:method      "wakuext_acceptRequestAddressForTransaction"
                    :params      [message-id address]
                    :js-response true
                    :on-success  #(re-frame/dispatch [:transport/message-sent %])}]})

(rf/defn handle-decline-request-address-for-transaction
  {:events [::decline-request-address-for-transaction]}
  [_ message-id]
  {:json-rpc/call [{:method      "wakuext_declineRequestAddressForTransaction"
                    :params      [message-id]
                    :js-response true
                    :on-success  #(re-frame/dispatch [:transport/message-sent %])}]})

(rf/defn handle-decline-request-transaction
  {:events [::decline-request-transaction]}
  [cofx message-id]
  {:json-rpc/call [{:method      "wakuext_declineRequestTransaction"
                    :params      [message-id]
                    :js-response true
                    :on-success  #(re-frame/dispatch [:transport/message-sent %])}]})
