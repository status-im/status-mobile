(ns status-im.commands.core
  (:require
   [re-frame.core :as re-frame]
   [status-im.ethereum.core :as ethereum]
   [status-im.utils.config :as config]
   [status-im.ethereum.json-rpc :as json-rpc]
   [status-im.utils.fx :as fx]))

(fx/defn handle-prepare-accept-request-address-for-transaction
  {:events [::prepare-accept-request-address-for-transaction]}
  [{:keys [db]} message]
  {:db (assoc db
              :commands/select-account
              {:message message
               :from (ethereum/get-default-account (:multiaccount/accounts db))})})

(fx/defn set-selected-account
  {:events [::set-selected-account]}
  [{:keys [db]} _ account]
  {:db (-> (assoc-in db [:commands/select-account :from] account)
           (assoc :bottom-sheet/show? false))})

(fx/defn handle-accept-request-address-for-transaction
  {:events [::accept-request-address-for-transaction]}
  [{:keys [db]} message-id address]
  {:db (dissoc db :commands/select-account)
   ::json-rpc/call [{:method (if config/waku-enabled?
                               "wakuext_acceptRequestAddressForTransaction"
                               "shhext_acceptRequestAddressForTransaction")
                     :params [message-id address]
                     :on-success #(re-frame/dispatch [:transport/message-sent % 1])}]})

(fx/defn handle-decline-request-address-for-transaction
  {:events [::decline-request-address-for-transaction]}
  [cofx message-id]
  {::json-rpc/call [{:method (if config/waku-enabled?
                               "wakuext_declineRequestAddressForTransaction"
                               "shhext_declineRequestAddressForTransaction")
                     :params [message-id]
                     :on-success #(re-frame/dispatch [:transport/message-sent % 1])}]})

(fx/defn handle-decline-request-transaction
  {:events [::decline-request-transaction]}
  [cofx message-id]
  {::json-rpc/call [{:method (if config/waku-enabled?
                               "wakuext_declineRequestTransaction"
                               "shhext_declineRequestTransaction")
                     :params [message-id]
                     :on-success #(re-frame/dispatch [:transport/message-sent % 1])}]})
