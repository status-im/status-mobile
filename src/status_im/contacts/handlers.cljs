(ns status-im.contacts.handlers
  (:require [re-frame.core :refer [register-handler after dispatch]]
            [status-im.models.contacts :as contacts]
            [status-im.utils.crypt :refer [encrypt]]
            [clojure.string :as s]
            [status-im.utils.utils :refer [http-post]]
            [status-im.utils.phone-number :refer [format-phone-number]]
            [status-im.utils.handlers :as u]))

(defn save-contact
  [_ [_ contact]]
  (contacts/save-contacts [contact]))

(register-handler :add-contact
  (-> (fn [db [_ {:keys [whisper-identity] :as contact}]]
        (update db :contacts assoc whisper-identity contact))
      ((after save-contact))))

(defn load-contacts! [db _]
  (let [contacts (->> (contacts/get-contacts)
                      (map (fn [{:keys [whisper-identity] :as contact}]
                             [whisper-identity contact]))
                      (into {}))]
    (assoc db :contacts contacts)))

(register-handler :load-contacts load-contacts!)

;; TODO see https://github.com/rt2zz/react-native-contacts/issues/45
(def react-native-contacts (js/require "react-native-contacts"))

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
               (dispatch [:error-on-fetching-loading error])
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
  (map (fn [{:keys [phone-number-hash whisper-identity]}]
         (let [contact (contacts-by-hash phone-number-hash)]
           (assoc contact :whisper-identity whisper-identity)))
       (js->clj contacts)))

(defn request-stored-contacts [contacts]
  (let [contacts-by-hash (get-contacts-by-hash contacts)
        data             (or (keys contacts-by-hash) ())]
    (http-post "get-contacts" {:phone-number-hashes data}
               (fn [{:keys [contacts]}]
                 (let [contacts' (add-identity contacts-by-hash contacts)]
                   (dispatch [:add-contacts contacts']))))))

(defn get-identities-by-contacts! [_ [_ contacts]]
  (request-stored-contacts contacts))

(register-handler :get-contacts-identities
  (u/side-effect! get-identities-by-contacts!))

(defn save-contacts! [{:keys [new-contacts]} _]
  (contacts/save-contacts new-contacts))

(defn add-new-contacts
  [{:keys [contacts] :as db} [_ new-contacts]]
  (let [identities    (set (map :whisper-identity contacts))
        new-contacts' (remove #(identities (:whisper-identity %)) new-contacts)]
    (-> db
        (update :contacts concat new-contacts')
        (assoc :new-contacts new-contacts'))))

(register-handler :add-contacts
  (after save-contacts!)
  add-new-contacts)

(defn add-new-contact [db [_ {:keys [whisper-identity] :as contact}]]
  (-> db
      (update :contacts assoc whisper-identity contact)
      (assoc :new-contact {:name ""
                           :address ""
                           :whisper-identity ""
                           :phone-number ""})))

(register-handler :add-new-contact
  (after save-contact)
  add-new-contact)

(defn set-new-contact-from-qr
  [{:keys [new-contact] :as db} [_ _ qr-contact]]
  (assoc db :new-contact (merge new-contact qr-contact)))

(register-handler :set-new-contact-from-qr set-new-contact-from-qr)
