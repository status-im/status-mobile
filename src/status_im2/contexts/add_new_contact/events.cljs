(ns status-im2.contexts.add-new-contact.events
  (:require [utils.re-frame :as rf]
            [status-im.utils.types :as types]
            [re-frame.core :as re-frame]
            [status-im.ethereum.core :as ethereum]
            [status-im.ethereum.ens :as ens]
            [status-im.ethereum.stateofus :as stateofus]
            [status-im.native-module.core :as status]
            [status-im2.utils.validators :as validators]
            [status-im.utils.utils :as utils]))

(re-frame/reg-fx
 :contacts/resolve-public-key-from-ens-name
 (fn [{:keys [chain-id ens-name on-success on-error]}]
   (ens/pubkey chain-id ens-name on-success on-error)))

(re-frame/reg-fx
 :contacts/decompress-public-key
 (fn [{:keys [public-key on-success on-error]}]
   (status/compressed-key->public-key
    public-key
    (fn [resp]
      (let [{:keys [error]} (types/json->clj resp)]
        (if error
          (on-error error)
          (on-success
           (str "0x" (subs resp 5)))))))))

(rf/defn set-new-identity
  {:events [:contacts/set-new-identity]}
  [{:keys [db]} input]
  (let [input           (utils/safe-trim input)
        public-key?     (validators/valid-public-key? input)
        compressed-key? (validators/valid-compressed-key? input)
        ens-name        (if compressed-key? nil (stateofus/ens-name-parse input))
        on-success      (fn [pubkey]
                          (rf/dispatch
                           [:contacts/set-new-identity-success
                            input ens-name pubkey]))
        on-error        (fn [err]
                          (rf/dispatch
                           [:contacts/set-new-identity-error err input]))]
    (cond
      (empty? input) {:db (dissoc db :contacts/new-identity)}
      public-key?    {:db (assoc db
                                 :contacts/new-identity
                                 {:input      input
                                  :public-key input
                                  :ens-name   nil
                                  :state      :error
                                  :error      :uncompressed-key})}
      :else
      (cond->
        {:db (assoc db
                    :contacts/new-identity
                    {:input      input
                     :public-key nil
                     :ens-name   nil
                     :state      :searching
                     :error      nil})}
        compressed-key?       (assoc :contacts/decompress-public-key
                                     {:public-key input
                                      :on-success on-success
                                      :on-error   on-error})
        (not compressed-key?) (assoc :contacts/resolve-public-key-from-ens-name
                                     {:chain-id   (ethereum/chain-id db)
                                      :ens-name   ens-name
                                      :on-success on-success
                                      :on-error   on-error})))))

(rf/defn set-new-identity-success
  {:events [:contacts/set-new-identity-success]}
  [{:keys [db]} input ens-name pubkey]
  {:db (assoc db
              :contacts/new-identity
              {:input      input
               :public-key pubkey
               :ens-name   ens-name
               :state      :valid
               :error      nil})})

(rf/defn set-new-identity-error
  {:events [:contacts/set-new-identity-error]}
  [{:keys [db]} error input]
  {:db (assoc db
              :contacts/new-identity
              {:input      input
               :public-key nil
               :ens-name   nil
               :state      :error
               :error      :invalid})})

(rf/defn clear-new-identity
  {:events [:contacts/clear-new-identity :contacts/new-chat-focus]}
  [{:keys [db]}]
  {:db (dissoc db :contacts/new-identity)})
