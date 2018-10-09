(ns status-im.pairing.core
  (:require
   [status-im.utils.fx :as fx]
   [status-im.utils.config :as config]
   [status-im.transport.message.protocol :as protocol]
   [status-im.data-store.installations :as data-store.installations]
   [status-im.utils.identicon :as identicon]
   [status-im.data-store.contacts :as data-store.contacts]
   [status-im.transport.message.pairing :as transport.pairing]))

(defn start [cofx]
  (let [{:keys [current-public-key web3]} (:db cofx)]
    {:shh/send-pairing-message {:web3    web3
                                :src     current-public-key
                                :payload []}}))

(defn merge-contact [local remote]
  (let [[old-contact new-contact] (sort-by :last-updated [local remote])]
    (-> local
        (merge new-contact)
        (assoc :photo-path
               (or (:photo-path new-contact)
                   (:photo-path old-contact)
                   (identicon/identicon (:whisper-identity local))))
        (assoc :pending? (boolean
                          (and (:pending? local true)
                               (:pending? remote true)))))))

(def merge-contacts (partial merge-with merge-contact))

(defn handle-bundles-added [{:keys [db]} bundle]
  (let [dev-mode? (get-in db [:account/account :dev-mode?])]
    (when (config/pairing-enabled? dev-mode?)
      (let [installation-id  (:installationID bundle)
            new-installation {:installation-id installation-id
                              :confirmed? false}]
        (when
         (and (= (:identity bundle)
                 (:current-public-key db))
              (not (get-in db [:pairing/installations installation-id])))
          {:db (assoc-in db
                         [:pairing/installations installation-id]
                         new-installation)
           :data-store/tx [(data-store.installations/save new-installation)]})))))

(defn sync-installation-messages [{:keys [db]}]
  (let [contacts (:contacts/contacts db)]
    (map
     (fn [[k v]] (transport.pairing/SyncInstallation. {k (dissoc v :photo-path)}))
     contacts)))

(defn send-installation-message [cofx]
  ;; The message needs to be broken up in chunks as we hit the whisper size limit
  (let [{:keys [current-public-key web3]} (:db cofx)
        sync-messages (sync-installation-messages cofx)]
    {:shh/send-direct-message
     (map #(hash-map :web3 web3
                     :src current-public-key
                     :dst current-public-key
                     :payload %) sync-messages)}))

(defn handle-sync-installation [{:keys [db] :as cofx} {:keys [contacts]} sender]
  (let [dev-mode? (get-in db [:account/account :dev-mode?])]
    (when (and (config/pairing-enabled? dev-mode?)
               (= sender (get-in cofx [:db :current-public-key])))
      (let [new-contacts  (merge-contacts (:contacts/contacts db) contacts)]
        {:db (assoc db :contacts/contacts new-contacts)
         :data-store/tx [(data-store.contacts/save-contacts-tx (vals new-contacts))]}))))

(fx/defn load-installations [{:keys [db all-installations]}]
  {:db (assoc db :pairing/installations (reduce
                                         (fn [acc {:keys [installation-id] :as i}]
                                           (assoc acc installation-id i))
                                         {}
                                         all-installations))})
