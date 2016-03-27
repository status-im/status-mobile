(ns syng-im.models.contacts
  (:require [cljs.core.async :as async :refer [chan put! <! >!]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.utils.utils :refer [log toast]]
            [syng-im.persistence.realm :as realm]))

(def fake-contacts? true)

(def react-native-contacts (js/require "react-native-contacts"))

(defn- generate-contact [n]
  {:name (str "Contact " n)
   :photo-path ""
   :phone-numbers [{:label "mobile" :number (apply str (repeat 7 n))}]
   :delivery-status (if (< (rand) 0.5) :delivered :seen)
   :datetime "15:30"
   :new-messages-count (rand-int 3)
   :online (< (rand) 0.5)})

(defn- generate-contacts [n]
  (map generate-contact (range 1 (inc n))))

(defn load-phone-contacts []
  (let [ch (chan)]
    (if fake-contacts?
      (put! ch {:error nil, :contacts (generate-contacts 10)})
      (.getAll react-native-contacts
               (fn [error raw-contacts]
                 (put! ch
                       {:error error
                        :contacts
                        (when (not error)
                          (log  raw-contacts)
                          (map (fn [contact]
                                 ;; (toast (str contact))
                                 (merge contact
                                        (generate-contact 1)
                                        {:name (:givenName contact)
                                         :photo-path (:thumbnailPath contact)
                                         :phone-numbers (:phoneNumbers contact)}))
                               (js->clj raw-contacts :keywordize-keys true)))}))))
    ch))

(defn- get-contacts []
  (if fake-contacts?
    [{:phone-number "123"
      :whisper-identity "abc"
      :name "fake"
      :photo-path ""}]
    (realm/get-list "Contact")))

(defn load-syng-contacts [db]
  (let [contacts (map (fn [contact]
                        (merge contact
                               {:delivery-status (if (< (rand) 0.5) :delivered :seen)
                                :datetime "15:30"
                                :new-messages-count (rand-int 3)
                                :online (< (rand) 0.5)}))
                      (get-contacts))]
    (assoc db :contacts contacts)))

(defn- create-contact [{:keys [phone-number whisper-identity name photo-path]}]
  (realm/create "Contact"
                {:phone-number phone-number
                 :whisper-identity whisper-identity
                 :name (or name "")
                 :photo-path (or photo-path "")}))

(defn- contact-exist? [contacts contact]
  (some #(= (:phone-number contact) (:phone-number %)) contacts))

(defn- add-contacts [contacts]
  (realm/write (fn []
                 (let [db-contacts (get-contacts)]
                   (dorun (map (fn [contact]
                                 (if (not (contact-exist? db-contacts contact))
                                   (create-contact contact)
                                   ;; TODO else override?
                                   ))
                               contacts))))))

(defn save-syng-contacts [syng-contacts]
  (add-contacts syng-contacts))
