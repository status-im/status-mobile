(ns status-im.add-new.core
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.add-new.db :as db]
            [status-im2.contexts.chat.events :as chat]
            [status-im2.contexts.contacts.events :as contact]
            [status-im.ethereum.core :as ethereum]
            [status-im.ethereum.ens :as ens]
            [status-im.ethereum.stateofus :as stateofus]
            [utils.i18n :as i18n]
            [status-im.router.core :as router]
            [status-im.utils.db :as utils.db]
            [utils.re-frame :as rf]
            [status-im.utils.random :as random]
            [status-im.utils.utils :as utils]
            [status-im2.navigation.events :as navigation]))

(re-frame/reg-fx
 :resolve-public-key
 (fn [{:keys [chain-id contact-identity cb]}]
   (let [ens-name (stateofus/ens-name-parse contact-identity)]
     (ens/pubkey chain-id ens-name cb))))

;;NOTE we want to handle only last resolve
(def resolve-last-id (atom nil))

(rf/defn new-chat-set-new-identity
  {:events [:new-chat/set-new-identity]}
  [{db :db} new-identity-raw new-ens-name id]
  (let [ens-error (and (= new-identity-raw "0x") (not (string/blank? new-ens-name)))]
    (when (or (not id) (= id @resolve-last-id))
      (if ens-error
        {:db (assoc-in db [:contacts/new-identity :state] :error)}
        (let [new-identity   (utils/safe-trim new-identity-raw)
              is-public-key? (and (string? new-identity)
                                  (utils.db/valid-public-key? new-identity))
              is-ens?        (and (not is-public-key?)
                                  (ens/valid-eth-name-prefix? new-identity))
              error          (db/validate-pub-key db new-identity)]
          (reset! resolve-last-id nil)
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
                              :ens-name   (stateofus/ens-name-parse new-ens-name)})}
                 (when is-ens?
                   (reset! resolve-last-id (random/id))
                   {:resolve-public-key
                    {:chain-id         (ethereum/chain-id db)
                     :contact-identity new-identity
                     :cb               #(re-frame/dispatch [:new-chat/set-new-identity
                                                            %
                                                            new-identity
                                                            @resolve-last-id])}})))))))

(rf/defn clear-new-identity
  {:events [::clear-new-identity ::new-chat-focus]}
  [{:keys [db]}]
  {:db (dissoc db :contacts/new-identity)})

(rf/defn qr-code-handled
  {:events [::qr-code-handled]}
  [{:keys [db] :as cofx} {:keys [type public-key chat-id data ens-name]}
   {:keys [new-contact? nickname] :as opts}]
  (let [public-key?       (and (string? data)
                               (string/starts-with? data "0x"))
        chat-key          (cond
                            (= type :private-chat) chat-id
                            (= type :contact) public-key
                            (and (= type :undefined)
                                 public-key?)
                            data)
        validation-result (db/validate-pub-key db chat-key)]
    (if-not validation-result
      (if new-contact?
        (rf/merge cofx
                  (contact/add-contact chat-key nickname ens-name)
                  (navigation/navigate-to-cofx :contacts-list {}))
        (chat/start-chat cofx chat-key ens-name))
      {:utils/show-popup {:title      (i18n/label :t/unable-to-read-this-code)
                          :content    (case validation-result
                                        :invalid
                                        (i18n/label :t/use-valid-contact-code)
                                        :yourself
                                        (i18n/label :t/can-not-add-yourself))
                          :on-dismiss #(re-frame/dispatch [:pop-to-root :shell-stack])}})))

(rf/defn qr-code-scanned
  {:events [:contact/qr-code-scanned]}
  [{:keys [db]} data opts]
  {::router/handle-uri {:chain (ethereum/chain-keyword db)
                        :chats (get db :chats)
                        :uri   data
                        :cb    #(re-frame/dispatch [::qr-code-handled % opts])}})
