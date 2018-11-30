(ns status-im.pairing.core
  (:require [re-frame.core :as re-frame]
            [clojure.string :as string]
            [status-im.i18n :as i18n]
            [status-im.utils.fx :as fx]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.config :as config]
            [status-im.utils.platform :as utils.platform]
            [status-im.accounts.db :as accounts.db]
            [status-im.transport.message.protocol :as protocol]
            [status-im.data-store.installations :as data-store.installations]
            [status-im.native-module.core :as native-module]
            [status-im.utils.identicon :as identicon]
            [status-im.data-store.contacts :as data-store.contacts]
            [status-im.data-store.accounts :as data-store.accounts]
            [status-im.transport.message.pairing :as transport.pairing]))

(def contact-batch-n 4)

(defn- parse-response [response-js]
  (-> response-js
      js/JSON.parse
      (js->clj :keywordize-keys true)))

(defn pair-installation [cofx]
  (let [installation-id (get-in cofx [:db :account/account :installation-id])
        device-type     utils.platform/os]
    (protocol/send (transport.pairing/PairInstallation. installation-id device-type) nil cofx)))

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
  (let [[old-contact new-contact] (sort-by :last-updated [remote local])]
    (-> local
        (merge new-contact)
        (assoc :pending? (boolean
                          (and (:pending? local true)
                               (:pending? remote true)))))))

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
  {:db               (assoc-in db [:pairing/prompt-user-pop-up] true)
   :ui/show-confirmation {:title      (i18n/label :t/pairing-new-installation-detected-title)
                          :content    (i18n/label :t/pairing-new-installation-detected-content)
                          :confirm-button-text (i18n/label :t/pairing-go-to-installation)
                          :cancel-button-text  (i18n/label :t/cancel)
                          :on-cancel  #(re-frame/dispatch [:pairing.ui/prompt-dismissed])
                          :on-accept #(re-frame/dispatch [:pairing.ui/prompt-accepted])}})

(fx/defn upsert-installation [{:keys [db]} {:keys [installation-id] :as new-installation}]
  (let [old-installation (get-in db [:pairing/installations installation-id])
        updated-installation (merge old-installation new-installation)]
    {:db (assoc-in db
                   [:pairing/installations installation-id]
                   updated-installation)
     :data-store/tx [(data-store.installations/save updated-installation)]}))

(defn handle-bundles-added [{:keys [db] :as cofx} bundle]
  (let [dev-mode? (get-in db [:account/account :dev-mode?])]
    (when (config/pairing-enabled? dev-mode?)
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
                    #(when-not (get-in % [:db :pairing/prompt-user-pop-up])
                       (prompt-user-on-new-installation %))))))))

(defn sync-installation-account-message [{:keys [db]}]
  (let [account (-> db
                    :account/account
                    (select-keys account-mergeable-keys))]
    (transport.pairing/SyncInstallation. {} account)))

(defn- contact-batch->sync-installation-message [batch]
  (let [contacts-to-sync (reduce (fn [acc {:keys [public-key] :as contact}]
                                   (assoc acc public-key (dissoc contact :photo-path)))
                                 {}
                                 batch)]
    (transport.pairing/SyncInstallation. contacts-to-sync nil)))

(defn sync-installation-messages [{:keys [db] :as cofx}]
  (let [contacts (:contacts/contacts db)
        contact-batches (partition-all contact-batch-n (->> contacts
                                                            vals
                                                            (remove :dapp?)))]
    (conj (mapv contact-batch->sync-installation-message contact-batches)
          (sync-installation-account-message cofx))))

(defn enable [{:keys [db]} installation-id]
  {:db (assoc-in db
                 [:pairing/installations installation-id :enabled?]
                 true)
   :data-store/tx [(data-store.installations/enable installation-id)]})

(defn disable [{:keys [db]} installation-id]
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

(defn enable-fx [_ installation-id]
  {:pairing/enable-installation installation-id})

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
     [{:web3 web3
       :src current-public-key
       :dst current-public-key
       :payload payload}]}))

(fx/defn send-installation-message-fx [cofx payload]
  (let [dev-mode? (get-in cofx [:db :account/account :dev-mode?])]
    (when (and (config/pairing-enabled? dev-mode?)
               (has-paired-installations? cofx))
      (protocol/send payload nil cofx))))

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

(defn handle-sync-installation [{:keys [db] :as cofx} {:keys [contacts account]} sender]
  (let [dev-mode? (get-in db [:account/account :dev-mode?])]
    (when (and (config/pairing-enabled? dev-mode?)
               (= sender (accounts.db/current-public-key cofx)))
      (let [new-contacts (merge-contacts (:contacts/contacts db) (ensure-photo-path contacts))
            new-account  (merge-account (:account/account db) account)]
        {:db                 (assoc db
                                    :contacts/contacts new-contacts
                                    :account/account new-account)
         :data-store/base-tx [(data-store.accounts/save-account-tx new-account)]
         :data-store/tx      [(data-store.contacts/save-contacts-tx (vals new-contacts))]}))))

(defn handle-pair-installation [{:keys [db] :as cofx} {:keys [installation-id device-type]} timestamp sender]
  (let [dev-mode? (get-in db [:account/account :dev-mode?])]
    (when (and (config/pairing-enabled? dev-mode?)
               (= sender (accounts.db/current-public-key cofx))
               (not= (get-in db [:account/account :installation-id]) installation-id))
      (let [installation {:installation-id installation-id
                          :device-type     device-type
                          :last-paired     timestamp}]
        (upsert-installation cofx installation)))))

(fx/defn load-installations [{:keys [db all-installations]}]
  {:db (assoc db :pairing/installations (reduce
                                         (fn [acc {:keys [installation-id] :as i}]
                                           (assoc acc installation-id i))
                                         {}
                                         all-installations))})
