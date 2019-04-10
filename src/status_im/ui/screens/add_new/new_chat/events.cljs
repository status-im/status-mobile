(ns status-im.ui.screens.add-new.new-chat.events
  (:require [re-frame.core :as re-frame]
            [status-im.ui.screens.add-new.new-chat.db :as db]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.ethereum.ens :as ens]
            [status-im.utils.ethereum.resolver :as resolver]
            [status-im.utils.handlers :as handlers]
            [clojure.string :as string]))

(re-frame/reg-fx
 :resolve-public-key
 (fn [{:keys [registry ens-name cb]}]
   (resolver/pubkey registry ens-name cb)))

(handlers/register-handler-fx
 :new-chat/set-new-identity
 (fn [{{:keys [network network-status] :as db} :db} [_ new-identity]]
   (let [is-public-key? (and (string? new-identity)
                             (string/starts-with? new-identity "0x"))]
     (merge {:db (assoc db
                        :contacts/new-identity       new-identity
                        :contacts/new-identity-error (db/validate-pub-key db new-identity))}
            (when-not is-public-key?
              (let [network (get-in db [:account/account :networks network])
                    chain   (ethereum/network->chain-keyword network)]
                {:resolve-public-key {:registry (get ens/ens-registries chain)
                                      :ens-name (if (ens/is-valid-eth-name? new-identity)
                                                  new-identity
                                                  (str new-identity ".stateofus.eth"))
                                      :cb #(re-frame/dispatch [:new-chat/set-new-identity %])}}))))))
