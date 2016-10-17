(ns status-im.contacts.handlers
  (:require [re-frame.core :refer [after dispatch]]
            [status-im.utils.handlers :refer [register-handler]]
            [status-im.data-store.contacts :as contacts]
            [status-im.utils.crypt :refer [encrypt]]
            [clojure.string :as s]
            [status-im.protocol.core :as protocol]
            [status-im.utils.utils :refer [http-post]]
            [status-im.utils.phone-number :refer [format-phone-number]]
            [status-im.utils.handlers :as u]
            [status-im.utils.utils :refer [require]]
            [status-im.navigation.handlers :as nav]
            [status-im.utils.random :as random]
            [taoensso.timbre :as log]))


(defmethod nav/preload-data! :group-contacts
  [db [_ _ group]]
  (if group
    (assoc db :contacts-group group)
    db))

(defmethod nav/preload-data! :new-group
  [db _]
  (-> db
      (assoc :new-group #{})
      (assoc :new-chat-name nil)))

(defmethod nav/preload-data! :contact-list
  [db [_ _ click-handler]]
  (assoc db :contacts-click-handler click-handler))

(defn save-contact
  [_ [_ contact]]
  (contacts/save contact))

(defn watch-contact
  [{:keys [web3]} [_ {:keys [whisper-identity public-key private-key]}]]
  (when (and public-key private-key)
    (protocol/watch-user! {:web3     web3
                           :identity whisper-identity
                           :keypair  {:public  public-key
                                      :private private-key}
                           :callback #(dispatch [:incoming-message %1 %2])})))

(register-handler :watch-contact (u/side-effect! watch-contact))

(defn send-contact-request
  [{:keys [current-public-key web3 current-account-id accounts]} [_ contact]]
  (let [{:keys [whisper-identity]} contact
        {:keys [name photo-path updates-public-key updates-private-key]} (get accounts current-account-id)]
    (protocol/contact-request!
      {:web3    web3
       :message {:from       current-public-key
                 :to         whisper-identity
                 :message-id (random/id)
                 :payload    {:contact {:name          name
                                        :profile-image photo-path
                                        :address       current-account-id}
                              :keypair {:public  updates-public-key
                                        :private updates-private-key}}}})))

(register-handler :update-contact!
  (-> (fn [db [_ {:keys [whisper-identity] :as contact}]]
        (update-in db [:contacts whisper-identity] merge contact))
      ((after save-contact))))

(defn load-contacts! [db _]
  (let [contacts (->> (contacts/get-all)
                      (map (fn [{:keys [whisper-identity] :as contact}]
                             [whisper-identity contact]))
                      (into {}))]
    (doseq [[_ contact] contacts]
      (dispatch [:watch-contact contact]))
    (assoc db :contacts contacts)))

(register-handler :load-contacts load-contacts!)

;; TODO see https://github.com/rt2zz/react-native-contacts/issues/45
(def react-native-contacts (require "react-native-contacts"))

(defn contact-name [contact]
  (->> contact
       ((juxt :givenName :middleName :familyName))
       (remove s/blank?)
       (s/join " ")))

(defn normalize-phone-contacts [contacts]
  (let [contacts' (js->clj contacts :keywordize-keys true)]
    (map (fn [{:keys [thumbnailPath phoneNumbers] :as contact}]
           {:name          (contact-name contact)
            :photo-path    thumbnailPath
            :phone-numbers phoneNumbers}) contacts')))

(defn fetch-contacts-from-phone!
  [_ _]
  (.getAll react-native-contacts
           (fn [error contacts]
             (if error
               (log/debug :error-on-fetching-loading error)
               (let [contacts' (normalize-phone-contacts contacts)]
                 (dispatch [:get-contacts-identities contacts']))))))

(register-handler :sync-contacts
  (u/side-effect! fetch-contacts-from-phone!))

(defn get-contacts-by-hash [contacts]
  (->> contacts
       (mapcat (fn [{:keys [phone-numbers] :as contact}]
                 (map (fn [{:keys [number]}]
                        (let [number' (format-phone-number number)]
                          [(encrypt number')
                           (-> contact
                               (assoc :phone-number number')
                               (dissoc :phone-numbers))]))
                      phone-numbers)))
       (into {})))

(defn add-identity [contacts-by-hash contacts]
  (map (fn [{:keys [phone-number-hash whisper-identity address]}]
         (let [contact (contacts-by-hash phone-number-hash)]
           (assoc contact :whisper-identity whisper-identity
                          :address address)))
       (js->clj contacts)))

(defn request-stored-contacts [contacts]
  (let [contacts-by-hash (get-contacts-by-hash contacts)
        data (or (keys contacts-by-hash) ())]
    (http-post "get-contacts" {:phone-number-hashes data}
               (fn [{:keys [contacts]}]
                 (let [contacts' (add-identity contacts-by-hash contacts)]
                   (dispatch [:add-contacts contacts']))))))

(defn get-identities-by-contacts! [_ [_ contacts]]
  (request-stored-contacts contacts))

(register-handler :get-contacts-identities
  (u/side-effect! get-identities-by-contacts!))

(defn save-contacts! [{:keys [new-contacts]} _]
  (contacts/save-all new-contacts))

(defn update-pending-status [old-contacts {:keys [whisper-identity pending] :as contact}]
  (let [{old-pending :pending
         :as         old-contact} (get old-contacts whisper-identity)]
    (if old-contact
      (assoc contact :pending (and old-pending pending))
      (assoc contact :pending pending))))

(defn add-new-contacts
  [{:keys [contacts] :as db} [_ new-contacts]]
  (let [identities (set (map :whisper-identity contacts))
        new-contacts' (->> new-contacts
                           (map #(update-pending-status contacts %))
                           (remove #(identities (:whisper-identity %)))
                           (map #(vector (:whisper-identity %) %))
                           (into {}))]
    (-> db
        (update :contacts merge new-contacts')
        (assoc :new-contacts (vals new-contacts')))))

(register-handler :add-contacts
  (after save-contacts!)
  add-new-contacts)

(defn add-new-contact [db [_ {:keys [whisper-identity] :as contact}]]
  (-> db
      (update :contacts assoc whisper-identity contact)
      (assoc :new-contact-identity "")))

(register-handler :add-new-contact
  (u/side-effect!
    (fn [_ [_ {:keys [whisper-identity] :as contact}]]
      (when-not (contacts/get-by-id whisper-identity)
        (dispatch [::prepare-contact contact])))))

(register-handler ::prepare-contact
  (-> add-new-contact
      ((after save-contact))
      ((after send-contact-request))))

(register-handler ::update-pending-contact
  (-> add-new-contact
      ((after save-contact))))

(register-handler :add-pending-contact
  (u/side-effect!
    (fn [{:keys [current-public-key web3 current-account-id accounts]}
         [_ {:keys [whisper-identity] :as contact}]]
      (let [contact (assoc contact :pending false)
            {:keys [name photo-path updates-public-key updates-private-key]}
            (accounts current-account-id)]
        (protocol/contact-request!
          {:web3    web3
           :message {:from       current-public-key
                     :to         whisper-identity
                     :message-id (random/id)
                     :payload    {:contact {:name          name
                                            :profile-image photo-path
                                            :address       current-account-id}
                                  :keypair {:public  updates-public-key
                                            :private updates-private-key}}}})
        (dispatch [::update-pending-contact contact])))))

(defn set-contact-identity-from-qr
  [db [_ _ contact-identity]]
  (assoc db :new-contact-identity contact-identity))

(register-handler :set-contact-identity-from-qr set-contact-identity-from-qr)

(register-handler :contact-update-received
  (u/side-effect!
    (fn [{:keys [chats] :as db} [_ {:keys [from payload]}]]
      (let [{:keys [content timestamp]} payload
            {:keys [status name profile-image]} (:profile content)
            prev-last-updated (get-in db [:contacts from :last-updated])]
        (if (<= prev-last-updated timestamp)
          (let [contact {:whisper-identity from
                         :name             name
                         :photo-path       profile-image
                         :status           status
                         :last-updated     timestamp}]
            (dispatch [:check-status! contact payload])
            (dispatch [:update-contact! contact])
            (when (chats from)
              (dispatch [:update-chat! {:chat-id from
                                        :name    name}]))))))))

(register-handler :contact-online-received
  (u/side-effect!
    (fn [db [_ {:keys               [from]
                {:keys [timestamp]} :payload}]]
      (let [prev-last-online (get-in db [:contacts from :last-online])]
        (when (< prev-last-online timestamp)
          (protocol/reset-pending-messages! from)
          (dispatch [:update-contact! {:whisper-identity from
                                       :last-online      timestamp}]))))))

