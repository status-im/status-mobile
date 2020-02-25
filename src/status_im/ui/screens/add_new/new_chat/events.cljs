(ns status-im.ui.screens.add-new.new-chat.events
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.ethereum.core :as ethereum]
            [status-im.ethereum.ens :as ens]
            [status-im.ethereum.resolver :as resolver]
            [status-im.ui.screens.add-new.new-chat.db :as db]
            [status-im.utils.handlers :as handlers]
            [status-im.ethereum.stateofus :as stateofus]
            [status-im.utils.random :as random]
            [status-im.utils.utils :as utils]))

(defn- ens-name-parse [contact-identity]
  (when (string? contact-identity)
    (string/lower-case
     (if (ens/is-valid-eth-name? contact-identity)
       contact-identity
       (stateofus/subdomain contact-identity)))))

(re-frame/reg-fx
 :resolve-public-key
 (fn [{:keys [chain contact-identity cb]}]
   (let [registry (get ens/ens-registries chain)
         ens-name (ens-name-parse contact-identity)]
     (resolver/pubkey registry ens-name cb))))

;;NOTE we want to handle only last resolve
(def resolve-last-id (atom nil))

(handlers/register-handler-fx
 :new-chat/set-new-identity
 (fn [{db :db} [_ new-identity-raw new-ens-name id]]
   (when (or (not id) (= id @resolve-last-id))
     (let [new-identity   (utils/safe-trim new-identity-raw)
           is-public-key? (and (string? new-identity)
                               (string/starts-with? new-identity "0x"))
           is-ens?        (and (not is-public-key?)
                               (ens/valid-eth-name-prefix? new-identity))
           error          (db/validate-pub-key db new-identity)]
       (merge {:db (assoc db
                          :contacts/new-identity
                          {:public-key new-identity
                           :state      (cond is-ens?
                                             :searching
                                             (and (string/blank? new-identity) (not new-ens-name))
                                             :empty
                                             error
                                             :error
                                             :else
                                             :valid)
                           :error      error
                           :ens-name   (ens-name-parse new-ens-name)})}
              (when is-ens?
                (reset! resolve-last-id (random/id))
                (let [chain (ethereum/chain-keyword db)]
                  {:resolve-public-key
                   {:chain            chain
                    :contact-identity new-identity
                    :cb               #(re-frame/dispatch [:new-chat/set-new-identity
                                                           %
                                                           new-identity
                                                           @resolve-last-id])}})))))))

(handlers/register-handler-fx
 ::new-chat-focus
 (fn [{:keys [db]}]
   {:db (dissoc db :contacts/new-identity)}))
