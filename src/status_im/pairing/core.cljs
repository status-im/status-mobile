(ns status-im.pairing.core
  (:require [re-frame.core :as re-frame]
            [clojure.string :as string]
            [status-im.i18n :as i18n]
            [status-im.utils.fx :as fx]
            [status-im.contact.device-info :as device-info]
            [status-im.contact.db :as contact.db]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.config :as config]
            [status-im.utils.platform :as utils.platform]
            [status-im.chat.models :as models.chat]
            [status-im.transport.message.public-chat :as transport.public-chat]
            [status-im.accounts.db :as accounts.db]
            [status-im.transport.message.protocol :as protocol]
            [status-im.transport.utils :as transport.utils]
            [status-im.data-store.installations :as data-store.installations]
            [status-im.native-module.core :as native-module]
            [status-im.utils.identicon :as identicon]
            [status-im.contact.core :as contact]
            [status-im.contact-code.core :as contact-code]
            [status-im.data-store.contacts :as data-store.contacts]
            [status-im.data-store.accounts :as data-store.accounts]
            [status-im.transport.message.pairing :as transport.pairing]))

(def contact-batch-n 4)

(defn- parse-response [response-js]
  (-> response-js
      js/JSON.parse
      (js->clj :keywordize-keys true)))

(defn pair-installation [cofx]
  (let [fcm-token         (get-in cofx [:db :notifications :fcm-token])
        installation-name (get-in cofx [:db :account/account :installation-name])
        installation-id (get-in cofx [:db :account/account :installation-id])
        device-type     utils.platform/os]
    (protocol/send (transport.pairing/PairInstallation. installation-id device-type installation-name fcm-token) nil cofx)))

(fx/defn confirm-message-processed
  [{:keys [db]} raw-message]
  {:transport/confirm-messages-processed [{:web3 (:web3 db)
                                           :js-obj raw-message}]})

(defn has-paired-installations? [cofx]
  (->>
   (get-in cofx [:db :pairing/installations])
   vals
   (some :enabled?)))

(defn send-pair-installation [cofx payload]
  (let [{:keys [web3]} (:db cofx)
        current-public-key (accounts.db/current-public-key cofx)]
    {:shh/send-pairing-message {:web3    web3
                                :src     current-public-key
                                :payload payload}}))

(defn merge-contact [local remote]
  ;;TODO we don't sync contact/blocked for now, it requires more complex handling
  (let [remove (update remote :system-tags disj :contact/blocked)
        [old-contact new-contact] (sort-by :last-updated [remote local])]
    (-> local
        (merge new-contact)
        (assoc :device-info (device-info/merge-info (:last-updated new-contact)
                                                    (:device-info old-contact)
                                                    (vals (:device-info new-contact)))
               ;; we only take system tags from the newest contact version
               :system-tags  (:system-tags new-contact)))))

(def merge-contacts (partial merge-with merge-contact))

(def account-mergeable-keys [:name :photo-path :last-updated])

(defn merge-account [local remote]
  (if (> (:last-updated remote) (:last-updated local))
    (merge local (select-keys remote account-mergeable-keys))
    local))

(fx/defn prompt-dismissed [{:keys [db]}]
  {:db (assoc-in db [:pairing/prompt-user-pop-up] false)})

(fx/defn prompt-accepted [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db (assoc-in db [:pairing/prompt-user-pop-up] false)}
            (navigation/navigate-to-cofx :installations nil)))

