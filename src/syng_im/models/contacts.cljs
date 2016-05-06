(ns syng-im.models.contacts
  (:require [cljs.core.async :as async :refer [chan put! <! >!]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.db :as db]
            [syng-im.utils.utils :refer [log toast]]
            [syng-im.persistence.realm :as realm]
            [syng-im.persistence.realm :as r]
            [syng-im.persistence.realm-queries :refer [include-query
                                                       exclude-query]]
            [clojure.string :as s]))

;; TODO see https://github.com/rt2zz/react-native-contacts/issues/45
(def fake-phone-contacts? false)
(def fake-contacts? false)

(def react-native-contacts (js/require "react-native-contacts"))

(defn- generate-contact [n]
  {:name               (str "Contact " n)
   :photo-path         ""
   :phone-numbers      [{:label "mobile" :number (apply str (repeat 7 n))}]
   :delivery-status    (if (< (rand) 0.5) :delivered :seen)
   :datetime           "15:30"
   :new-messages-count (rand-int 3)
   :online             (< (rand) 0.5)})

(defn- generate-contacts [n]
  (map generate-contact (range 1 (inc n))))

(defn load-phone-contacts []
  (let [ch (chan)]
    (if fake-phone-contacts?
      (put! ch {:error nil, :contacts (generate-contacts 10)})
      (.getAll react-native-contacts
               (fn [error raw-contacts]
                 (put! ch
                       {:error error
                        :contacts
                               (when (not error)
                                 (log raw-contacts)
                                 (map (fn [contact]
                                        (merge contact
                                               (generate-contact 1)
                                               {:name          (:givenName contact)
                                                :photo-path    (:thumbnailPath contact)
                                                :phone-numbers (:phoneNumbers contact)}))
                                      (js->clj raw-contacts :keywordize-keys true)))}))))
    ch))

(defn- get-contacts []
  (if fake-contacts?
    [{:phone-number     "123"
      :whisper-identity "abc"
      :name             "fake"
      :photo-path       ""}]
    (realm/get-list :contacts)))

(defn load-syng-contacts [db]
  (let [contacts (map (fn [contact]
                        (merge contact
                               {:delivery-status    (if (< (rand) 0.5) :delivered :seen)
                                :datetime           "15:30"
                                :new-messages-count (rand-int 3)
                                :online             (< (rand) 0.5)}))
                      (get-contacts))]
    (assoc db :contacts contacts)))

(defn- create-contact [{:keys [phone-number whisper-identity name photo-path]}]
  (realm/create :contacts
                {:phone-number     phone-number
                 :whisper-identity whisper-identity
                 :name             (or name "")
                 :photo-path       (or photo-path "")}))

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


;;;;;;;;;;;;;;;;;;;;----------------------------------------------

(defn contacts-list []
  (r/sorted (r/get-all :contacts) :name :asc))

(defn contacts-list-exclude [exclude-idents]
  (let [query (exclude-query :whisper-identity exclude-idents)]
    (-> (r/get-all :contacts)
        (r/filtered query)
        (r/sorted :name :asc))))

(defn contacts-list-include [include-indents]
  (let [query (include-query :whisper-identity include-indents)]
    (-> (r/get-all :contacts)
        (r/filtered query)
        (r/sorted :name :asc))))

(defn contact-by-identity [identity]
  (r/single-cljs (r/get-by-field :contacts :whisper-identity identity)))

;;;;;;;;;;;;;;;;;;;;----------------------------------------------

(defn set-contact-identity [db contact-id]
  (assoc-in db db/contact-identity-path contact-id))

(defn contact-identity [db]
  (get-in db db/contact-identity-path))

(comment

  (r/write #(create-contact {:phone-number     "0543072333"
                             :whisper-identity "0x04e43e861a6dd99ad9eee7bd58af89dcaa430188ebec8698de7b7bad54573324fff4ac5cb9bb277af317efd7abfc917b91bf48cc41e40bf70062fd79400016a1f9"
                             :name             "Splinter"
                             :photo-path       ""}))

  (r/write #(create-contact {:phone-number     "0544828649"
                             :whisper-identity "0x0487954e7fa746d8cf787403c2c491aadad540b9bb1f0f7b8184792e91c33b6a394079295f5777ec6d4af9ad5ba24794b3ff1ec8be9ff6a708c85a163733192665"
                             :name             "Exodius"
                             :photo-path       ""}))

  (r/write #(create-contact {:phone-number     "0522222222"
                             :whisper-identity "0x0407c278af94e0b4599645023f5bec03cbdb3973bd0ae33b94c6a5885d9d20e82ff3f3c3584a637ba016af40bac2f711fd6028045756f561e36e4b07d0c2b4e623"
                             :name             "Mr. Eagle"
                             :photo-path       ""}))

  (r/write #(create-contact {:phone-number     "0533333333"
                             :whisper-identity "0x04512f852558ea09d09419019f3f443ec03ff2c1913c48e567723d70e5abf239ed87fb62486b90b85e12de5d327501c1993c9905a69f2ca7e1bfbaab12dd033313"
                             :name             "Mr. PiggyBear"
                             :photo-path       ""}))

  (contacts-list)

  (:new-group @re-frame.db/app-db)

  )
