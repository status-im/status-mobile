(ns status-im2.contexts.add-new-contact.events
  (:require [utils.re-frame :as rf]
            [status-im.utils.types :as types]
            [re-frame.core :as re-frame]
            [status-im.ethereum.core :as ethereum]
            [status-im.ethereum.ens :as ens]
            [status-im.ethereum.stateofus :as stateofus]
            [status-im.native-module.core :as status]
            [status-im2.navigation.events :as navigation]
            [status-im2.utils.validators :as validators]
            [status-im.utils.utils :as utils]
            [utils.re-frame :as rf]))

(re-frame/reg-fx
 :contacts/decompress-public-key
 (fn [{:keys [public-key on-success on-error]}]
   (status/compressed-key->public-key
    public-key
    (fn [resp]
      (let [{:keys [error]} (types/json->clj resp)]
        (if error
          (on-error error)
          (on-success (str "0x" (subs resp 5)))))))))

(re-frame/reg-fx
 :contacts/resolve-public-key-from-ens-name
 (fn [{:keys [chain-id ens-name on-success on-error]}]
   (ens/pubkey chain-id ens-name on-success on-error)))

(defn fx-callbacks
  [input ens-name]
  {:on-success (fn [pubkey]
                 (rf/dispatch
                  [:contacts/set-new-identity-success
                   input ens-name pubkey]))
   :on-error   (fn [err]
                 (rf/dispatch
                  [:contacts/set-new-identity-error err input]))})

(defn identify-type
  [input]
  (let [regex           #"^https?://join.status.im/u/(.+)"
        id              (as-> (utils/safe-trim input) $
                          (if-some [[_ match] (re-matches regex $)]
                            match
                            $)
                          (if (empty? $) nil $))
        public-key?     (validators/valid-public-key? id)
        compressed-key? (validators/valid-compressed-key? id)
        type            (cond (empty? id)     :empty
                              public-key?     :public-key
                              compressed-key? :compressed-key
                              :else           :ens-name)
        ens-name        (when (= type :ens-name)
                          (stateofus/ens-name-parse id))]
    {:input    input
     :id       id
     :type     type
     :ens-name ens-name}))

(rf/defn set-new-identity
  {:events [:contacts/set-new-identity]}
  [{:keys [db]} input]
  (let [{:keys [input id type ens-name]} (identify-type input)]
    (case type
      :empty          {:db (dissoc db :contacts/new-identity)}
      :public-key     {:db (assoc db
                                  :contacts/new-identity
                                  {:input      input
                                   :public-key id
                                   :state      :error
                                   :error      :uncompressed-key})}
      :compressed-key {:db
                       (assoc db
                              :contacts/new-identity
                              {:input input
                               :state :searching})
                       :contacts/decompress-public-key
                       (merge {:public-key id}
                              (fx-callbacks id ens-name))}
      :ens-name       {:db
                       (assoc db
                              :contacts/new-identity
                              {:input input
                               :state :searching})
                       :contacts/resolve-public-key-from-ens-name
                       (merge {:chain-id (ethereum/chain-id db)
                               :ens-name ens-name}
                              (fx-callbacks id ens-name))})))

(rf/defn set-new-identity-success
  {:events [:contacts/set-new-identity-success]}
  [{:keys [db]} input ens-name pubkey]
  {:db (assoc db
              :contacts/new-identity
              {:input      input
               :public-key pubkey
               :ens-name   ens-name
               :state      :valid})})

(rf/defn set-new-identity-error
  {:events [:contacts/set-new-identity-error]}
  [{:keys [db]} error input]
  {:db (assoc db
              :contacts/new-identity
              {:input input
               :state :error
               :error :invalid})})

(rf/defn clear-new-identity
  {:events [:contacts/clear-new-identity :contacts/new-chat-focus]}
  [{:keys [db]}]
  {:db (dissoc db :contacts/new-identity)})

(rf/defn qr-code-scanned
  {:events [:contacts/qr-code-scanned]}
  [{:keys [db] :as cofx} input]
  (rf/merge cofx
            (set-new-identity input)
            (navigation/navigate-back)))