(fx/defn prompt-user-on-new-installation [{:keys [db]}]
  (when-not config/pairing-popup-disabled?
    {:db               (assoc-in db [:pairing/prompt-user-pop-up] true)
     :ui/show-confirmation {:title      (i18n/label :t/pairing-new-installation-detected-title)
                            :content    (i18n/label :t/pairing-new-installation-detected-content)
                            :confirm-button-text (i18n/label :t/pairing-go-to-installation)
                            :cancel-button-text  (i18n/label :t/cancel)
                            :on-cancel  #(re-frame/dispatch [:pairing.ui/prompt-dismissed])
                            :on-accept #(re-frame/dispatch [:pairing.ui/prompt-accepted])}}))

(fx/defn upsert-installation [{:keys [db] :as cofx} {:keys [installation-id] :as new-installation}]
  (let [success-event [:message/messages-persisted [(or (:dedup-id cofx) (:js-obj cofx))]]
        old-installation (get-in db [:pairing/installations installation-id])
        updated-installation (merge old-installation new-installation)]
    {:db (assoc-in db
                   [:pairing/installations installation-id]
                   updated-installation)
     :data-store/tx [{:transaction (data-store.installations/save updated-installation)
                      :success-event success-event}]}))

(defn handle-bundles-added [{:keys [db] :as cofx} bundle]
  (let [installation-id  (:installationID bundle)
        new-installation {:installation-id installation-id
                          :has-bundle?     true}]
    (when
     (and (= (:identity bundle)
             (accounts.db/current-public-key cofx))
          (not= (get-in db [:account/account :installation-id]) installation-id)
          (not (get-in db [:pairing/installations installation-id])))
      (fx/merge cofx
                (upsert-installation new-installation)
                #(when-not (or (get-in % [:db :pairing/prompt-user-pop-up])
                               (= :installations (:view-id db)))
                   (prompt-user-on-new-installation %))))))

(defn sync-installation-account-message [{:keys [db]}]
  (let [account (-> db
                    :account/account
                    (select-keys account-mergeable-keys))]
    (transport.pairing/SyncInstallation. {} account {})))

(defn- contact-batch->sync-installation-message [batch]
  (let [contacts-to-sync
        (reduce (fn [acc {:keys [public-key system-tags] :as contact}]
                  (assoc acc
                         public-key
                         (cond-> (-> contact
                                     (dissoc :photo-path)
                                     (update :system-tags disj :contact/blocked))
                           ;; for compatibility with version < contact.v7
                           (contact.db/added? contact) (assoc :pending? false)
                           (contact.db/pending? contact) (assoc :pending? true))))
                {}
                batch)]
    (transport.pairing/SyncInstallation. contacts-to-sync {} {})))

(defn- chats->sync-installation-messages [{:keys [db]}]
  (->> db
       :chats
       vals
       (filter :public?)
       (filter :is-active)
       (map #(select-keys % [:chat-id :public?]))
       (map #(transport.pairing/SyncInstallation. {} {} %))))

(defn sync-installation-messages [{:keys [db] :as cofx}]
  (let [contacts (:contacts/contacts db)
        contact-batches (partition-all contact-batch-n (vals contacts))]
    (concat (mapv contact-batch->sync-installation-message contact-batches)

            [(sync-installation-account-message cofx)]
            (chats->sync-installation-messages cofx))))

(fx/defn enable [{:keys [db]} installation-id]
  {:db (assoc-in db
                 [:pairing/installations installation-id :enabled?]
                 true)
   :data-store/tx [(data-store.installations/enable installation-id)]})

(fx/defn disable [{:keys [db]} installation-id]
  {:db (assoc-in db
                 [:pairing/installations installation-id :enabled?]
                 false)
   :data-store/tx [(data-store.installations/disable installation-id)]})

(defn handle-enable-installation-response
  "Callback to dispatch on enable signature response"
  [installation-id response-js]
  (let [{:keys [error]} (parse-response response-js)]
    (if error
      (re-frame/dispatch [:pairing.callback/enable-installation-failed  error])
      (re-frame/dispatch [:pairing.callback/enable-installation-success installation-id]))))

(defn handle-disable-installation-response
  "Callback to dispatch on disable signature response"
  [installation-id response-js]
  (let [{:keys [error]} (parse-response response-js)]
    (if error
      (re-frame/dispatch [:pairing.callback/disable-installation-failed  error])
      (re-frame/dispatch [:pairing.callback/disable-installation-success installation-id]))))

(defn enable-installation! [installation-id]
  (native-module/enable-installation installation-id
                                     (partial handle-enable-installation-response installation-id)))

(defn disable-installation! [installation-id]
  (native-module/disable-installation installation-id
                                      (partial handle-disable-installation-response installation-id)))

(defn enable-fx [cofx installation-id]
  (if (< (count (filter :enabled? (get-in cofx [:db :pairing/installations]))) config/max-installations)
    {:pairing/enable-installation installation-id}
    {:utils/show-popup {:title (i18n/label :t/pairing-maximum-number-reached-title)

                        :content (i18n/label :t/pairing-maximum-number-reached-content)}}))

(defn disable-fx [_ installation-id]
  {:pairing/disable-installation installation-id})

(re-frame/reg-fx
 :pairing/enable-installation
 enable-installation!)

(re-frame/reg-fx
 :pairing/disable-installation
 disable-installation!)

(fx/defn send-sync-installation [cofx payload]
  (let [{:keys [web3]} (:db cofx)
        current-public-key (accounts.db/current-public-key cofx)]
    {:shh/send-direct-message
     [{:web3    web3
       :src     current-public-key
       :dst     current-public-key
       :topics  (get-in cofx [:db :mailserver/topics])
       :payload payload}]}))

(fx/defn send-installation-message-fx [cofx payload]
  (when (has-paired-installations? cofx)
    (protocol/send payload nil cofx)))

(fx/defn sync-public-chat [cofx chat-id]
  (let [sync-message (transport.pairing/SyncInstallation. {} {} {:public? true
                                                                 :chat-id chat-id})]
    (send-installation-message-fx cofx sync-message)))

(fx/defn sync-contact
  [cofx {:keys [public-key] :as contact}]
  (let [sync-message (transport.pairing/SyncInstallation.
                      {public-key (cond-> contact
                                    ;; for compatibility with version < contact.v7
                                    (contact.db/added? contact) (assoc :pending? false)
                                    (contact.db/pending? contact) (assoc :pending? true))}
                      {} {})]
    (send-installation-message-fx cofx sync-message)))

(defn send-installation-messages [cofx]
  ;; The message needs to be broken up in chunks as we hit the whisper size limit
  (let [sync-messages (sync-installation-messages cofx)
        sync-messages-fx (map send-installation-message-fx sync-messages)]
    (apply fx/merge cofx sync-messages-fx)))

(defn ensure-photo-path
  "Make sure a photo path is there, generate otherwise"
  [contacts]
  (reduce-kv (fn [acc k {:keys [public-key photo-path] :as v}]
               (assoc acc k
                      (assoc
                       v
                       :photo-path
                       (if (string/blank? photo-path)
                         (identicon/identicon public-key)
                         photo-path))))
             {}
             contacts))

(defn handle-sync-installation [{:keys [db] :as cofx} {:keys [contacts account chat]} sender]
  (if (= sender (accounts.db/current-public-key cofx))
    (let [success-event [:message/messages-persisted [(or (:dedup-id cofx) (:js-obj cofx))]]
          new-contacts  (vals (merge-contacts (:contacts/contacts db) (ensure-photo-path contacts)))
          new-account   (merge-account (:account/account db) account)
          contacts-fx   (mapv contact/upsert-contact new-contacts)]
      (apply fx/merge
             cofx
             (concat
              [{:db                 (assoc db :account/account new-account)
                :data-store/base-tx [{:transaction   (data-store.accounts/save-account-tx new-account)
                                      :success-event success-event}]}
               #(when (:public? chat)
                  (models.chat/start-public-chat % (:chat-id chat) {:dont-navigate? true}))]
              contacts-fx)))
    (confirm-message-processed cofx (or (:dedup-id cofx)
                                        (:js-obj cofx)))))

(defn handle-pair-installation [{:keys [db] :as cofx} {:keys [name
                                                              fcm-token
                                                              installation-id
                                                              device-type]} timestamp sender]
  (if (and (= sender (accounts.db/current-public-key cofx))
           (not= (get-in db [:account/account :installation-id]) installation-id))
    (let [installation {:installation-id   installation-id
                        :name              name
                        :fcm-token         fcm-token
                        :device-type       device-type
                        :last-paired       timestamp}]
      (upsert-installation cofx installation))
    (confirm-message-processed cofx (or (:dedup-id cofx)
                                        (:js-obj cofx)))))

(fx/defn set-name [{:keys [db] :as cofx} installation-name]
  (let [new-account (assoc (get-in cofx [:db :account/account]) :installation-name installation-name)]
    {:db (assoc db :account/account new-account)
     :data-store/base-tx [(data-store.accounts/save-account-tx new-account)]}))

(fx/defn load-installations [{:keys [db all-installations]}]
  {:db (assoc db :pairing/installations (reduce
                                         (fn [acc {:keys [installation-id] :as i}]
                                           (assoc acc installation-id i))
                                         {}
                                         all-installations))})
